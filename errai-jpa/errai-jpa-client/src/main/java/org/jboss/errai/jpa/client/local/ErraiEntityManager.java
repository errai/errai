package org.jboss.errai.jpa.client.local;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.FlushModeType;
import javax.persistence.LockModeType;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SingularAttribute;

import org.jboss.errai.common.client.api.WrappedPortable;
import org.jboss.errai.databinding.client.BindableProxy;
import org.jboss.errai.jpa.client.local.backend.StorageBackend;
import org.jboss.errai.jpa.client.local.backend.WebStorageBackend;
import org.jboss.errai.marshalling.client.api.MarshallerFramework;

/**
 * The Errai specialization of the JPA 2.0 EntityManager interface, together
 * with an implementation of much of the logic. When the end-user project is
 * compiled, a concrete subclass of this class is generated. The subclass
 * populates the metamodel with the type and attribute information required for
 * enumerating and creating entity instances, and enumerating, reading, and
 * writing their fields.
 *
 * @author Jonathan Fuerth <jfuerth@gmail.com>
 */
public abstract class ErraiEntityManager implements EntityManager {

  /**
   * Hint that can be used with {@link #find(Class, Object, Map)} to specify
   * that the find operation should not have any side effects, such as adding
   * the entity to the persistence context and delivering PostLoad event.
   */
  static final String NO_SIDE_EFFECTS = "errai.jpa.NO_SIDE_EFFECTS";

  // magic incantation. ooga booga!
  static {
    // ensure that the marshalling framework has been initialized
    MarshallerFramework.initializeDefaultSessionProvider();
  }

  /**
   * The metamodel. Gets populated on first call to {@link #getMetamodel()}.
   */
  final ErraiMetamodel metamodel = new ErraiMetamodel();

  /**
   * All persistent instances known to this entity manager.
   */
  final Map<Key<?, ?>, Object> persistenceContext = new HashMap<Key<?, ?>, Object>();

  /**
   * All of the entities that are partly constructed but are still getting their
   * references connected up. This is required in order to prevent infinite
   * recursion when demarshalling cyclic object graphs.
   */
  private Map<Key<Object, Object>, Object> partiallyConstructedEntities = new HashMap<Key<Object, Object>, Object>();

  /**
   * The actual storage backend.
   */
  private final StorageBackend backend = new WebStorageBackend(this); // XXX publishing reference to partially constructed object

  /**
   * All the named queries. Populated by a generated method in the
   * GeneratedErraiEntityManager subclass.
   */
  final Map<String, TypedQueryFactory> namedQueries = new HashMap<String, TypedQueryFactory>();

  /**
   * Constructor for subclasses.
   */
  protected ErraiEntityManager() {
  }

  /**
   * Populates the metamodel of this EntityManager. Called by getMetamodel() one
   * time only.
   * <p>
   * The implementation of this method must populate the metamodel with all
   * known entity types, managed types, and so on. The implementation must
   * freeze the metamodel before returning. The first call to
   * {@link #getMetamodel()} throws RuntimeException if
   * {@code this.metamodel.isFrozen() == false} after this method returns.
   * <p>
   * Note that this method is normally implemented by a generated subclass, but
   * handwritten subclasses may also be useful for testing purposes.
   */
  protected abstract void populateMetamodel();

  /**
   * Populates the collection of named queries in this EntityManager. Called by
   * {@link #createNamedQuery(String, Class)} if the namedQueries map is empty.
   * <p>
   * The implementation of this method must add factories for all known named
   * queries to the {@link #namedQueries} map.
   * <p>
   * Note that this method is normally implemented by a generated subclass, but
   * handwritten subclasses may also be useful for testing purposes.
   */
  protected abstract void populateNamedQueries();

  /**
   * This method performs the unchecked (but safe) cast of
   * {@code object.getClass()} to {@code Class<T>}. Using this method avoids the
   * need to mark larger blocks of code with a SuppressWarnings annotation.
   *
   * @param object
   *          The object to get the associated Class object from. Not null.
   * @return The Class object with type parameter fixed to the compile-time type
   *         of object.
   */
  @SuppressWarnings("unchecked")
  private <T> Class<T> getNarrowedClass(T object) {
    Object o = object;
    while (o instanceof WrappedPortable) {
      o = ((WrappedPortable) o).unwrap();
    }
    return (Class<T>) o.getClass();
  }

  /**
   * Performs an unchecked cast of the given value to the given type. This
   * inlineable method exists because GWT does not implement Class.cast(), which
   * necessitates unchecked casts in generic code. It's important to narrow the
   * unchecked warning suppression to the smallest possible amount of code.
   *
   * @param type The type that {@code value} is believed to have.
   * @param value The value to cast.
   * @return value
   */
  @SuppressWarnings("unchecked")
  private static final <T> T cast(Class<T> type, Object value) {
    return (T) value;
  }

