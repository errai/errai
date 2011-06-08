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

import org.hibernate.Transaction;
import org.hibernate.classic.Session;
import org.junit.Test;

import java.util.HashSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Verify merging of entities
 *
 * @author: Heiko Braun <hbraun@redhat.com>
 * @date: Jun 12, 2010
 */
public class MergingTest extends CommonTestSetup {
  /**
   * Export a detched entity with lazy relations.
   * These turn into null when exported outside persistent context.
   * However upon merging they should not be deleted and survive
   * a merge-load cycle
   */
  @Test
  public void mergeEntity() {
    // create a clone

    Session session = sessionFactory.openSession();
    String userId = createTestUser(session);
    session.close();

    session = sessionFactory.openSession();
    User user = loadUser(session, userId);
    session.close();

    User exportedUser = (User) beanManager.clone(user);//mapper.map(user, User.class);
    assertNull(exportedUser.getOrders()); // lazy turns into null

    // update clone and persist

    session = sessionFactory.openSession();
    Transaction tx = session.beginTransaction();
    exportedUser.setName("Okieh");
    User mergedUser = (User) beanManager.merge(exportedUser);
    session.saveOrUpdate(mergedUser);   // TODO: Why explicit save?
    tx.commit();

    assertEquals(userId, exportedUser.getUserId());
    session.close();

    // verify updates

    session = sessionFactory.openSession();
    User user2 = loadUser(session, userId);
    assertEquals("Okieh", user2.getName());
    assertEquals("Relations that havn't been exported should survive merging", user2.getOrders().size(), 1);

  }

  /**
   * Modify a lazy, uninitialized relation within clone
   * and then merge it back in.
   */
  @Test
  public void mergeModifiedLazyRelation() {
    // create a clone

    Session session = sessionFactory.openSession();
    String userId = createTestUser(session);
    session.close();

    session = sessionFactory.openSession();
    User user = loadUser(session, userId);
    session.close();

    User exportedUser = (User) beanManager.clone(user);//mapper.map(user, User.class);
    assertNull(exportedUser.getOrders()); // lazy turns into null

    // update clone and persist

    session = sessionFactory.openSession();
    Transaction tx = session.beginTransaction();
    exportedUser.setName("Okieh");
    exportedUser.setOrders(new HashSet<Order>());
    exportedUser.getOrders().add(new Order());

    User mergedUser = (User) beanManager.merge(exportedUser);
    session.saveOrUpdate(mergedUser);
    tx.commit();

    assertEquals(userId, exportedUser.getUserId());
    session.close();

    // verify updates

    session = sessionFactory.openSession();
    User user2 = loadUser(session, userId);
    assertEquals("Okieh", user2.getName());
    assertEquals("Relations should be merged", 2, user2.getOrders().size());

  }

}
