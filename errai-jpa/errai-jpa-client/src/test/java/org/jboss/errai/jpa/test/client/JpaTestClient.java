package org.jboss.errai.jpa.test.client;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.jboss.errai.ioc.client.api.EntryPoint;
import org.jboss.errai.ioc.client.api.TestOnly;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@TestOnly @EntryPoint
public class JpaTestClient {

  static JpaTestClient INSTANCE;

  @Inject EntityManager entityManager;
  
  private final Logger logger;

  public JpaTestClient() {
    logger = LoggerFactory.getLogger(JpaTestClient.class);
    fallbackToSessionStorage();
    if (INSTANCE != null) {
      logger.warn("overwriting JpaTestClient singleton reference from " + INSTANCE + " to " + this);
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

  @PostConstruct
  public void storeAlbums() {
    logger.debug("JpaTestClient postconstruct");
  }
}
