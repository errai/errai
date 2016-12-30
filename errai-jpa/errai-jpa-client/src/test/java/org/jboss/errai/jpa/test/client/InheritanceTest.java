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

package org.jboss.errai.jpa.test.client;


import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;

import org.jboss.errai.ioc.client.Container;
import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.jpa.client.local.ErraiEntityManager;
import org.jboss.errai.jpa.test.client.res.JpaClientTestCase;
import org.jboss.errai.jpa.test.entity.inherit.ChildOfAbstractParentEntity;
import org.jboss.errai.jpa.test.entity.inherit.ChildOfConcreteParentEntity;
import org.jboss.errai.jpa.test.entity.inherit.GrandchildOfConcreteParentEntity;
import org.jboss.errai.jpa.test.entity.inherit.IdTestingEntity1;
import org.jboss.errai.jpa.test.entity.inherit.IdTestingEntity2;
import org.jboss.errai.jpa.test.entity.inherit.ParentAbstractEntity;
import org.jboss.errai.jpa.test.entity.inherit.ParentConcreteEntity;

/**
 * Tests the JPA EntityManager facilities provided by Errai JPA.
 * <p>
 * Note that there is a {@link HibernateJpaTest subclass of this test} that runs
 * all the same checks against Hibernate, as a sanity check that we're testing
 * for actual JPA-sanctioned and JPA-compatible behaviour.
 *
 * @author Jonathan Fuerth <jfuerth@gmail.com>
 */
public class InheritanceTest extends JpaClientTestCase {

  @Override
  public String getModuleName() {
    return "org.jboss.errai.jpa.test.JpaTest";
  }

  protected EntityManager getEntityManager() {
    final JpaTestClient testClient = JpaTestClient.INSTANCE;
    assertNotNull(testClient);
    final EntityManager em = testClient.entityManager;
    assertNotNull(em);
    ((ErraiEntityManager) em).removeAll();
    return em;
  }

  @Override
  protected void gwtSetUp() throws Exception {
    super.gwtSetUp();

    // We need to bootstrap the IoC container manually because GWTTestCase
    // doesn't call onModuleLoad() for us.
    new Container().bootstrapContainer();
  }

  @Override
  protected void gwtTearDown() throws Exception {
    Container.reset();
    IOC.reset();
  }

  public void testStoreAndRetrieveConcreteParent() throws Exception {
    final EntityManager em = getEntityManager();

    final ParentConcreteEntity pc = new ParentConcreteEntity();
    pc.setPrivateParentField(-4);
    pc.setPackagePrivateParentField(2);
    pc.setProtectedParentField(432344);
    pc.setPublicParentField(99887766);
    em.persist(pc);
    em.flush();

    em.clear();

    final ParentConcreteEntity found = em.find(ParentConcreteEntity.class, pc.getId());
    assertEquals(pc.toString(), found.toString());
  }

  public void testStoreAndRetrieveChildofConcreteParent() throws Exception {
    final EntityManager em = getEntityManager();

    final ChildOfConcreteParentEntity cc = new ChildOfConcreteParentEntity();
    cc.setPrivateParentField(-4);
    cc.setPackagePrivateParentField(2);
    cc.setProtectedParentField(432344);
    cc.setPublicParentField(99887766);
    cc.setChildField(101010);
    em.persist(cc);
    em.flush();

    em.clear();

    final ChildOfConcreteParentEntity found = em.find(ChildOfConcreteParentEntity.class, cc.getId());
    assertEquals(cc.toString(), found.toString());

    // ensure lookup by superclass also works
    final ParentConcreteEntity found2 = em.find(ParentConcreteEntity.class, cc.getId());
    assertSame(found, found2);
  }

  public void testStoreAndRetrieveChildofAbstractParent() throws Exception {
    final EntityManager em = getEntityManager();

    final ChildOfAbstractParentEntity ac = new ChildOfAbstractParentEntity();
    ac.setPrivateParentField(-4);
    ac.setPackagePrivateParentField(2);
    ac.setProtectedParentField(432344);
    ac.setPublicParentField(99887766);
    ac.setChildField(101010);
    em.persist(ac);
    em.flush();

    em.clear();

    final ChildOfAbstractParentEntity found = em.find(ChildOfAbstractParentEntity.class, ac.getId());
    assertEquals(ac.toString(), found.toString());

    // ensure lookup by superclass also works
    final ParentAbstractEntity found2 = em.find(ParentAbstractEntity.class, ac.getId());
    assertSame(found, found2);
  }

  public void testMetamodelOfConcreteChild() throws Exception {
    final EntityManager em = getEntityManager();
    final Metamodel mm = em.getMetamodel();

    final EntityType<ChildOfConcreteParentEntity> childEntityType = mm.entity(ChildOfConcreteParentEntity.class);
    final EntityType<ParentConcreteEntity> parentEntityType = mm.entity(ParentConcreteEntity.class);

    // test that inherited attributes report correct declaring type
    final Attribute<? super ChildOfConcreteParentEntity, ?> inheritedAttribute = childEntityType.getAttribute("privateParentField");
    assertEquals(parentEntityType, inheritedAttribute.getDeclaringType());

    // test that declared attributes report correct declaring type
    final Attribute<? super ChildOfConcreteParentEntity, ?> declaredAttribute = childEntityType.getAttribute("childField");
    assertEquals(childEntityType, declaredAttribute.getDeclaringType());
  }

