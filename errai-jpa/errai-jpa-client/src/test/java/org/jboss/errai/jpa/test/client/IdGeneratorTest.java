package org.jboss.errai.jpa.test.client;


import java.math.BigInteger;

import javax.persistence.EntityManager;

import org.jboss.errai.ioc.client.Container;
import org.jboss.errai.ioc.client.container.IOCBeanManagerLifecycle;
import org.jboss.errai.jpa.rebind.ErraiEntityManagerGenerator;
import org.jboss.errai.jpa.test.entity.EntityWithBigIntegerId;
import org.jboss.errai.jpa.test.entity.EntityWithBoxedIntId;
import org.jboss.errai.jpa.test.entity.EntityWithBoxedLongId;
import org.jboss.errai.jpa.test.entity.EntityWithPrimitiveIntId;
import org.jboss.errai.jpa.test.entity.EntityWithPrimitiveLongId;

import com.google.gwt.junit.client.GWTTestCase;

/**
 * Tests the ability of Errai JPA to generate key values for all allowable
 * generated ID types.
 *
 * @author Jonathan Fuerth <jfuerth@gmail.com>
 */
public class IdGeneratorTest extends GWTTestCase {

  @Override
  public String getModuleName() {
    return "org.jboss.errai.jpa.test.JpaTest";
  }

  protected EntityManager getEntityManager() {
    JpaTestClient testClient = JpaTestClient.INSTANCE;
    assertNotNull(testClient);
    assertNotNull(testClient.entityManager);
    return JpaTestClient.INSTANCE.entityManager;
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
   * Tests that the entity manager was injected into the testing class. If this
   * test fails, the likely cause is that the
   * {@link ErraiEntityManagerGenerator} failed to output a compilable class. In
   * that case, try re-running this test with
   * {@code -Derrai.codegen.permissive=true} and
   * {@code -Derrai.codegen.printOut=true}. This should allow you to inspect the
   * generated source code and to see the Java compiler errors.
   */
  public void testEntityManagerInjection() throws Exception {
    getEntityManager(); // has its own assertions
  }

  public void testPrimitiveLong() throws Exception {
    EntityManager em = getEntityManager();

    EntityWithPrimitiveLongId entity = new EntityWithPrimitiveLongId();
    assertEquals(0, entity.getId());

    em.persist(entity);
    em.flush();

    assertTrue(entity.getId() > 0);
  }

  public void testBoxedLong() throws Exception {
    EntityManager em = getEntityManager();

    EntityWithBoxedLongId entity = new EntityWithBoxedLongId();
    assertNull(entity.getId());

    em.persist(entity);
    em.flush();

    assertNotNull(entity.getId());
    assertTrue(entity.getId() > 0);
  }

  public void testPrimitiveInt() throws Exception {
    EntityManager em = getEntityManager();

    EntityWithPrimitiveIntId entity = new EntityWithPrimitiveIntId();
    assertEquals(0, entity.getId());

    em.persist(entity);
    em.flush();

    assertTrue(entity.getId() > 0);
  }

  public void testBoxedInt() throws Exception {
    EntityManager em = getEntityManager();

    EntityWithBoxedIntId entity = new EntityWithBoxedIntId();
    assertNull(entity.getId());

    em.persist(entity);
    em.flush();

    assertNotNull(entity.getId());
    assertTrue(entity.getId() > 0);
  }

  public void testBigInteger() throws Exception {
    EntityManager em = getEntityManager();

    EntityWithBigIntegerId entity = new EntityWithBigIntegerId();
    assertNull(entity.getId());

    em.persist(entity);
    em.flush();

    assertNotNull(entity.getId());
    assertTrue(entity.getId().compareTo(BigInteger.ZERO) > 0);
  }

}
