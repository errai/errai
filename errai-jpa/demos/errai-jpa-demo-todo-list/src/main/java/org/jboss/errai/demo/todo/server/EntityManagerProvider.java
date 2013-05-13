package org.jboss.errai.demo.todo.server;

import javax.ejb.Stateless;
import javax.enterprise.inject.Produces;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * A simple EJB that makes the server-side EntityManager available as a CDI bean.
 *
 * @author Jonathan Fuerth <jfuerth@redhat.com>
 */
@Stateless
public class EntityManagerProvider {

  @Produces @PersistenceContext
  private static EntityManager em;

}
