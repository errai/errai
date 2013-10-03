package org.jboss.errai.jpa.test.client;


import javax.persistence.EntityManager;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;

import org.jboss.errai.ioc.client.Container;
import org.jboss.errai.ioc.client.container.IOCBeanManagerLifecycle;
import org.jboss.errai.jpa.test.entity.inherit.ChildOfConcreteParentEntity;
import org.jboss.errai.jpa.test.entity.inherit.ParentConcreteEntity;

import com.google.gwt.junit.client.GWTTestCase;

/**
 * Tests the JPA EntityManager facilities provided by Errai JPA.
 * <p>
 * Note that there is a {@link HibernateJpaTest subclass of this test} that runs
 * all the same checks against Hibernate, as a sanity check that we're testing
 * for actual JPA-sanctioned and JPA-compatible behaviour.
 *
 * @author Jonathan Fuerth <jfuerth@gmail.com>
 */
public class InheritanceTest extends GWTTestCase {

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

  public void testStoreAndRetrieveConcreteParent() throws Exception {
    EntityManager em = getEntityManager();

    ParentConcreteEntity pc = new ParentConcreteEntity();
    pc.setPrivateParentField(-4);
    pc.setPackagePrivateParentField(2);
    pc.setProtectedParentField(432344);
    pc.setPublicParentField(99887766);
    em.persist(pc);

    em.clear();

    ParentConcreteEntity found = em.find(ParentConcreteEntity.class, pc.getId());
    assertEquals(pc.toString(), found.toString());
  }

  public void testStoreAndRetrieveChildofConcreteParent() throws Exception {
    EntityManager em = getEntityManager();

    ChildOfConcreteParentEntity cc = new ChildOfConcreteParentEntity();
    cc.setPrivateParentField(-4);
    cc.setPackagePrivateParentField(2);
    cc.setProtectedParentField(432344);
    cc.setPublicParentField(99887766);
    cc.setChildField(101010);
    em.persist(cc);

    em.clear();

    ChildOfConcreteParentEntity found = em.find(ChildOfConcreteParentEntity.class, cc.getId());
    assertEquals(cc.toString(), found.toString());
  }

  public void testMetamodelOfConcreteChild() throws Exception {
    EntityManager em = getEntityManager();
    Metamodel mm = em.getMetamodel();

    EntityType<ChildOfConcreteParentEntity> childEntityType = mm.entity(ChildOfConcreteParentEntity.class);
    EntityType<ParentConcreteEntity> parentEntityType = mm.entity(ParentConcreteEntity.class);

    // test that inherited attributes report correct declaring type
    Attribute<? super ChildOfConcreteParentEntity, ?> inheritedAttribute = childEntityType.getAttribute("privateParentField");
    assertEquals(parentEntityType, inheritedAttribute.getDeclaringType());

    // test that declared attributes report correct declaring type
    Attribute<? super ChildOfConcreteParentEntity, ?> declaredAttribute = childEntityType.getAttribute("childField");
    assertEquals(childEntityType, declaredAttribute.getDeclaringType());
  }

  // TODO test polymorphic query (entities can appear in query results for their supertypes)
}
