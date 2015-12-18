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

import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;

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
  public void testStoreAndFetchOneWithEverythingUsingFieldAccess() throws Exception {
    // skip
  }

  /**
   * Regression check for ERRAI-675.
   * Hibernate is allowed to find the non client entity since it manages client as well as server entities.
   */
  @Override
  public void testNonClientEntityIsNotInEntityManager() {
    // skip
  }

  /**
   * Hibernate (at least on top of HSQLDB) damages BigDecimal (adds extra
   * precision), java.util.Date (returns a java.sql.Date), and
   * java.sql.Timestamp (truncates nanosecond precision). So we skip this test
   * in Hibernate mode.
   */
  @Override
  public void testStoreAndFetchOneWithEverythingUsingMethodAccess() throws Exception {
    // skip
  }

  /**
   * This is a test to see how Errai copes with structural changes to the entity
   * model. Not applicable to Hibernate.
   */
  @Override
  public void testAddPrimitiveFieldToPreviouslyPersistedEntity() {
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

  /**
   * Hibernate doesn't know how to unwrap Errai's WrappedPortables, so we have
   * to skip this test.
   */
  @Override
  public void testEnsurePropertyChangeEventIsFiredAfterIdGeneration() {
    // skip
  }
}
