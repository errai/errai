package org.jboss.errai.jpa.client.local;

import javax.enterprise.context.ApplicationScoped;

import org.jboss.errai.ioc.client.api.builtin.IOCProducer;

import com.google.gwt.core.client.GWT;

/**
 * Provides the Errai JPA Entity Manager to client-side code.
 *
 * @author Jonathan Fuerth <jfuerth@gmail.com>
 */
@ApplicationScoped
public class ErraiEntityManagerProducer {

  private ErraiEntityManager INSTANCE;

  @IOCProducer
  public ErraiEntityManager getEntityManager() {
    if (INSTANCE == null) {
      ErraiEntityManagerFactory factory = GWT.create(ErraiEntityManagerFactory.class);
      INSTANCE = factory.createEntityManager();
    }
    return INSTANCE;
  }
}
