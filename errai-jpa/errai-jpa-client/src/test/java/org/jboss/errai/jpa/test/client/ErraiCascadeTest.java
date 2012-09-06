package org.jboss.errai.jpa.test.client;


import javax.persistence.EntityManager;

import org.jboss.errai.ioc.client.Container;
import org.jboss.errai.jpa.client.local.ErraiEntityManager;
import org.jboss.errai.jpa.rebind.ErraiEntityManagerGenerator;
import org.jboss.errai.jpa.test.entity.CascadeFrom;
import org.jboss.errai.jpa.test.entity.CascadeTo;

import com.google.gwt.junit.client.GWTTestCase;

/**
 * Tests the cascade behaviour for entity state changes under Errai's JPA EntityManager.
 * <p>
 * Note that there is a {@link HibernateCascadeTest subclass of this test} that runs
 * all the same checks against Hibernate, as a sanity check that we're testing
 * for actual JPA-sanctioned and JPA-compatible behaviour.
 *
 * @author Jonathan Fuerth <jfuerth@gmail.com>
 */
public class ErraiCascadeTest extends GWTTestCase {

  @Override
  public String getModuleName() {
    return "org.jboss.errai.jpa.test.JpaTest";
  }

  protected EntityManager getEntityManagerAndClearStorageBackend() {
    JpaTestClient testClient = JpaTestClient.INSTANCE;
    assertNotNull(testClient);
    assertNotNull(testClient.entityManager);
    ((ErraiEntityManager) testClient.entityManager).removeAll();
    return testClient.entityManager;
  }

  @Override
  protected void gwtSetUp() throws Exception {
    super.gwtSetUp();

    // We need to bootstrap the IoC container manually because GWTTestCase
    // doesn't call onModuleLoad() for us.
    new Container().bootstrapContainer();
  }

  /**
   * Tests that the entity manager was injected into the testing class. If this
   * test fails, the likely cause is that the
   * {@link ErraiEntityManagerGenerator} failed to output a compilable class. In
   * that case, try re-running this test with
   * {@code -Derrai.codegen.permissive=true} and
   * {@code -Derrai.codegen.printOut=true}. This should allow you to inspect the
   * generated source code and to see the Java compiler errors.
   */
  public void testEntityManagerInjection() throws Exception {
    getEntityManagerAndClearStorageBackend(); // has its own assertions
  }

  public void testCascadePersist() throws Exception {
    EntityManager em = getEntityManagerAndClearStorageBackend();

    CascadeFrom from = new CascadeFrom();
    from.setAll(new CascadeTo());
    from.setPersist(new CascadeTo());
    em.persist(from);
    em.flush();
    assertTrue(em.contains(from));
    assertTrue(em.contains(from.getAll()));
    assertTrue(em.contains(from.getPersist()));
  }

  public void testCascadePersistFailsWithNonCascadedNewEntity() throws Exception {
    EntityManager em = getEntityManagerAndClearStorageBackend();

    CascadeFrom from = new CascadeFrom();
    from.setAll(new CascadeTo());
    from.setPersist(new CascadeTo());
    from.setNone(new CascadeTo()); // this should lead to an error

    try {
      em.persist(from);
      em.flush();
      fail("Expected IllegalStateException");
    }
    catch (IllegalStateException ex) {
      // check for name of offending relationship
      assertTrue("Exception message doesn't mention bad relationship: " + ex.getMessage(),
              ex.getMessage().contains("none"));
    }
  }

}
