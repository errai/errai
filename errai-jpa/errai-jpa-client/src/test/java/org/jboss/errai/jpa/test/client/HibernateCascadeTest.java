package org.jboss.errai.jpa.test.client;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

/**
 * Provides a sanity check for {@link ErraiCascadeTest} by running all the same
 * tests that we run against Errai JPA against Hibernate.
 *
 * @author Jonathan Fuerth <jfuerth@gmail.com>
 */
public class HibernateCascadeTest extends ErraiCascadeTest {

  private EntityManager entityManager;

  /**
   * Returns null because this test case is not meant to run in the GWT client
   * environment.
   */
  @Override
  public String getModuleName() {
    return null;
  }

  @Override
  protected void gwtSetUp() throws Exception {
    // don't call super implementation.. it is client-side-specific

    Map<String, String> properties = new HashMap<String, String>();
    properties.put("hibernate.connection.driver_class", "org.h2.Driver");
    properties.put("hibernate.connection.url", "jdbc:h2:mem:temporary");
    properties.put("hibernate.connection.username", "sa");
    properties.put("hibernate.connection.password", "");
    properties.put("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
    properties.put("hibernate.hbm2ddl.auto", "update");
    properties.put("javax.persistence.validation.mode", "none");
    EntityManagerFactory emf = Persistence.createEntityManagerFactory("ErraiJpaClientTests", properties);
    entityManager = emf.createEntityManager();
    entityManager.getTransaction().begin();
  }

  @Override
  protected void gwtTearDown() throws Exception {
    if (entityManager.getTransaction().isActive()) {
      entityManager.getTransaction().rollback();
    }
  }

  @Override
  protected EntityManager getEntityManagerAndClearStorageBackend() {
    // the backend is already clear because we set up a new in-memory database for each test
    return entityManager;
  }

  // the actual test methods are inherited from the superclass

  /**
   * Disabled for Hibernate because there is no Bindable Proxy integration.
   */
  @Override
  public void testCascadeMergeBindableProxyIntoItsOwnTarget() throws Exception {
  }
}
