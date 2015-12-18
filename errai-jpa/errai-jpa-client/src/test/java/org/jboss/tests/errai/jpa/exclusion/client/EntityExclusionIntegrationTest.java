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
import org.jboss.errai.ioc.client.container.IOCBeanManagerLifecycle;
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
    JpaTestClient testClient = JpaTestClient.INSTANCE;
    assertNotNull(testClient);
    assertNotNull(testClient.entityManager);
    ((ErraiEntityManager) testClient.entityManager).removeAll();
    return testClient.entityManager;
  }

  @Override
  protected void gwtSetUp() throws Exception {
    super.gwtSetUp();

    Album.CALLBACK_LOG.clear();

    new IOCBeanManagerLifecycle().resetBeanManager();

    // We need to bootstrap the IoC container manually because GWTTestCase
    // doesn't call onModuleLoad() for us.
    new Container().bootstrapContainer();
  }

  @Override
  protected void gwtTearDown() throws Exception {
    Container.reset();
    IOC.reset();
  }

  public void testWhiteListedEntityIsInEntityManager() throws Exception {
    try {
      // we cannot use the class name to test here since the class is not available in client side code generation
      EntityType et = ((ErraiMetamodel) getEntityManager().getMetamodel())
                        .entity("org.jboss.tests.errai.jpa.exclusion.client.res.WhiteListedEntity");

      assertEquals("Incorrect entity type found", et.getJavaType().getName(),
                    "org.jboss.tests.errai.jpa.exclusion.client.res.WhiteListedEntity");

    } catch (IllegalArgumentException ex) {
      fail("WhiteListedEntity was not included in EntityManager");
    }
  }

  public void testBlackListedEntityIsNotInEntityManager() throws Exception {

    try {
      ((ErraiMetamodel) getEntityManager().getMetamodel())
                        .entity("org.jboss.tests.errai.jpa.exclusion.client.res.BlackListedEntity");

      fail("BlackListedEntity was not excluded from EntityManager");
    } catch (IllegalArgumentException ex) {
      // this is the expected behavior
    }
  }

  public void testWhiteListedPackageIsInEntityManager() throws Exception {
    try {
      EntityType et = ((ErraiMetamodel) getEntityManager().getMetamodel())
                        .entity("org.jboss.tests.errai.jpa.exclusion.whitelist.WhiteListedPackageEntity");

      assertEquals("Incorrect entity type found", et.getJavaType().getName(),
                    "org.jboss.tests.errai.jpa.exclusion.whitelist.WhiteListedPackageEntity");

    } catch (IllegalArgumentException e) {
      fail("WhiteListedPackageEntity was not found in EntityManager");
    }
  }

  public void testNestedBlackListedEntityIsNotInEntityManager() throws Exception {
    // tests to see if a blacklisted class that is inside a whitelisted package is excluded
    // (blacklist overrides whitelist)
    try {
      ((ErraiMetamodel) getEntityManager().getMetamodel())
        .entity("org.jboss.tests.errai.jpa.exclusion.whitelist.BlackListedEntityInWhiteListedPackage");

      fail("BlackListedEntityInWhiteListedPackage was not excluded from EntityManager");
    } catch (IllegalArgumentException ex) {
      // this is the expected behavior
    }
  }

  public void testBlackListedPackageIsNotInEntityManager() throws Exception {
      Set<EntityType<?>> entitySet = getEntityManager().getMetamodel().getEntities();

      for (EntityType<?> et : entitySet) {
        String className = et.getJavaType().getName();
        if (className.startsWith("org.jboss.tests.errai.jpa.exclusion.blacklist")) {
          fail("Class "+ className + "from blacklisted package not excluded from Entity Manager");
        }
      }
  }

  public void testWhiteAndBlackListedEntityIsNotInEntityManager() throws Exception {
    // blacklist overrides whitelist
    try {
      ((ErraiMetamodel) getEntityManager().getMetamodel())
        .entity("org.jboss.tests.errai.jpa.exclusion.client.WhiteAndBlackListedEntity");

      fail("WhiteAndBlackListedEntity was not excluded from EntityManager");
    } catch (IllegalArgumentException ex) {
      // this is the expected behavior
    }


  }
}
