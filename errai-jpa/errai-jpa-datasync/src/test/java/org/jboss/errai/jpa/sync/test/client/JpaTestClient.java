package org.jboss.errai.jpa.sync.test.client;

import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.jboss.errai.ioc.client.api.EntryPoint;
import org.jboss.errai.ioc.client.api.TestOnly;

@TestOnly @EntryPoint
public class JpaTestClient {

  static JpaTestClient INSTANCE;

  @Inject EntityManager entityManager;

  public JpaTestClient() {
    fallbackToSessionStorage();
    if (INSTANCE != null) {
      System.out.println("WARN: overwriting JpaTestClient singleton reference from " + INSTANCE + " to " + this);
    }
    INSTANCE = this;
  }

  /**
   * HTMLUnit supports sessionStorage but not localStorage. They have the same
   * API, so we just alias localStorage to sessionStorage.
   */
  public static native void fallbackToSessionStorage() /*-{
    if ($wnd.localStorage === undefined) {
      $wnd.localStorage = $wnd.sessionStorage;
    }
  }-*/;
}
