/*
 * Copyright 2009 JBoss, a divison Red Hat, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.errai.test.persistence;

import org.hibernate.classic.Session;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Verify detaching of entities
 *
 * @author: Heiko Braun <hbraun@redhat.com>
 * @date: Jun 11, 2010
 */
public class DetachmentTest extends CommonTestSetup {
  /**
   * Exporting an entity within persitent context
   * shouldn't have any impact besides turning hibernate proxies
   * into their regular counterparts.
   */
  @Test
  public void exportWithinPerstenceContext() {
    Session session = sessionFactory.openSession();
    String userId = createTestUser(session);

    User user = loadUser(session, userId);
    User exportedUser = (User) beanManager.clone(user);

    assertEquals(user.getUserId(), exportedUser.getUserId());
    assertEquals(user.getOrders().size(), exportedUser.getOrders().size());  // lazy

    assertEquals(user.getOrders().getClass().getName(), "org.hibernate.collection.PersistentSet");
    assertEquals(exportedUser.getOrders().getClass().getName(), "java.util.HashSet");
  }

  /**
   * Exporting entities with lazy relations outside a persistent context
   * should turn hibernate proxies into null.
   */
  @Test
  public void exportOutsidePersistenceContext() {
    Session session = sessionFactory.openSession();
    String userId = createTestUser(session);
    session.close();

    // -----------------
    session = sessionFactory.openSession();
    User user = loadUser(session, userId);
    session.close();

    User exportedUser = (User) beanManager.clone(user);

    assertEquals(user.getUserId(), exportedUser.getUserId());
    assertNull("relation should be null", exportedUser.getOrders()); // not exported (null)   
  }
}