  /**
   * Generates a new ID value for the given entity instance that is guaranteed
   * to be unique <i>on this client</i>. If the entity instance with this ID is
   * ever synchronized to the server, this client-local ID will be replaced by a
   * permanent server-generated ID.
   * <p>
   * This method only works for attributes that are configured as
   * {@code @GeneratedValue}s. The GenerationType has no effect locally, but of
   * course it will come into play on the server side when and if the entity is
   * synchronized to the server.
   *
   * @param entityInstance
   *          the entity instance to receive the generated ID. This attribute of
   *          that entity instance will be set to the newly generated ID value.
   * @return the generated ID value, which has already been set on the entity
   *         instance.
   */
  private <X, T> T generateAndSetLocalId(X entityInstance, ErraiSingularAttribute<X, T> attr) {
    T nextId = attr.getValueGenerator().next();
    attr.set(entityInstance, nextId);
    return nextId;
  }

  /**
   * As they say in television, "this is where the magic happens." This method
   * attempts to resolve the given object as an entity and put that entity into
   * the given state, taking into account its existing state and performing the
   * required side effects during the state transition.
   */
  private <X> void changeEntityState(X entity, EntityState newState) {
    ErraiEntityType<X> entityType = getMetamodel().entity(getNarrowedClass(entity));

    final Key<X, ?> key = keyFor(entityType, entity);
    final EntityState oldState = getState(key);

    switch (newState) {
    case MANAGED:
      switch (oldState) {
      case NEW:
      case REMOVED:
        entityType.deliverPrePersist(entity);
        persistenceContext.put(key, entity);
        backend.put(key, entity);
        entityType.deliverPostPersist(entity);
        // FALLTHROUGH
      case MANAGED:
        // no-op, but cascade to relatives
        break;
      case DETACHED:
        throw new EntityExistsException();
      }
      break;
    case DETACHED:
      switch (oldState) {
      case NEW:
      case DETACHED:
        // ignore
        break;
      case MANAGED:
      case REMOVED:
        persistenceContext.remove(key);
        break;
      }
      break;
    case REMOVED:
      switch (oldState) {
      case NEW:
      case MANAGED:
        entityType.deliverPreRemove(entity);
        persistenceContext.remove(key);
        backend.remove(key);
        entityType.deliverPostRemove(entity);
        break;
      case DETACHED:
        throw new IllegalArgumentException("Entities can't transition from " + oldState + " to " + newState);
      case REMOVED:
        // ignore
        break;
      }
      break;
    case NEW:
      throw new IllegalArgumentException("Entities can't transition from " + oldState + " to " + newState);
    }

    // Tell the BindableProxy that we changed the entity
    // (we haven't _necessarily_ changed anything.. if this becomes a performance problem,
    // we can set a flag in the above state change logic make this call depend on that flag)
    if (entity instanceof BindableProxy) {
      ((BindableProxy<?>) entity).updateWidgets();
    }

    // now cascade the operation
    for (SingularAttribute<? super X, ?> a : entityType.getSingularAttributes()) {
      ErraiSingularAttribute<? super X, ?> attrib = (ErraiSingularAttribute<? super X, ?>) a;
      cascadeStateChange(attrib, entity, newState);
    }
    for (PluralAttribute<? super X, ?, ?> a : entityType.getPluralAttributes()) {
      ErraiPluralAttribute<? super X, ?, ?> attrib = (ErraiPluralAttribute<? super X, ?, ?>) a;
      cascadeStateChange(attrib, entity, newState);
    }

  }

  /**
   * Creates the key that describes the given entity, <b>generating and setting
   * it if it is presently unset and the given entity type's ID is configured to
   * be generated on demand</b>. This version of the {@code keyFor()} method
   * assumes the given object's entity type can be obtained by calling {@code
   * entity.getClass()}. If you already have a specific entity type in mind, use
   * the {@link #keyFor(ErraiEntityType, Object)} version of the method.
   *
   * @param entityType
   *          The entity type of the entity
   * @param entity
   *          The entity instance. <b>Side effect: this instance may have its ID
   *          value initialized as a result of this call</b>.
   * @return The key for the given entity, which--for generated values--may have
   *         just been set on the entity.
   */
  public <X> Key<X, ?> keyFor(X entity) {
    ErraiEntityType<X> entityType = getMetamodel().entity(getNarrowedClass(entity));
    return keyFor(entityType, entity);
  }

