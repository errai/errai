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

package org.jboss.tests.errai.jpa.exclusion.client;

import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.metamodel.EntityType;

import org.jboss.errai.ioc.client.Container;
import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.jpa.client.local.ErraiEntityManager;
import org.jboss.errai.jpa.client.local.ErraiMetamodel;
import org.jboss.errai.jpa.test.client.JpaTestClient;
import org.jboss.errai.jpa.test.client.res.JpaClientTestCase;
import org.jboss.errai.jpa.test.entity.Album;

public class EntityExclusionIntegrationTest extends JpaClientTestCase {

  @Override
  public String getModuleName() {
    return "org.jboss.tests.errai.jpa.exclusion.ExclusionJpaTest";
  }

  protected EntityManager getEntityManager() {
    final JpaTestClient testClient = JpaTestClient.INSTANCE;
    assertNotNull(testClient);
    assertNotNull(testClient.entityManager);
    ((ErraiEntityManager) testClient.entityManager).removeAll();
    return testClient.entityManager;
  }

  @Override
  protected void gwtSetUp() throws Exception {
    super.gwtSetUp();

    Album.CALLBACK_LOG.clear();

    // We need to bootstrap the IoC container manually because GWTTestCase
    // doesn't call onModuleLoad() for us.
    new Container().bootstrapContainer();
  }

  @Override
  protected void gwtTearDown() throws Exception {
    Container.reset();
    IOC.reset();
  }

  public void testAllowListedEntityIsInEntityManager() throws Exception {
    try {
      // we cannot use the class name to test here since the class is not available in client side code generation
      final EntityType et = ((ErraiMetamodel) getEntityManager().getMetamodel())
                        .entity("org.jboss.tests.errai.jpa.exclusion.client.res.AllowListedEntity");

      assertEquals("Incorrect entity type found", et.getJavaType().getName(),
                    "org.jboss.tests.errai.jpa.exclusion.client.res.AllowListedEntity");

    } catch (final IllegalArgumentException ex) {
      fail("AllowListedEntity was not included in EntityManager");
    }
  }

  public void testDenyListedEntityIsNotInEntityManager() throws Exception {

    try {
      ((ErraiMetamodel) getEntityManager().getMetamodel())
                        .entity("org.jboss.tests.errai.jpa.exclusion.client.res.DenyListedEntity");

      fail("DenyListedEntity was not excluded from EntityManager");
    } catch (final IllegalArgumentException ex) {
      // this is the expected behavior
    }
  }

  public void testAllowListedPackageIsInEntityManager() throws Exception {
    try {
      final EntityType et = ((ErraiMetamodel) getEntityManager().getMetamodel())
                        .entity("org.jboss.tests.errai.jpa.exclusion.allowlist.AllowListedPackageEntity");

      assertEquals("Incorrect entity type found", et.getJavaType().getName(),
                    "org.jboss.tests.errai.jpa.exclusion.allowlist.AllowListedPackageEntity");

    } catch (final IllegalArgumentException e) {
      fail("AllowListedPackageEntity was not found in EntityManager");
    }
  }

  public void testNestedDenyListedEntityIsNotInEntityManager() throws Exception {
    // tests to see if a denylisted class that is inside a allowlisted package is excluded
    // (denylist overrides allowlist)
    try {
      ((ErraiMetamodel) getEntityManager().getMetamodel())
        .entity("org.jboss.tests.errai.jpa.exclusion.allowlist.DenyListedEntityInAllowListedPackage");

      fail("DenyListedEntityInAllowListedPackage was not excluded from EntityManager");
    } catch (final IllegalArgumentException ex) {
      // this is the expected behavior
    }
  }

  public void testDenyListedPackageIsNotInEntityManager() throws Exception {
      final Set<EntityType<?>> entitySet = getEntityManager().getMetamodel().getEntities();

      for (final EntityType<?> et : entitySet) {
        final String className = et.getJavaType().getName();
        if (className.startsWith("org.jboss.tests.errai.jpa.exclusion.denylist")) {
          fail("Class "+ className + "from denylisted package not excluded from Entity Manager");
        }
      }
  }

  public void testAllowAndDenyListedEntityIsNotInEntityManager() throws Exception {
    // denylist overrides allowlist
    try {
      ((ErraiMetamodel) getEntityManager().getMetamodel())
        .entity("org.jboss.tests.errai.jpa.exclusion.client.AllowAndDenyListedEntity");

      fail("AllowAndDenyListedEntity was not excluded from EntityManager");
    } catch (final IllegalArgumentException ex) {
      // this is the expected behavior
    }


  }
}