  /**
   * Tests that a query for a parent type also returns entities from subtypes that match the criteria.
   */
  public void testPolymorphicQueryReturningSubtypesOfConcreteParent() throws Exception {
    final EntityManager em = getEntityManager();

    final ParentConcreteEntity pc = new ParentConcreteEntity();
    pc.setPrivateParentField(1);
    pc.setPackagePrivateParentField(1);
    pc.setProtectedParentField(1);
    pc.setPublicParentField(1);
    em.persist(pc);

    final ChildOfConcreteParentEntity cc = new ChildOfConcreteParentEntity();
    cc.setPrivateParentField(2);
    cc.setPackagePrivateParentField(2);
    cc.setProtectedParentField(2);
    cc.setPublicParentField(2);
    cc.setChildField(2);
    em.persist(cc);

    final GrandchildOfConcreteParentEntity gcc = new GrandchildOfConcreteParentEntity();
    gcc.setPrivateParentField(3);
    gcc.setPackagePrivateParentField(3);
    gcc.setProtectedParentField(3);
    gcc.setPublicParentField(3);
    gcc.setChildField(3);
    em.persist(gcc);

    final ChildOfConcreteParentEntity cc2 = new ChildOfConcreteParentEntity();
    cc2.setPrivateParentField(4);
    cc2.setPackagePrivateParentField(4);
    cc2.setProtectedParentField(4);
    cc2.setPublicParentField(4);
    cc2.setChildField(4);
    em.persist(cc2);

    em.flush();

    final TypedQuery<ParentConcreteEntity> query = em.createNamedQuery("parentConcreteEntity", ParentConcreteEntity.class);
    query.setParameter("protectedFieldAtLeast", 1);
    query.setParameter("protectedFieldAtMost", 3);
    final List<ParentConcreteEntity> resultList = query.getResultList();

    assertEquals(3, resultList.size());
    assertTrue(resultList.contains(pc));
    assertTrue(resultList.contains(cc));
    assertTrue(resultList.contains(gcc));

    // this one was out of range for the WHERE clause
    assertFalse(resultList.contains(cc2));
  }

  public void testPolymorphicQueryReturningOnlyConcreteChild() throws Exception {
    final EntityManager em = getEntityManager();

    final ParentConcreteEntity pc = new ParentConcreteEntity();
    pc.setPrivateParentField(1);
    pc.setPackagePrivateParentField(1);
    pc.setProtectedParentField(1);
    pc.setPublicParentField(1);
    em.persist(pc);

    final ChildOfConcreteParentEntity cc = new ChildOfConcreteParentEntity();
    cc.setPrivateParentField(2);
    cc.setPackagePrivateParentField(2);
    cc.setProtectedParentField(2);
    cc.setPublicParentField(2);
    cc.setChildField(2);
    em.persist(cc);

    em.flush();

    final TypedQuery<ParentConcreteEntity> query = em.createNamedQuery("parentConcreteEntity", ParentConcreteEntity.class);
    query.setParameter("protectedFieldAtLeast", 2);
    query.setParameter("protectedFieldAtMost", 2);
    final List<ParentConcreteEntity> resultList = query.getResultList();

    assertEquals(1, resultList.size());
    assertTrue(resultList.contains(cc));
  }

  public void testPolymorphicQueryForOnlyConcreteChild() throws Exception {
    final EntityManager em = getEntityManager();

    final ParentConcreteEntity pc = new ParentConcreteEntity();
    pc.setPrivateParentField(1);
    pc.setPackagePrivateParentField(1);
    pc.setProtectedParentField(1);
    pc.setPublicParentField(1);
    em.persist(pc);

    final ChildOfConcreteParentEntity cc = new ChildOfConcreteParentEntity();
    cc.setPrivateParentField(2);
    cc.setPackagePrivateParentField(2);
    cc.setProtectedParentField(2);
    cc.setPublicParentField(2);
    cc.setChildField(2);
    em.persist(cc);

    final ChildOfConcreteParentEntity cc2 = new ChildOfConcreteParentEntity();
    cc2.setPrivateParentField(3);
    cc2.setPackagePrivateParentField(3);
    cc2.setProtectedParentField(3);
    cc2.setPublicParentField(3);
    cc2.setChildField(3);
    em.persist(cc2);

    em.flush();

    final TypedQuery<ParentConcreteEntity> query = em.createNamedQuery("childOfParentConcreteEntity", ParentConcreteEntity.class);
    query.setParameter("protectedFieldAtLeast", 1);
    query.setParameter("protectedFieldAtMost", 2);
    final List<ParentConcreteEntity> resultList = query.getResultList();

    assertEquals(1, resultList.size());
    assertTrue(resultList.contains(cc));
  }

  public void testIdGenerationIsUniqueWithinInheritanceGroup() throws Exception {
    final EntityManager em = getEntityManager();

    // IdTestingEntity1 and IdTestingEntity2 share a common superclass entity. their ids must not overlap.
    final IdTestingEntity1 idte1 = new IdTestingEntity1();
    final IdTestingEntity2 idte2 = new IdTestingEntity2();

    em.persist(idte1);
    em.persist(idte2);
    em.flush();

    assertFalse(idte1.getId() == idte2.getId());
  }

}
