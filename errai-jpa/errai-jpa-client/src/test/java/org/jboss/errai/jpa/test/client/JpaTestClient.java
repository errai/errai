package org.jboss.errai.jpa.test.client;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.jboss.errai.ioc.client.api.EntryPoint;
import org.jboss.errai.ioc.client.api.TestOnly;

@TestOnly @EntryPoint
public class JpaTestClient {

  static JpaTestClient INSTANCE;

  @Inject EntityManager entityManager;

  public JpaTestClient() {
    if (INSTANCE != null) {
      System.out.println("WARN: overwriting JpaTestClient singleton reference from " + INSTANCE + " to " + this);
    }
    INSTANCE = this;
  }

  @PostConstruct
  public void storeAlbums() {

  }
}
