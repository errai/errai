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

package org.jboss.errai.bus.server;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.jboss.errai.bus.client.tests.support.HibernateObject;
import org.jboss.errai.bus.client.tests.support.HibernateRpc;
import org.jboss.errai.bus.client.tests.support.OtherHibernateObject;
import org.jboss.errai.bus.server.annotations.Service;

@Service
public class HibernateRpcImpl implements HibernateRpc {
  private static EntityManagerFactory emf = createEntityManagerFactory();

  private static EntityManagerFactory createEntityManagerFactory() {
    Map<String, String> properties = new HashMap<String, String>();
    properties.put("hibernate.connection.driver_class", "org.h2.Driver");
    properties.put("hibernate.connection.url", "jdbc:h2:mem:temporary");
    properties.put("hibernate.connection.username", "sa");
    properties.put("hibernate.connection.password", "");
    properties.put("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
    properties.put("hibernate.hbm2ddl.auto", "update");
    properties.put("javax.persistence.validation.mode", "none");

    return Persistence.createEntityManagerFactory("HibernateRpcTest", properties);
  }

  @Override
  public HibernateObject getHibernateObject(Integer id) {
    EntityManager em = emf.createEntityManager();
    try {
      HibernateObject o = em.find(HibernateObject.class, id);
      o.getOther();
      return o;
    }
    finally {
      em.close();
    }
  }

  @Override
  public void addHibernateObject(HibernateObject entity) {
    EntityManager em = emf.createEntityManager();
    try {
      em.getTransaction().begin();
      em.persist(entity);
      em.getTransaction().commit();
    }
    finally {
      em.close();
    }
  }

  @Override
  public OtherHibernateObject getOther(Integer idOfParent) {
    /*
     * It is intentional that this entity manager is not closed in this method.
     * Since the object we are retrieving is actually a hibernate proxy, the
     * entity manager must remain open until the object is marshaled.
     */
    EntityManager em = emf.createEntityManager();
    OtherHibernateObject other = em.find(HibernateObject.class, idOfParent).getOther();

    return other;
  }

}
