package org.jboss.errai.jpa.client.local;

import javax.persistence.EntityManager;
import javax.persistence.metamodel.Metamodel;

/**
 * The Errai specialization of the JPA 2.0 EntityManager interface. An
 * implementation of this interface, based on all JPA entities visible to the
 * GWT compiler, is generated when the end-user project is compiled.
 *
 * @author Jonathan Fuerth <jfuerth@gmail.com>
 */
public abstract class ErraiEntityManager implements EntityManager {

  final Metamodel metamodel = new ErraiMetamodel();

  @Override
  public Metamodel getMetamodel() {
    return metamodel;
  }
}
