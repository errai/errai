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

import com.sun.japex.JapexDriverBase;
import com.sun.japex.TestCase;
import org.hibernate.HibernateException;
import org.hibernate.Transaction;
import org.hibernate.classic.Session;

import java.util.Set;

/**
 * @author: Heiko Braun <hbraun@redhat.com>
 * @date: Jun 16, 2010
 */
public class PerformanceEntity extends JapexDriverBase {
  private CommonTestSetup testEnv;
  private User entity;

  @Override
  public void initializeDriver() {
    testEnv = new CommonTestSetup();

    Session session = testEnv.getSessionFactory().openSession();
    String userId = testEnv.createTestUser(session);

    Transaction tx = session.beginTransaction();

    try {
      entity = testEnv.loadUser(session, userId);
      Set<Order> orderSet = entity.getOrders();
      for (Order order : orderSet) {
        order.getItems();
      }

      tx.commit();
    } catch (HibernateException e) {
      tx.rollback();
    }
  }

  @Override
  public void run(TestCase testCase) {
    User dto = (User) testEnv.getBeanManager().clone(entity);
  }
}
