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


import javax.persistence.EntityManager;

import org.jboss.errai.ioc.client.Container;
import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.jpa.client.local.ErraiEntityManager;
import org.jboss.errai.jpa.client.local.backend.LocalStorage;
import org.jboss.errai.jpa.client.local.backend.StorageBackend;
import org.jboss.errai.jpa.client.local.backend.StorageBackendFactory;
import org.jboss.errai.jpa.client.local.backend.WebStorageBackend;
import org.jboss.errai.jpa.test.client.res.JpaClientTestCase;
import org.jboss.errai.jpa.test.entity.Genre;

/**
 * Tests the JPA EntityManager facilities provided by Errai JPA.
 * <p>
 * Note that there is a {@link HibernateJpaTest subclass of this test} that runs
 * all the same checks against Hibernate, as a sanity check that we're testing
 * for actual JPA-sanctioned and JPA-compatible behaviour.
 *
 * @author Jonathan Fuerth <jfuerth@gmail.com>
 */
public class NamespacedEntityManagerTest extends JpaClientTestCase {

  @Override
  public String getModuleName() {
    return "org.jboss.errai.jpa.test.JpaTest";
  }

  protected ErraiEntityManager getNonNamespacedEntityManager() {
    JpaTestClient testClient = JpaTestClient.INSTANCE;
    assertNotNull(testClient);
    assertNotNull(testClient.entityManager);
    return (ErraiEntityManager) JpaTestClient.INSTANCE.entityManager;
  }

  protected EntityManager getNamespacedEntityManager(final String namespace) {
    ErraiEntityManager originalEm = getNonNamespacedEntityManager();
    StorageBackendFactory namespacedStorageBackend = new StorageBackendFactory() {
      @Override
      public StorageBackend createInstanceFor(ErraiEntityManager em) {
        return new WebStorageBackend(em, namespace);
      }
    };
    return new ErraiEntityManager(originalEm, namespacedStorageBackend);
  }

  @Override
  protected void gwtSetUp() throws Exception {
    super.gwtSetUp();

    // We need to bootstrap the IoC container manually because GWTTestCase
    // doesn't call onModuleLoad() for us.
    new Container().bootstrapContainer();

    LocalStorage.removeAll();
  }

  @Override
  protected void gwtTearDown() throws Exception {
    Container.reset();
    IOC.reset();
  }

  public void testDefaultNamespaceIsolated() throws Exception {
    EntityManager em = getNonNamespacedEntityManager();
    EntityManager emA = getNamespacedEntityManager("a");

    Genre test1 = new Genre("test1");
    Genre test2 = new Genre("test2");

    em.persist(test1);
    em.persist(test2);
    em.flush();

    emA.persist(test1);
    emA.persist(test2);
    emA.flush();

    em.remove(test1);
    emA.remove(test2);

    em.clear();
    emA.clear();

    assertNull(em.find(Genre.class, test1.getId()));
    assertNotNull(em.find(Genre.class, test2.getId()));

    assertNotNull(emA.find(Genre.class, test1.getId()));
    assertNull(emA.find(Genre.class, test2.getId()));
  }

  public void testNonDefaultNamespacesIsolated() throws Exception {
    EntityManager emB = getNamespacedEntityManager("b");
    EntityManager emA = getNamespacedEntityManager("a");

    Genre test1 = new Genre("test1");
    Genre test2 = new Genre("test2");

    emB.persist(test1);
    emB.persist(test2);
    emB.flush();

    emA.persist(test1);
    emA.persist(test2);
    emA.flush();

    emB.remove(test1);
    emA.remove(test2);

    emB.clear();
    emA.clear();

    assertNull(emB.find(Genre.class, test1.getId()));
    assertNotNull(emB.find(Genre.class, test2.getId()));

    assertNotNull(emA.find(Genre.class, test1.getId()));
    assertNull(emA.find(Genre.class, test2.getId()));
  }

  public void testRemoveAllIsIsolatedToNamespace() throws Exception {
    EntityManager em = getNonNamespacedEntityManager();
    EntityManager emA = getNamespacedEntityManager("a");
    EntityManager emB = getNamespacedEntityManager("b");

    Genre test1 = new Genre("test1");
    Genre test2 = new Genre("test2");

    em.persist(test1);
    em.persist(test2);
    em.flush();

    emA.persist(test1);
    emA.persist(test2);
    emA.flush();

    emB.persist(test1);
    emB.persist(test2);
    emB.flush();


    ((ErraiEntityManager) emA).removeAll();

    assertNotNull(em.find(Genre.class, test1.getId()));
    assertNotNull(em.find(Genre.class, test2.getId()));

    assertNull(emA.find(Genre.class, test1.getId()));
    assertNull(emA.find(Genre.class, test2.getId()));

    assertNotNull(emB.find(Genre.class, test1.getId()));
    assertNotNull(emB.find(Genre.class, test2.getId()));


    ((ErraiEntityManager) em).removeAll();

    assertNull(em.find(Genre.class, test1.getId()));
    assertNull(em.find(Genre.class, test2.getId()));

    assertNull(emA.find(Genre.class, test1.getId()));
    assertNull(emA.find(Genre.class, test2.getId()));

    assertNotNull(emB.find(Genre.class, test1.getId()));
    assertNotNull(emB.find(Genre.class, test2.getId()));
  }
}
