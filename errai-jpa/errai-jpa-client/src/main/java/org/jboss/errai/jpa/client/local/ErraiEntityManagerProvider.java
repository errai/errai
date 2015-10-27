package org.jboss.errai.jpa.client.local;

import javax.inject.Provider;
import javax.inject.Singleton;
import javax.persistence.EntityManager;

import org.jboss.errai.ioc.client.api.IOCProvider;

import com.google.gwt.core.client.GWT;

/**
 * Provides the Errai JPA Entity Manager to client-side code.
 *
 * @author Jonathan Fuerth <jfuerth@gmail.com>
 */
@IOCProvider
@Singleton
public class ErraiEntityManagerProvider implements Provider<EntityManager> {

  private ErraiEntityManager INSTANCE;

  @Override
  public EntityManager get() {
    if (INSTANCE == null) {
      ErraiEntityManagerFactory factory = GWT.create(ErraiEntityManagerFactory.class);
      INSTANCE = factory.createEntityManager();
    }
    return INSTANCE;
  }
}
