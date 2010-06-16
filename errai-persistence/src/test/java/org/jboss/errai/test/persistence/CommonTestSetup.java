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

import net.sf.gilead.core.PersistentBeanManager;
import net.sf.gilead.core.hibernate.HibernateUtil;
import net.sf.gilead.core.store.stateful.InMemoryProxyStore;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.classic.Session;
import org.jboss.errai.test.persistence.dto.ItemDTO;
import org.jboss.errai.test.persistence.dto.OrderDTO;
import org.jboss.errai.test.persistence.dto.UserDTO;

import java.util.*;

/**
 * @author: Heiko Braun <hbraun@redhat.com>
 * @date: Jun 11, 2010
 */
public class CommonTestSetup
{
  protected static SessionFactory sessionFactory;
  protected static PersistentBeanManager beanManager;

  public CommonTestSetup()
  {
    sessionFactory = new Configuration().getSessionFactory();
    beanManager = createBeanManager(sessionFactory);
  }

  protected PersistentBeanManager createBeanManager(SessionFactory sessionFactory)
  {
    // configure gilead
    HibernateUtil persistenceUtil = new HibernateUtil();
    persistenceUtil.setSessionFactory(sessionFactory);

    InMemoryProxyStore proxyStore = new InMemoryProxyStore();
    proxyStore.setPersistenceUtil(persistenceUtil);

    PersistentBeanManager beanManager = new PersistentBeanManager();
    beanManager.setPersistenceUtil(persistenceUtil);
    beanManager.setProxyStore(proxyStore);

    return beanManager;
  }

  protected User loadUser(Session session, String userId)
  {
    Query q1 = session.createQuery("from User u where u.userId=:id");
    q1.setString("id", userId);
    User user = (User)q1.uniqueResult();
    return user;
  }

  protected String createTestUser(Session session)
  {
    // cleanup first
    cleanTables(session);

    User user = createUserEntity();

    // read-write test
    Transaction tx = session.beginTransaction();
    try
    {
      session.save(user);
      tx.commit();
    }
    catch (HibernateException e)
    {
      tx.rollback();
      throw new RuntimeException("Faile to save User", e);
    }

    return user.getUserId();
  }

  public User createUserEntity()
  {
    User user = new User();
    user.setName("Heiko");

    Set<Order> orders = new HashSet<Order>();
    user.setOrders(orders);

    List<Item> items = new ArrayList<Item>();
    items.add(new Item("TV", 12.99));
    items.add(new Item("Radio", 15.99));
    items.add(new Item("Laptop", 1.99));

    Order order = new Order();
    order.setItems(items);
    orders.add(order);
    return user;
  }

  public UserDTO createUserDTO()
   {
     UserDTO user = new UserDTO();
     user.setUserId(UUID.randomUUID().toString());
     user.setName("Heiko");

     Set<OrderDTO> orders = new HashSet<OrderDTO>();
     user.setOrders(orders);

     List<ItemDTO> items = new ArrayList<ItemDTO>();
     items.add(new ItemDTO("TV", 12.99));
     items.add(new ItemDTO("Radio", 15.99));
     items.add(new ItemDTO("Laptop", 1.99));

     OrderDTO order = new OrderDTO();
     order.setOrderNum(UUID.randomUUID().toString());
     order.setItems(items);
     orders.add(order);
     return user;
   }


  private void cleanTables(Session session)
  {
    Transaction tx = session.beginTransaction();
    try
    {
      Query query = session.createQuery("delete from Item");
      query.executeUpdate();

      query = session.createQuery("delete from Order");
      query.executeUpdate();

      query = session.createQuery("delete from User");
      query.executeUpdate();
      
      tx.commit();
    }
    catch (HibernateException e)
    {
      tx.rollback();
      throw new RuntimeException("Failed to clean user table", e);
    }
  }

  public static SessionFactory getSessionFactory()
  {
    return sessionFactory;
  }

  public static PersistentBeanManager getBeanManager()
  {
    return beanManager;
  }
}
