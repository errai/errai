/*
 * Copyright (C) 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.jpa.client.local;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.inject.Alternative;
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

import org.jboss.errai.common.client.api.Assert;
import org.jboss.errai.common.client.api.WrappedPortable;
import org.jboss.errai.databinding.client.BindableProxy;
import org.jboss.errai.jpa.client.local.backend.StorageBackend;
import org.jboss.errai.jpa.client.local.backend.StorageBackendFactory;
import org.jboss.errai.marshalling.client.api.MarshallerFramework;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Errai implementation and specialization of the JPA 2.0 EntityManager interface.
 *
 * @author Jonathan Fuerth <jfuerth@gmail.com>
 */
@Alternative
public class ErraiEntityManager implements EntityManager {

  // magic incantation. ooga booga!
  static {
    // ensure that the marshalling framework has been initialized
    MarshallerFramework.initializeDefaultSessionProvider();
  }

  /**
   * The metamodel. Gets populated on first call to {@link #getMetamodel()}.
   */
  final ErraiMetamodel metamodel;

  /**
   * Container for all the live objects managed by this entity manager.
   */
  final PersistenceContext persistenceContext;

  /**
   * All removed instances known to this entity manager.
   */
  final Map<Key<?, ?>, Object> removedEntities = new HashMap<Key<?, ?>, Object>();

  /**
   * All of the entities that are partly constructed but are still getting their
   * references connected up. This is required in order to prevent infinite
   * recursion when demarshalling cyclic object graphs.
   */
  private Map<Key<Object, Object>, Object> partiallyConstructedEntities = new HashMap<Key<Object, Object>, Object>();

  /**
   * The actual storage backend.
   */
  private final StorageBackend backend;

  /**
   * All the named queries. Populated by a generated method in the
   * GeneratedErraiEntityManager subclass.
   */
  final Map<String, TypedQueryFactory> namedQueries;

  /**
   * The logging interface.
   */
  private final Logger logger;

  /**
   * Constructor for building custom-purpose EntityManager instances. For common
   * usecases, simply use {@code @Inject EntityManager em} and let the
   * {@link ErraiEntityManagerProducer} handle the prerequisites for you.
   */
  public ErraiEntityManager(
          ErraiMetamodel metamodel,
          Map<String, TypedQueryFactory> namedQueries,
          StorageBackendFactory storageBackendFactory) {
    this.metamodel = Assert.notNull(metamodel);
    this.namedQueries = Assert.notNull(namedQueries);
    this.persistenceContext = new PersistenceContext(metamodel);
    this.logger = LoggerFactory.getLogger(ErraiEntityManager.class);

    // Caution: we're handing out a reference to this partially constructed instance!
    this.backend = storageBackendFactory.createInstanceFor(this);
  }