  /**
   * Creates the key that describes the given entity, <b>generating and setting
   * it if it is presently unset and the given entity type's ID is configured to
   * be generated on demand</b>.
   *
   * @param entityType
   *          The entity type of the entity
   * @param entity
   *          The entity instance. <b>Side effect: this instance may have its ID
   *          value initialized as a result of this call</b>.
   * @return The key for the given entity, which--for generated values--may have
   *         just been set on the entity.
   */
  public <X> Key<X, ?> keyFor(ErraiEntityType<X> entityType, X entity) {
    ErraiSingularAttribute<? super X, ?> idAttr;
    switch (entityType.getIdType().getPersistenceType()) {
    case BASIC:
      idAttr = entityType.getId(entityType.getIdType().getJavaType());
      break;
    default:
      throw new RuntimeException(entityType.getIdType().getPersistenceType() + " ids are not yet supported");
    }
    Object id = idAttr.get(entity);
    if ( id == null || (id instanceof Number && ((Number) id).doubleValue() == 0.0) ) {
      id = generateAndSetLocalId(entity, idAttr);
      // TODO track this generated ID for later reconciliation with the server
    }
    return new Key<X, Object>(entityType, id);
  }

  /**
   * Determines the current state of the entity identified by the given key.
   *
   * @param key
   *          The entity key
   * @return The current state of the given entity according to this entity
   *         manager.
   */
  private <T> EntityState getState(Key<T, ?> key) {
    final EntityState oldState;
    if (persistenceContext.get(key) != null) {
      oldState = EntityState.MANAGED;
    }
    else if (backend.contains(key)) {
      oldState = EntityState.DETACHED;
    }
    else {
      oldState = EntityState.NEW;
    }
    // TODO handle REMOVED state
    return oldState;
  }

  /**
   * Subroutine of {@link #changeEntityState(Object, EntityState)}. Cascades the
   * given change of state onto all of the related entities whose cascade rules
   * are appropriate to the new state. It is assumed that the given entity is
   * already in the given state.
   *
   * @param <X> the type of the owning entity we are cascading from
   * @param <R> the type of the related entity we are cascading to
   */
  private <X, R> void cascadeStateChange(ErraiAttribute<X, R> cascadeAcross, X owningEntity, EntityState newState) {
    if (!cascadeAcross.isAssociation()) return;

    CascadeType cascadeType;
    switch (newState) {
    case DETACHED: cascadeType = CascadeType.DETACH; break;
    case MANAGED: cascadeType = CascadeType.PERSIST; break; // XXX could be a MERGE once that's implemented
    case REMOVED: cascadeType = CascadeType.REMOVE; break;
    case NEW: throw new IllegalArgumentException();
    default: throw new AssertionError("Unknown entity state " + newState);
    }
    R relatedEntity = cascadeAcross.get(owningEntity);
    System.out.println("*** Cascade " + cascadeType + " across " + cascadeAcross.getName() + " to " + relatedEntity + "?");
    if (relatedEntity == null) {
      System.out.println("    No (because it's null)");
    }
    else if (cascadeAcross.cascades(cascadeType)) {
      System.out.println("    Yes");
      if (cascadeAcross.isCollection()) {
        for (Object element : (Iterable<?>) relatedEntity) {
          changeEntityState(element, newState);
        }
      }
      else {
        changeEntityState(relatedEntity, newState);
      }
    }
    else {
      System.out.println("    No");
      if (cascadeType == CascadeType.PERSIST && !contains(relatedEntity)) {
        throw new IllegalStateException(
                "Entity " + owningEntity + " references an unsaved entity via relationship attribute [" +
                cascadeAcross.getName() + "]. Save related attribute before flushing or change" +
                " cascade rule to include PERSIST.");
      }
    }
  }

  /**
   * Updates the persistent representation of the given entity in this entity
   * manager's storage backend.
   * <p>
   * This methods checks if the entity value has truly changed, and if so it
   * fires the PreUpdate and PostUpdate events.
   * <p>
   * This method also verifies that the entity's current identity matches the
   * key's identity. In JPA 2.0, application code is not allowed to modify a
   * managed entity's ID attribute. This is just a safety check to ensure that
   * hasn't happened.
   *
   * @param key
   *          The entity's key in the persistence context.
   * @param entity
   *          The "live" entity value in the persistence context.
   * @throws PersistenceException
   *           if the entity's current ID attribute value differs from the one
   *           in the key (which would have been its identity when it first
   *           became managed).
   */
  private <X> void updateInBackend(Key<X, ?> key, X entity) {
    ErraiEntityType<X> entityType = getMetamodel().entity(getNarrowedClass(entity));
    if (backend.isModified(key, entity)) {
      Object currentId = entityType.getId(Object.class).get(entity);
      if (!key.getId().equals(currentId)) {
        throw new PersistenceException(
                "Detected ID attribute change in managed entity. Expected ID: " +
                key.getId() + "; Actual ID: " + currentId);
      }
      entityType.deliverPreUpdate(entity);
      backend.put(key, entity);
      entityType.deliverPostUpdate(entity);
    }
  }

