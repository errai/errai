package org.jboss.errai.jpa.client.local;

import javax.persistence.EntityManager;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;

/**
 * The Errai specialization of the JPA 2.0 EntityManager interface. An
 * implementation of this interface, based on all JPA entities visible to the
 * GWT compiler, is generated when the end-user project is compiled.
 *
 * @author Jonathan Fuerth <jfuerth@gmail.com>
 */
public abstract class ErraiEntityManager implements EntityManager {

  final ErraiMetamodel metamodel = new ErraiMetamodel();

  protected ErraiEntityManager() {
    populateMetamodel();
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

  @Override
  public Metamodel getMetamodel() {
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
    EntityType<?> entityType = metamodel.entity(entity.getClass());
  }
}
