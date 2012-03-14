package org.jboss.errai.jpa.client.local;

import javax.persistence.EntityManager;

/**
 * The Errai specialization of the JPA 2.0 EntityManager interface. An
 * implementation of this interface, based on all JPA entities visible to the
 * GWT compiler, is generated when the end-user project is compiled.
 *
 * @author Jonathan Fuerth <jfuerth@gmail.com>
 */
public interface ErraiEntityManager extends EntityManager {

  // TODO deprecate all the methods we don't implement on the client side
}