  /**
   * Makes the Entity Manager aware of an entity instance that is in the process
   * of being constructed: its fields and references to other entities may not
   * yet be fully populated. Partially constructed entities are tracked
   * primarily for the benefit of the storage and marshaling backends; they are
   * not part of the persistence context and they do not cause any lifecycle
   * events to be fired.
   */
  @SuppressWarnings("unchecked")
  <X> void putPartiallyConstructedEntity(Key<X, ?> key, X instance) {
    partiallyConstructedEntities.put((Key<Object, Object>) key, (Object) instance);
  }

  /**
   * Retieves the partially constructed entity associated with the given key, if
   * any.
   *
   * @return the partially constructed entity, or null if there is not a
   *         partially constructed entity associated with the given key.
   * @see #putPartiallyConstructedEntity(Key, Object)
   */
  @SuppressWarnings("unchecked")
  <X> X getPartiallyConstructedEntity(Key<X, ?> key) {
    return (X) partiallyConstructedEntities.get(key);
  }

  /**
   * Removes the partially constructed entity associated with the given key, if
   * any. Has no effect if the given key is not associated with a partially
   * constructed entity.
   *
   * @see #putPartiallyConstructedEntity(Key, Object)
   */
  void removePartiallyConstructedEntity(Key<?, ?> key) {
    partiallyConstructedEntities.remove(key);
  }

  /**
   * EXPERIMENTAL. This method is very unlikely to survive in the long run.
   */
  public <X> List<X> findAll(ErraiEntityType<X> type, EntityJsonMatcher matcher) {
    return backend.getAll(type, matcher);
  }

  // -------------- Actual JPA API below this line -------------------

  @Override
  public ErraiMetamodel getMetamodel() {
    if (!metamodel.isFrozen()) {
      populateMetamodel();
      if (!metamodel.isFrozen()) {
        throw new RuntimeException("The populateMetamodel() method didn't call metamodel.freeze()!");
      }
    }
    return metamodel;
  }

  @Override
  public void persist(Object entity) {
    changeEntityState(entity, EntityState.MANAGED);
  }

  @Override
  public void flush() {
    // deferred backend operations not (yet!) implemented

    // persist updates to entities in the persistence context
    for (Map.Entry<Key<?, ?>, Object> entry : persistenceContext.entrySet()) {
      // type safety warning should go away when we have a real PersistenceContext implementation
      updateInBackend((Key<Object, ?>) entry.getKey(), entry.getValue());
    }
  }

  @Override
  public void detach(Object entity) {
    changeEntityState(entity, EntityState.DETACHED);
  }

  @Override
  public void clear() {
    List<?> entities = new ArrayList<Object>(persistenceContext.values());
    for (Object entity : entities) {
      detach(entity);
    }
  }

  @Override
  public <X> X find(Class<X> entityClass, Object primaryKey) {
    return find(entityClass, primaryKey, Collections.<String, Object>emptyMap());
  }

  @Override
  public <X> X find(Class<X> entityClass, Object primaryKey, Map<String, Object> properties) {
    Key<X, ?> key = Key.get(this, entityClass, primaryKey);
    return find(key, properties);
  }

  /**
   * Retrieves the entity instance identified by the given Key.
   *
   * @param key The key to look up. Must not be null.
   * @param properties JPA hints (standard and Errai-specific) for the lookup.
   * @return the entity instance, or null if the entity cannot be found.
   */
  public <X> X find(Key<X, ?> key, Map<String, Object> properties) {
    X entity = cast(key.getEntityType().getJavaType(), persistenceContext.get(key));
    if (entity == null) {
      entity = backend.get(key);
      if (entity != null && !properties.containsKey(NO_SIDE_EFFECTS)) {
        persistenceContext.put(key, entity);

        // XXX when persistenceContext gets its own class, this should go on the ultimate ingress point
        getMetamodel().entity(key.getEntityType().getJavaType()).deliverPostLoad(entity);
      }
    }
    return entity;
  }

  @Override
  public void remove(Object entity) {
    changeEntityState(entity, EntityState.REMOVED);
  }

