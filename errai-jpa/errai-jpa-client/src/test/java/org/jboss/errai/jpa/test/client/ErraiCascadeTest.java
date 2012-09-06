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
   * Creates a CascadeFrom object, fills in all its related CascadeTo
   * attributes, persists everything, and flushes the entity manager. Useful as
   * a first step in most tests of cascade behaviour.
   *
   * @param em
   *          The entity manager to persist with
   * @return a newly-created CascadeFrom, persistent and managed by em, with all
   *         related CascadeTo attributes likewise persisted and managed by em.
   */
  private static CascadeFrom createFullyPersistedObject(EntityManager em) {
    CascadeFrom from = new CascadeFrom();
    from.setAll(new CascadeTo());
    from.setDetach(new CascadeTo());
    from.setMerge(new CascadeTo());
    from.setNone(new CascadeTo());
    from.setPersist(new CascadeTo());
    from.setRefresh(new CascadeTo());
    from.setRemove(new CascadeTo());

    em.persist(from.getAll());
    em.persist(from.getDetach());
    em.persist(from.getMerge());
    em.persist(from.getNone());
    em.persist(from.getPersist());
    em.persist(from.getRefresh());
    em.persist(from.getRemove());
    em.persist(from);

    em.flush();
    assertTrue(em.contains(from));
    assertTrue(em.contains(from.getAll()));
    assertTrue(em.contains(from.getDetach()));
    assertTrue(em.contains(from.getMerge()));
    assertTrue(em.contains(from.getNone()));
    assertTrue(em.contains(from.getPersist()));
    assertTrue(em.contains(from.getRefresh()));
    assertTrue(em.contains(from.getRemove()));

    return from;
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

  public void testCascadeRemove() throws Exception {
    EntityManager em = getEntityManagerAndClearStorageBackend();

    CascadeFrom from = createFullyPersistedObject(em);

    em.remove(from);

    assertFalse(em.contains(from));
    assertFalse(em.contains(from.getAll()));
    assertTrue(em.contains(from.getDetach()));
    assertTrue(em.contains(from.getMerge()));
    assertTrue(em.contains(from.getNone()));
    assertTrue(em.contains(from.getPersist()));
    assertTrue(em.contains(from.getRefresh()));
    assertFalse(em.contains(from.getRemove()));
  }

  public void testCascadeDetach() throws Exception {
    EntityManager em = getEntityManagerAndClearStorageBackend();

    CascadeFrom from = createFullyPersistedObject(em);

    em.detach(from);

    assertFalse(em.contains(from));
    assertFalse(em.contains(from.getAll()));
    assertFalse(em.contains(from.getDetach()));
    assertTrue(em.contains(from.getMerge()));
    assertTrue(em.contains(from.getNone()));
    assertTrue(em.contains(from.getPersist()));
    assertTrue(em.contains(from.getRefresh()));
    assertTrue(em.contains(from.getRemove()));
  }

}
