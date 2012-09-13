package org.jboss.errai.jpa.test.client;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.jboss.errai.jpa.test.entity.Album;

/**
 * Provides a sanity check for {@link ErraiJpaTest} by running all the same
 * tests that we run against Errai JPA against Hibernate.
 *
 * @author Jonathan Fuerth <jfuerth@gmail.com>
 */
public class HibernateJpaTest extends ErraiJpaTest {

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

    Album.CALLBACK_LOG.clear();
  }

  @Override
  protected void gwtTearDown() throws Exception {
    if (entityManager.getTransaction().isActive()) {
      entityManager.getTransaction().rollback();
    }
  }

  @Override
  protected EntityManager getEntityManager() {
    return entityManager;
  }

  // the actual test methods are inherited from the superclass

  /**
   * Hibernate (at least on top of HSQLDB) damages BigDecimal (adds extra
   * precision), java.util.Date (returns a java.sql.Date), and
   * java.sql.Timestamp (truncates nanosecond precision). So we skip this test
   * in Hibernate mode.
   */
  @Override
  public void testStoreAndFetchOneWithEverything() throws Exception {
    // skip
  }

  /**
   * Hibernate doesn't know how to unwrap Errai's WrappedPortables, so we have
   * to skip this test.
   */
  @Override
  public void testPersistProxiedEntity() {
    // skip
  }

  /**
   * Hibernate doesn't know how to unwrap Errai's WrappedPortables, so we have
   * to skip this test.
   */
  @Override
  public void testUpdateDataBinderProxiedEntity() {
    // skip
  }
}
