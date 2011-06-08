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

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Transaction;
import org.hibernate.classic.Session;
import org.junit.Test;

import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Verify the hibernate configuration
 *
 * @author: Heiko Braun <hbraun@redhat.com>
 * @date: Jun 11, 2010
 */
public class PersistenceTest extends CommonTestSetup {

  @Test
  public void testReadWriteEntity() {
    Session session = sessionFactory.openSession();
    String userId = createTestUser(session);

    Transaction tx = session.beginTransaction();

    try {
      User user = loadUser(session, userId);

      Query q2 = session.createQuery("from User");
      List<User> users = q2.list();
      assertEquals(1, users.size());
      assertTrue(user.getOrders().size() == 1);
    } catch (HibernateException e) {
      tx.rollback();
    }

  }

  @Test(expected = org.hibernate.LazyInitializationException.class)
  public void testLazyWithoutPersistenceContext() {
    Session session = sessionFactory.openSession();
    String userId = createTestUser(session);
    session.close();

    session = sessionFactory.openSession();
    User user = loadUser(session, userId);
    session.close(); // enforces LazyInitializationException

    Set<Order> orders = user.getOrders();
    assertEquals(orders.size(), 1);
  }

}
