/*
 * Copyright (C) 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

  public static JpaTestClient INSTANCE;

  @Inject public EntityManager entityManager;
  
  private final Logger logger;

  public JpaTestClient() {
    logger = LoggerFactory.getLogger(JpaTestClient.class);
    fallbackToSessionStorage();
    if (INSTANCE != null) {
      logger.debug("overwriting JpaTestClient singleton reference from " + INSTANCE + " to " + this);
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
