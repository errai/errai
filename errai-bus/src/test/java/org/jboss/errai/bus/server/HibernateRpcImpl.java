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