  /**
   * Creates an EntityManager that knows about all the same managed types and
   * named queries as the given entity manager, but works from a different
   * storage backend. When combined with the namespacing support of a storage
   * backend, this allows you to work with several independent entity managers
   * at the same time.
   *
   * @param delegateEntityManager
   * @param namespacedStorageBackend
   */
  public ErraiEntityManager(ErraiEntityManager delegateEntityManager, StorageBackendFactory namespacedStorageBackend) {
    this(delegateEntityManager.getMetamodel(), delegateEntityManager.namedQueries, namespacedStorageBackend);
  }

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
    T nextId = attr.getValueGenerator().next(this);
    attr.set(entityInstance, nextId);
    return nextId;
  }

  /**
   * As they say in television, "this is where the magic happens." This method
   * attempts to resolve the given object as an entity and apply the operation
   * to the entity, taking into account its existing state and performing the
   * required side effects during the state transition.
   */
  private <X> X applyCascadingOperation(X entity, CascadeType newState) {
    ErraiIdentifiableType<X> entityType = getMetamodel().entity(getNarrowedClass(entity));

    final Key<X, ?> key = keyFor(entityType, entity);
    final EntityState oldState = getState(key, entity);

    logger.trace("+++ Performing " + newState + " operation on " + oldState + " entity: " + entity);
    X entityToReturn = entity;

    switch (newState) {
    case PERSIST:
      switch (oldState) {
      case REMOVED:
        removedEntities.remove(key);
        // FALLTHROUGH
      case NEW:
        entityType.deliverPrePersist(entity);
        persistenceContext.put(key, entity);
        backend.put(key, entity);
        entityType.deliverPostPersist(entity);
        break;
      case MANAGED:
        // no-op, but cascade to relatives
        break;
      case DETACHED:
        throw new EntityExistsException();
      }
      break;
    case MERGE:
      switch (oldState) {
      case NEW:
      case DETACHED:
        boolean sendUpdateEvent = true; // if false, send persist event
        X mergeTarget = find(key, Collections.<String,Object>emptyMap());
        if (mergeTarget == null) {
          sendUpdateEvent = false;
          mergeTarget = entityType.newInstance();
        }
        entityType.mergeState(this, mergeTarget, entity);
        entityToReturn = mergeTarget;

        if (sendUpdateEvent) {
          entityType.deliverPreUpdate(mergeTarget);
        }
        else {
          entityType.deliverPrePersist(mergeTarget);
        }

        persistenceContext.put(key, mergeTarget);
        backend.put(key, mergeTarget);

        if (sendUpdateEvent) {
          entityType.deliverPostUpdate(mergeTarget);
        }
        else {
          entityType.deliverPostPersist(mergeTarget);
        }
        break;
      case MANAGED:
        // no-op, but cascade to relatives
        break;
      case REMOVED:
        throw new IllegalArgumentException("Cannot merge removed entity " + entity);
      }
      break;
    case DETACH:
      switch (oldState) {
      case NEW:
      case DETACHED:
        // ignore
        break;
      case MANAGED:
        persistenceContext.remove(key);
        break;
      case REMOVED:
        removedEntities.remove(key);
        break;
      }
      break;
    case REMOVE:
      switch (oldState) {
      case NEW:
      case MANAGED:
        entityType.deliverPreRemove(entity);
        persistenceContext.remove(key);
        removedEntities.put(key, entity);
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
    default:
      throw new IllegalArgumentException("Operation not implemented yet: " + newState);
    }

    // Tell the BindableProxy that we changed the entity
    // (we haven't _necessarily_ changed anything.. if this becomes a performance problem,
    // we can set a flag in the above state change logic make this call depend on that flag)
    if (entityToReturn instanceof BindableProxy) {
      ((BindableProxy<?>) entityToReturn).updateWidgets();
    }

    // now cascade the operation
    for (SingularAttribute<? super X, ?> a : entityType.getSingularAttributes()) {
      ErraiSingularAttribute<? super X, ?> attrib = (ErraiSingularAttribute<? super X, ?>) a;
      cascadeStateChange(attrib, entityToReturn, entity, newState);
    }
    for (PluralAttribute<? super X, ?, ?> a : entityType.getPluralAttributes()) {
      ErraiPluralAttribute<? super X, ?, ?> attrib = (ErraiPluralAttribute<? super X, ?, ?>) a;
      cascadeStateChange(attrib, entityToReturn, entity, newState);
    }

    return entityToReturn;
  }

  /**
   * Creates the key that describes the given entity, <b>generating and setting
   * it if it is presently unset and the given entity type's ID is configured to
   * be generated on demand</b>. This version of the {@code keyFor()} method
   * assumes the given object's entity type can be obtained by calling {@code
   * entity.getClass()}. If you already have a specific entity type in mind, use
   * the {@link #keyFor(ErraiIdentifiableType, Object)} version of the method.
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
    ErraiIdentifiableType<X> entityType = getMetamodel().entity(getNarrowedClass(entity));
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
  public <X> Key<X, ?> keyFor(ErraiIdentifiableType<X> entityType, X entity) {
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
   * @param instance
   *          The entity instance whose state to check
   * @return The current state of the given entity according to this entity
   *         manager.
   */
  private <T> EntityState getState(Key<T, ?> key, T instance) {

    // unwrap the given instance to find its true identity
    Object o = instance;
    while (o instanceof WrappedPortable) {
      o = ((WrappedPortable) instance).unwrap();
    }

    // unwrap the retrieved instance (which could be null) to find its true identity
    Object inPersistenceContext = persistenceContext.get(key);
    while (inPersistenceContext instanceof WrappedPortable) {
      inPersistenceContext = ((WrappedPortable) inPersistenceContext).unwrap();
    }

    final EntityState oldState;
    if (inPersistenceContext == o) {
      oldState = EntityState.MANAGED;
    }
    else if (inPersistenceContext != null) {
      // we already have a different instance of this type of entity with the same ID
      oldState = EntityState.DETACHED;
    }
    else if (removedEntities.containsKey(key)) {
      oldState = EntityState.REMOVED;
    }
    else if (backend.contains(key)) {
      oldState = EntityState.DETACHED;
    }
    else {
      oldState = EntityState.NEW;
    }
    return oldState;
  }

  /**
   * Subroutine of {@link #applyCascadingOperation(Object, CascadeType)}. Cascades the
   * given change of state onto all of the related entities whose cascade rules
   * are appropriate to the new state. It is assumed that the cascading operation has
   * already been applied to the owning entity.
   *
   * @param <X> the type of the owning entity we are cascading from
   * @param <R> the type of the related entity we are cascading to
   */
  private <X, R> void cascadeStateChange(ErraiAttribute<X, R> cascadeAcross, X targetEntity, X sourceEntity, CascadeType cascadeType) {
    if (!cascadeAcross.isAssociation()) return;

    if (cascadeType == CascadeType.REFRESH) {
      throw new IllegalArgumentException("Refresh not yet supported");
    }

    R sourceRelatedEntity = cascadeAcross.get(sourceEntity);
    logger.trace("*** Cascade " + cascadeType + " across " + cascadeAcross.getName() + " to " + sourceRelatedEntity + "?");

    if (sourceRelatedEntity == null) {
      logger.trace("    No (because it's null)");
    }
    else if (cascadeAcross.cascades(cascadeType)) {
      logger.trace("    Yes");
      if (cascadeAcross.isCollection()) {
        R collectionOfMergeTargets = ((ErraiPluralAttribute<X, R, ?>) cascadeAcross).createEmptyCollection();
        for (Object element : (Iterable<?>) sourceRelatedEntity) {
          ((Collection) collectionOfMergeTargets).add(applyCascadingOperation(element, cascadeType));
        }

        if (cascadeType == CascadeType.MERGE) {
          cascadeAcross.set(targetEntity, collectionOfMergeTargets);
        }
      }
      else {
        R resolvedTarget = applyCascadingOperation(sourceRelatedEntity, cascadeType);

        // check if we need to reference the newly merged thing (only matters when cascadeType == MERGE)
        R originalTargetRelatedEntity = cascadeAcross.get(targetEntity);
        if (resolvedTarget != originalTargetRelatedEntity) {
          cascadeAcross.set(targetEntity, resolvedTarget);
        }
      }
    }
    else {
      logger.trace("    No");
      R resolvedTargetRelatedEntity = cascadeAcross.get(targetEntity);
      boolean relatedEntitiesAreManaged = true;
      if (cascadeAcross.isCollection()) {
        Collection<?> children = (Collection<?>) resolvedTargetRelatedEntity;
        for (Object child : children) {
          relatedEntitiesAreManaged &= contains(child);
        }
      }
      else {
        relatedEntitiesAreManaged = contains(resolvedTargetRelatedEntity);
      }
      if ((cascadeType == CascadeType.PERSIST || cascadeType == CascadeType.MERGE) && !relatedEntitiesAreManaged) {
        throw new IllegalStateException(
                "Entity " + targetEntity + " references an unsaved entity via relationship attribute [" +
                cascadeAcross.getName() + "]. Save related attribute before flushing or change" +
                " cascade rule to include " + cascadeType);
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
    ErraiIdentifiableType<X> entityType = getMetamodel().entity(getNarrowedClass(entity));
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
  public <X> List<X> findAll(ErraiIdentifiableType<X> type, EntityJsonMatcher matcher) {
    return backend.getAll(type, matcher);
  }

  /**
   * Tests if this entity manager's storage backend contains an entity that
   * could conflict with the given key. This method is free of side effects: it
   * will not affect the contents of the persistence context, and it will not
   * affect the persistence state of any entity (hence it will not deliver any
   * events to JPA lifecycle listeners).
   *
   * @param key
   *          The key to test for in backend storage. Not null.
   * @return true if and only if this entity manager's storage backend contains
   *         an entity with the given key.
   */
  public boolean isKeyInUse(Key<?, ?> key) {

    // search up the supertype chain for the most generic entity type reachable from the type given in the key
    ErraiManagedType<?> superManagedType = key.getEntityType();
    Class<?> javaType = key.getEntityType().getJavaType().getSuperclass();
    while (javaType != null) {
      ErraiManagedType<?> mt = metamodel.entity(javaType.getName(), false);
      if (mt != null) {
        superManagedType = mt;
      }
      javaType = javaType.getSuperclass();
    }
    Key<?, ?> mostGenericKey = new Key<Object, Object>((ErraiManagedType<Object>) superManagedType, key.getId());
    return backend.contains(mostGenericKey);
  }

  // -------------- Actual JPA API below this line -------------------

  @Override
  public ErraiMetamodel getMetamodel() {
    if (!metamodel.isFrozen()) {
      throw new RuntimeException("The metamodel isn't frozen!");
    }
    return metamodel;
  }

  @Override
  public void persist(Object entity) {
    applyCascadingOperation(entity, CascadeType.PERSIST);
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
    applyCascadingOperation(entity, CascadeType.DETACH);
  }

  @Override
  public void clear() {
    removedEntities.clear();
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
      if (entity != null) {
        persistenceContext.put(key, entity);
        ((ErraiIdentifiableType<X>) key.getEntityType()).deliverPostLoad(entity);
      }
    }
    return entity;
  }

  @Override
  public void remove(Object entity) {
    applyCascadingOperation(entity, CascadeType.REMOVE);
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
    TypedQueryFactory factory = namedQueries.get(name);
    if (factory == null) throw new IllegalArgumentException("No named query \"" + name + "\"");
    return factory.createIfCompatible(resultClass, this);
  }

  @Override
  public <T> T merge(T entity) {
    return applyCascadingOperation(entity, CascadeType.MERGE);
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
    Object found = persistenceContext.get(keyFor(entity));
    return found == entity;
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
