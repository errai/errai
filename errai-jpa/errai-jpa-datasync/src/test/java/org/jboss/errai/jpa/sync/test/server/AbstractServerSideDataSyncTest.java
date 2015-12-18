package org.jboss.errai.jpa.sync.test.server;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.junit.After;
import org.junit.Before;

public class AbstractServerSideDataSyncTest {

  protected EntityManager em;

  @Before
  public void setup() {
    Map<String, String> properties = new HashMap<String, String>();
    properties.put("hibernate.connection.driver_class", "org.h2.Driver");
    properties.put("hibernate.connection.url", "jdbc:h2:mem:temporary");
    properties.put("hibernate.connection.username", "sa");
    properties.put("hibernate.connection.password", "");
    properties.put("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
    properties.put("hibernate.hbm2ddl.auto", "update");
    properties.put("javax.persistence.validation.mode", "none");
    EntityManagerFactory emf = Persistence.createEntityManagerFactory("ErraiDataSyncTests", properties);
    em = emf.createEntityManager();
    em.getTransaction().begin();
  }

  @After
  public void tearDown() {
    if (em.getTransaction().isActive()) {
      em.getTransaction().rollback();
    }
  }

}