  /**
   * Removes everything from the backend data store and clears the persistence context.
   */
  public void removeAll() {
    clear();
    backend.removeAll();
  }

  @Override
  public Query createNamedQuery(String name) {
    return createNamedQuery(name, Object.class);
  }

  @Override
  public <T> TypedQuery<T> createNamedQuery(String name, Class<T> resultClass) {
    if (namedQueries.isEmpty()) {
      populateNamedQueries();
    }
    TypedQueryFactory factory = namedQueries.get(name);
    if (factory == null) throw new IllegalArgumentException("No named query \"" + name + "\"");
    return factory.createIfCompatible(resultClass);
  }

  @Override
  public <T> T merge(T entity) {
    throw new UnsupportedOperationException("Not implemented");
  }

  @Override
  public <T> T find(Class<T> entityClass, Object primaryKey,
          LockModeType lockMode) {
    throw new UnsupportedOperationException("Not implemented");
  }

  @Override
  public <T> T find(Class<T> entityClass, Object primaryKey,
          LockModeType lockMode, Map<String, Object> properties) {
    throw new UnsupportedOperationException("Not implemented");
  }

  @Override
  public <T> T getReference(Class<T> entityClass, Object primaryKey) {
    throw new UnsupportedOperationException("Not implemented");
  }

  @Override
  public void setFlushMode(FlushModeType flushMode) {
    throw new UnsupportedOperationException("Not implemented");
  }

  @Override
  public FlushModeType getFlushMode() {
    throw new UnsupportedOperationException("Not implemented");
  }

  @Override
  public void lock(Object entity, LockModeType lockMode) {
    throw new UnsupportedOperationException("Not implemented");
  }

  @Override
  public void lock(Object entity, LockModeType lockMode,
          Map<String, Object> properties) {
    throw new UnsupportedOperationException("Not implemented");
  }

  @Override
  public void refresh(Object entity) {
    throw new UnsupportedOperationException("Not implemented");
  }

  @Override
  public void refresh(Object entity, Map<String, Object> properties) {
    throw new UnsupportedOperationException("Not implemented");
  }

  @Override
  public void refresh(Object entity, LockModeType lockMode) {
    throw new UnsupportedOperationException("Not implemented");
  }

  @Override
  public void refresh(Object entity, LockModeType lockMode,
          Map<String, Object> properties) {
    throw new UnsupportedOperationException("Not implemented");
  }

  @Override
  public boolean contains(Object entity) {
    return persistenceContext.containsValue(entity);
  }

  @Override
  public LockModeType getLockMode(Object entity) {
    throw new UnsupportedOperationException("Not implemented");
  }

  @Override
  public void setProperty(String propertyName, Object value) {
    throw new UnsupportedOperationException("Not implemented");
  }

  @Override
  public Map<String, Object> getProperties() {
    throw new UnsupportedOperationException("Not implemented");
  }

  @Override
  public Query createQuery(String qlString) {
    throw new UnsupportedOperationException("Not implemented");
  }

  @Override
  public <T> TypedQuery<T> createQuery(CriteriaQuery<T> criteriaQuery) {
    throw new UnsupportedOperationException("Not implemented");
  }

  @Override
  public <T> TypedQuery<T> createQuery(String qlString, Class<T> resultClass) {
    throw new UnsupportedOperationException("Not implemented");
  }

  @Override
  public Query createNativeQuery(String sqlString) {
    throw new UnsupportedOperationException("Not implemented");
  }

  @Override
  public Query createNativeQuery(String sqlString, Class resultClass) {
    throw new UnsupportedOperationException("Not implemented");
  }

  @Override
  public Query createNativeQuery(String sqlString, String resultSetMapping) {
    throw new UnsupportedOperationException("Not implemented");
  }

  @Override
  public void joinTransaction() {
    throw new UnsupportedOperationException("Not implemented");
  }

  @Override
  public <T> T unwrap(Class<T> cls) {
    throw new UnsupportedOperationException("Not implemented");
  }

  @Override
  public Object getDelegate() {
    throw new UnsupportedOperationException("Not implemented");
  }

  @Override
  public void close() {
    throw new UnsupportedOperationException("Not implemented");
  }

  @Override
  public boolean isOpen() {
    throw new UnsupportedOperationException("Not implemented");
  }

  @Override
  public EntityTransaction getTransaction() {
    throw new UnsupportedOperationException("Not implemented");
  }

  @Override
  public EntityManagerFactory getEntityManagerFactory() {
    throw new UnsupportedOperationException("Not implemented");
  }

  @Override
  public CriteriaBuilder getCriteriaBuilder() {
    throw new UnsupportedOperationException("Not implemented");
  }
}
