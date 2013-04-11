package org.jboss.errai.jpa.test.client;


import javax.persistence.EntityManager;

import org.jboss.errai.ioc.client.Container;
import org.jboss.errai.ioc.client.container.IOCBeanManagerLifecycle;
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

    new IOCBeanManagerLifecycle().resetBeanManager();

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

  // It's not 100% clear from the spec that this should fail in the same way
  // as persist, but this test is consistent with the behaviour of Hibernate.
  public void testCascadeMergeFailsWithNonCascadedNewEntity() throws Exception {
    EntityManager em = getEntityManagerAndClearStorageBackend();

    CascadeFrom from = new CascadeFrom();
    from.setAll(new CascadeTo());
    from.setMerge(new CascadeTo());
    from.setNone(new CascadeTo()); // this should lead to an error

    try {
      em.merge(from);
      em.flush();
      fail("Expected IllegalStateException");
    }
    catch (IllegalStateException ex) {
      // check for name of offending relationship
      assertTrue("Exception message doesn't mention bad relationship: " + ex.getMessage(),
              ex.getMessage().contains("none"));
    }
  }

  /**
   * Tests this rule from the JPA spec 3.2.7.1 Merging Detached Entity State:
   * <blockquote>
   * If X is a detached entity, the state of X is copied onto a pre-existing managed
   * entity instance X' of the same identity or a new managed copy X' of X is created.
   * </blockquote>
   */
  public void testCascadeMergeRule1WhenPreExistingManagedEntityIsInPersistenceContext() throws Exception {
    EntityManager em = getEntityManagerAndClearStorageBackend();

    CascadeFrom preExistingEntity = new CascadeFrom();
    em.persist(preExistingEntity);
    em.flush();

    assertTrue(em.contains(preExistingEntity));

    CascadeFrom x = new CascadeFrom();
    x.setId(preExistingEntity.getId());
    CascadeFrom xPrime = em.merge(x);
    em.flush();

    assertSame(preExistingEntity, xPrime);
    assertNotSame(x, xPrime);

    assertTrue(em.contains(xPrime));
    assertFalse(em.contains(x));
  }

  /**
   * Tests this rule from the JPA spec 3.2.7.1 Merging Detached Entity State:
   * <blockquote>
   * If X is a detached entity, the state of X is copied onto a pre-existing managed
   * entity instance X' of the same identity or a new managed copy X' of X is created.
   * </blockquote>
   */
  public void testCascadeMergeRule1WhenPreExistingManagedEntityIsNotInPersistenceContext() throws Exception {
    EntityManager em = getEntityManagerAndClearStorageBackend();

    CascadeFrom preExistingEntity = new CascadeFrom();
    em.persist(preExistingEntity);
    em.flush();
    em.clear();

    assertFalse(em.contains(preExistingEntity));

    CascadeFrom x = new CascadeFrom();
    x.setId(preExistingEntity.getId());
    CascadeFrom xPrime = em.merge(x);
    em.flush();

    assertNotSame(preExistingEntity, xPrime);
    assertNotSame(x, xPrime);

    assertTrue(em.contains(xPrime));
    assertFalse(em.contains(x));
    assertFalse(em.contains(preExistingEntity));
  }

  /**
   * Tests this rule from the JPA spec 3.2.7.1 Merging Detached Entity State:
   * <blockquote>
   * If X is a new entity instance, a new managed entity instance X' is created
   * and the state of X is copied into the new managed entity instance X'.
   * </blockquote>
   */
  public void testCascadeMergeRule2() throws Exception {
    EntityManager em = getEntityManagerAndClearStorageBackend();

    CascadeFrom x = new CascadeFrom();
    CascadeFrom xPrime = em.merge(x);
    em.flush();

    assertNotSame(x, xPrime);
    assertTrue(em.contains(xPrime));
    assertFalse(em.contains(x));
  }

  /**
   * Tests this rule from the JPA spec 3.2.7.1 Merging Detached Entity State:
   * <blockquote>
   * If X is a removed entity instance, an IllegalArgumentException will be
   * thrown by the merge operation (or the transaction commit will fail).
   * </blockquote>
   */
  public void testCascadeMergeRule3() throws Exception {
    EntityManager em = getEntityManagerAndClearStorageBackend();

    CascadeFrom x = new CascadeFrom();
    em.persist(x);
    em.flush();
    em.remove(x);

    try {
      em.merge(x);
      em.flush();
      fail("Expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      // expected
    }
  }

  /**
   * Tests this rule from the JPA spec 3.2.7.1 Merging Detached Entity State:
   * <blockquote>
   * If X is a managed entity, it is ignored by the merge operation, however,
   * the merge operation is cascaded to entities referenced by relationships
   * from X if these relationships have been annotated with the cascade
   * element value cascade=MERGE or cascade=ALL annotation.
   * </blockquote>
   */
  public void testCascadeMergeRule4() throws Exception {
    EntityManager em = getEntityManagerAndClearStorageBackend();

    CascadeFrom x = new CascadeFrom();
    em.persist(x);
    em.flush();

    x.setAll(new CascadeTo());
    x.setDetach(new CascadeTo());
    x.setMerge(new CascadeTo());
    x.setNone(new CascadeTo());
    x.setPersist(new CascadeTo());
    x.setRefresh(new CascadeTo());
    x.setRemove(new CascadeTo());

    // have to persist the ones that won't cascade automatically
    em.persist(x.getDetach());
    em.persist(x.getNone());
    em.persist(x.getRefresh());
    em.persist(x.getRemove());

    CascadeFrom xPrime = em.merge(x);
    em.flush();

    assertSame(x, xPrime);

    em.clear();
    CascadeFrom xFetched = em.find(CascadeFrom.class, x.getId());
    assertNotSame(x.getAll(), xFetched.getAll());
    assertNotSame(x.getDetach(), xFetched.getDetach());
    assertNotSame(x.getMerge(), xFetched.getMerge());
    assertNotSame(x.getNone(), xFetched.getNone());
    assertNotSame(x.getPersist(), xFetched.getPersist());
    assertNotSame(x.getRefresh(), xFetched.getRefresh());
    assertNotSame(x.getRemove(), xFetched.getRemove());
  }

  /**
   * Tests these rules from the JPA spec 3.2.7.1 Merging Detached Entity State:
   * <p>
   * Rule 5:
   * <blockquote>
   * For all entities Y referenced by relationships from X having the cascade
   * element value cascade=MERGE or cascade=ALL, Y is merged recursively as
   * Y'. For all such Y refer- enced by X, X' is set to reference Y'. (Note
   * that if X is managed then X is the same object as X'.)
   * </blockquote>
   *
   * And Rule 6:
   * <blockquote>
   * If X is an entity merged to X', with a reference to another entity Y,
   * where cascade=MERGE or cascade=ALL is not specified, then navigation of
   * the same association from X' yields a reference to a managed object Y'
   * with the same persistent identity as Y.
   * </blockquote>
   * <p>
   * Note that we're not entirely clear on what this means, or how it differs
   * from rule 5. We're going with "do what Hibernate does."
   */
  public void testCascadeMergeRules5And6() throws Exception {
    EntityManager em = getEntityManagerAndClearStorageBackend();

    CascadeFrom x = new CascadeFrom();

    x.setAll(new CascadeTo());
    x.setDetach(new CascadeTo());
    x.setMerge(new CascadeTo());
    x.setNone(new CascadeTo());
    x.setPersist(new CascadeTo());
    x.setRefresh(new CascadeTo());
    x.setRemove(new CascadeTo());

    // have to persist the ones that won't cascade automatically
    em.persist(x.getDetach());
    em.persist(x.getNone());
    em.persist(x.getRefresh());
    em.persist(x.getRemove());

    CascadeFrom xPrime = em.merge(x);
    em.flush();

    assertNotSame(x, xPrime);

    em.clear();
    CascadeFrom xFetched = em.find(CascadeFrom.class, xPrime.getId());
    assertNotSame(x.getAll(), xFetched.getAll());
    assertNotSame(x.getDetach(), xFetched.getDetach());
    assertNotSame(x.getMerge(), xFetched.getMerge());
    assertNotSame(x.getNone(), xFetched.getNone());
    assertNotSame(x.getPersist(), xFetched.getPersist());
    assertNotSame(x.getRefresh(), xFetched.getRefresh());
    assertNotSame(x.getRemove(), xFetched.getRemove());
  }

  public void testCascadedMergeCopiesEntityState() {
    EntityManager em = getEntityManagerAndClearStorageBackend();

    CascadeFrom x = new CascadeFrom();

    x.setAll(new CascadeTo());
    x.setMerge(new CascadeTo());
    x.setPersist(new CascadeTo());

    em.persist(x.getMerge());
    em.persist(x);
    em.flush();
    em.clear();

    x.getAll().setString("updated string");
    x.getMerge().setString("updated merge");
    x.getPersist().setString("updated persist");
    CascadeFrom xPrime = em.merge(x);
    em.flush();

    assertNotSame(x, xPrime);

    assertNotSame(x.getAll(), xPrime.getAll());
    assertEquals("updated string", xPrime.getAll().getString());

    assertNotSame(x.getMerge(), xPrime.getMerge());
    assertEquals("updated merge", xPrime.getMerge().getString());

    // this one should not have the new state because the cascade rule doesn't include merge
    assertNotSame(x.getPersist(), xPrime.getPersist());
    assertNull(xPrime.getPersist().getString());
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
