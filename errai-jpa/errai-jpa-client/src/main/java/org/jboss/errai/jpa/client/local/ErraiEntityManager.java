package org.jboss.errai.jpa.client.local;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.metamodel.SingularAttribute;

import org.jboss.errai.jpa.client.local.backend.StorageBackend;
import org.jboss.errai.jpa.client.local.backend.WebStorageBackend;
import org.jboss.errai.marshalling.client.api.MarshallerFramework;

/**
 * The Errai specialization of the JPA 2.0 EntityManager interface. An
 * implementation of this interface, based on all JPA entities visible to the
 * GWT compiler, is generated when the end-user project is compiled.
 *
 * @author Jonathan Fuerth <jfuerth@gmail.com>
 */
public abstract class ErraiEntityManager implements EntityManager {

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
   * All of the objects that need to be examined when {@link #flush()} is called.
   */
  final List<Object> persistenceContext = new ArrayList<Object>();

  /**
   * The actual storage backend.
   */
  private final StorageBackend backend = new WebStorageBackend();

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
    return (Class<T>) object.getClass();
  }

  private <T> void persistImpl(T entity) {
    ErraiEntityType<T> entityType = getMetamodel().entity(getNarrowedClass(entity));
    persistenceContext.add(entity);

    ErraiSingularAttribute<? super T,?> idAttr;
    switch (entityType.getIdType().getPersistenceType()) {
    case BASIC:
      idAttr = (ErraiSingularAttribute<? super T, ?>) entityType.getId(entityType.getIdType().getJavaType());
      break;
    default:
      throw new RuntimeException(entityType.getIdType().getPersistenceType() + " ids are not yet supported");
    }

    Object id = idAttr.get(entity);
    if (id == null) {
      id = generateAndSetLocalId(entity, idAttr);
      // TODO track this generated ID for later reconciliation with the server
    }

    backend.put(entityType, id, entity);
  }


  // XXX these would be better held by the SingularAttribute instances themselves
  private final Map<SingularAttribute<?, ?>, Iterator<?>> localIdGenerators = new HashMap<SingularAttribute<?, ?>, Iterator<?>>();


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
  public <X, T> T generateAndSetLocalId(X entityInstance, ErraiSingularAttribute<X, T> attr) {
    Iterator<T> idGenerator = (Iterator<T>) localIdGenerators.get(attr);
    if (idGenerator == null) {
      if (attr.getJavaType() == Long.class) {
        // XXX move this into the attribute class so it can always be the right type of sequence generator
        idGenerator = (Iterator<T>) new LongIdGenerator(this, (ErraiSingularAttribute<?, Long>) attr);
        localIdGenerators.put(attr, idGenerator);
      } else {
        throw new UnsupportedOperationException("Can't generate ID of type " + attr.getJavaType());
      }
    }

    T nextId = idGenerator.next();
    attr.set(entityInstance, nextId);
    return nextId;
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
    persistImpl(entity);
  }

  @Override
  public void flush() {
    for (Object entity : persistenceContext) {
      System.out.println("Flushing " + entity);
    }
  }

  @Override
  public void detach(Object entity) {
    persistenceContext.remove(entity);
  }

  @Override
  public <T> T find(Class<T> entityClass, Object primaryKey) {
    ErraiEntityType<T> entityType = getMetamodel().entity(entityClass);
    T entity = backend.get(entityType, primaryKey);
    return entity;
  }

  @Override
  public <T> T find(Class<T> entityClass, Object primaryKey, Map<String, Object> properties) {
    return find(entityClass, primaryKey);
  }
}
