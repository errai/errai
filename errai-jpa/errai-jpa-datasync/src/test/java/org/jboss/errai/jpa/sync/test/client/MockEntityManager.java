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

package org.jboss.errai.jpa.sync.test.client;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.FlushModeType;
import javax.persistence.LockModeType;
import javax.persistence.Query;
import javax.persistence.StoredProcedureQuery;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.metamodel.Metamodel;

import org.hibernate.mapping.PersistentClass;
import org.hibernate.metamodel.internal.MetamodelImpl;

public class MockEntityManager implements EntityManager {

  @Override
  public void persist(Object entity) {
  }

  @Override
  public <T> T merge(T entity) {
    return null;
  }

  @Override
  public void remove(Object entity) {
  }

  @Override
  public <T> T find(Class<T> entityClass, Object primaryKey) {
    return null;
  }

  @Override
  public <T> T find(Class<T> entityClass, Object primaryKey, Map<String, Object> properties) {
    return null;
  }

  @Override
  public <T> T find(Class<T> entityClass, Object primaryKey, LockModeType lockMode) {
    return null;
  }

  @Override
  public <T> T find(Class<T> entityClass, Object primaryKey, LockModeType lockMode, Map<String, Object> properties) {
    return null;
  }

  @Override
  public <T> T getReference(Class<T> entityClass, Object primaryKey) {
    return null;
  }

  @Override
  public void flush() {
  }

  @Override
  public void setFlushMode(FlushModeType flushMode) {
  }

  @Override
  public FlushModeType getFlushMode() {
    return null;
  }

  @Override
  public void lock(Object entity, LockModeType lockMode) {
  }

  @Override
  public void lock(Object entity, LockModeType lockMode, Map<String, Object> properties) {
  }

  @Override
  public void refresh(Object entity) {
  }

  @Override
  public void refresh(Object entity, Map<String, Object> properties) {

  }

  @Override
  public void refresh(Object entity, LockModeType lockMode) {
  }

  @Override
  public void refresh(Object entity, LockModeType lockMode, Map<String, Object> properties) {
  }

  @Override
  public void clear() {

  }

  @Override
  public void detach(Object entity) {

  }

  @Override
  public boolean contains(Object entity) {
    return false;
  }

  @Override
  public LockModeType getLockMode(Object entity) {
    return null;
  }

  @Override
  public void setProperty(String propertyName, Object value) {
  }

  @Override
  public Map<String, Object> getProperties() {
    return null;
  }

  @Override
  public Query createQuery(String qlString) {
    return null;
  }

  @Override
  public <T> TypedQuery<T> createQuery(CriteriaQuery<T> criteriaQuery) {
    return null;
  }

  @Override
  public Query createQuery(CriteriaUpdate criteriaUpdate) {
    return null;
  }

  @Override
  public Query createQuery(CriteriaDelete criteriaDelete) {
    return null;
  }

  @Override
  public <T> TypedQuery<T> createQuery(String qlString, Class<T> resultClass) {
    return null;
  }

  @Override
  public Query createNamedQuery(String name) {
    return null;
  }

  @Override
  public <T> TypedQuery<T> createNamedQuery(String name, Class<T> resultClass) {
    return null;
  }

  @Override
  public Query createNativeQuery(String sqlString) {
    return null;
  }

  @SuppressWarnings("rawtypes")
  @Override
  public Query createNativeQuery(String sqlString, Class resultClass) {
    return null;
  }

  @Override
  public Query createNativeQuery(String sqlString, String resultSetMapping) {
    return null;
  }

  @Override
  public StoredProcedureQuery createNamedStoredProcedureQuery(String s) {
    return null;
  }

  @Override
  public StoredProcedureQuery createStoredProcedureQuery(String s) {
    return null;
  }

  @Override
  public StoredProcedureQuery createStoredProcedureQuery(String s, Class... classes) {
    return null;
  }

  @Override
  public StoredProcedureQuery createStoredProcedureQuery(String s, String... strings) {
    return null;
  }

  @Override
  public void joinTransaction() {
  }

  @Override
  public boolean isJoinedToTransaction() {
    return false;
  }

  @Override
  public <T> T unwrap(Class<T> cls) {
    return null;
  }

  @Override
  public Object getDelegate() {
    return null;
  }

  @Override
  public void close() {

  }

  @Override
  public boolean isOpen() {
    return false;
  }

  @Override
  public EntityTransaction getTransaction() {
    return null;
  }

  @Override
  public EntityManagerFactory getEntityManagerFactory() {
    return null;
  }

  @Override
  public CriteriaBuilder getCriteriaBuilder() {
    return null;
  }

  @Override
  public Metamodel getMetamodel() {
    return new MetamodelImpl(null, null);
  }

  @Override
  public <T> EntityGraph<T> createEntityGraph(Class<T> aClass) {
    return null;
  }

  @Override
  public EntityGraph<?> createEntityGraph(String s) {
    return null;
  }

  @Override
  public EntityGraph<?> getEntityGraph(String s) {
    return null;
  }

  @Override
  public <T> List<EntityGraph<? super T>> getEntityGraphs(Class<T> aClass) {
    return null;
  }

}
