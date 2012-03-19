package org.jboss.errai.jpa.test.client;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.jboss.errai.ioc.client.api.EntryPoint;

// TODO annotate with @TestOnly after merging in the fix from master branch
@EntryPoint
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
    System.out.println("JpaTestClient postconstruct");
  }
}
