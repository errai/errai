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

package org.jboss.errai.jpa.test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.CollectionAttribute;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.MapAttribute;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;

import junit.framework.TestCase;

import org.jboss.errai.jpa.rebind.ClassSorter;
import org.jboss.errai.jpa.test.entity.inherit.ChildOfConcreteParentEntity;
import org.jboss.errai.jpa.test.entity.inherit.ParentConcreteEntity;

public class ClassSorterUnitTest extends TestCase {

  private static class MockManagedType<X> implements ManagedType<X> {

    private Class<X> javaType;

    public MockManagedType(Class<X> javaType) {
      this.javaType = javaType;
    }

    public static <X> MockManagedType<X> of(Class<X> javaType) {
      return new MockManagedType<X>(javaType);
    }

    @Override
    public javax.persistence.metamodel.Type.PersistenceType getPersistenceType() {
      throw new RuntimeException("Not implemented");
    }

    @Override
    public Class<X> getJavaType() {
      return javaType;
    }

    @Override
    public Set<Attribute<? super X, ?>> getAttributes() {
      throw new RuntimeException("Not implemented");
    }

    @Override
    public Set<Attribute<X, ?>> getDeclaredAttributes() {
      throw new RuntimeException("Not implemented");
    }

    @Override
    public <Y> SingularAttribute<? super X, Y> getSingularAttribute(String name, Class<Y> type) {
      throw new RuntimeException("Not implemented");
    }

    @Override
    public <Y> SingularAttribute<X, Y> getDeclaredSingularAttribute(String name, Class<Y> type) {
      throw new RuntimeException("Not implemented");
    }

    @Override
    public Set<SingularAttribute<? super X, ?>> getSingularAttributes() {
      throw new RuntimeException("Not implemented");
    }

    @Override
    public Set<SingularAttribute<X, ?>> getDeclaredSingularAttributes() {
      throw new RuntimeException("Not implemented");
    }

    @Override
    public <E> CollectionAttribute<? super X, E> getCollection(String name, Class<E> elementType) {
      throw new RuntimeException("Not implemented");
    }

    @Override
    public <E> CollectionAttribute<X, E> getDeclaredCollection(String name, Class<E> elementType) {
      throw new RuntimeException("Not implemented");
    }

    @Override
    public <E> SetAttribute<? super X, E> getSet(String name, Class<E> elementType) {
      throw new RuntimeException("Not implemented");
    }

    @Override
    public <E> SetAttribute<X, E> getDeclaredSet(String name, Class<E> elementType) {
      throw new RuntimeException("Not implemented");
    }

    @Override
    public <E> ListAttribute<? super X, E> getList(String name, Class<E> elementType) {
      throw new RuntimeException("Not implemented");
    }

    @Override
    public <E> ListAttribute<X, E> getDeclaredList(String name, Class<E> elementType) {
      throw new RuntimeException("Not implemented");
    }

    @Override
    public <K, V> MapAttribute<? super X, K, V> getMap(String name, Class<K> keyType, Class<V> valueType) {
      throw new RuntimeException("Not implemented");
    }

    @Override
    public <K, V> MapAttribute<X, K, V> getDeclaredMap(String name, Class<K> keyType, Class<V> valueType) {
      throw new RuntimeException("Not implemented");
    }

    @Override
    public Set<PluralAttribute<? super X, ?, ?>> getPluralAttributes() {
      throw new RuntimeException("Not implemented");
    }

    @Override
    public Set<PluralAttribute<X, ?, ?>> getDeclaredPluralAttributes() {
      throw new RuntimeException("Not implemented");
    }

    @Override
    public Attribute<? super X, ?> getAttribute(String name) {
      throw new RuntimeException("Not implemented");
    }

    @Override
    public Attribute<X, ?> getDeclaredAttribute(String name) {
      throw new RuntimeException("Not implemented");
    }

    @Override
    public SingularAttribute<? super X, ?> getSingularAttribute(String name) {
      throw new RuntimeException("Not implemented");
    }

    @Override
    public SingularAttribute<X, ?> getDeclaredSingularAttribute(String name) {
      throw new RuntimeException("Not implemented");
    }

    @Override
    public CollectionAttribute<? super X, ?> getCollection(String name) {
      throw new RuntimeException("Not implemented");
    }

    @Override
    public CollectionAttribute<X, ?> getDeclaredCollection(String name) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public SetAttribute<? super X, ?> getSet(String name) {
      throw new RuntimeException("Not implemented");
    }

    @Override
    public SetAttribute<X, ?> getDeclaredSet(String name) {
      throw new RuntimeException("Not implemented");
    }

    @Override
    public ListAttribute<? super X, ?> getList(String name) {
      throw new RuntimeException("Not implemented");
    }

    @Override
    public ListAttribute<X, ?> getDeclaredList(String name) {
      throw new RuntimeException("Not implemented");
    }

    @Override
    public MapAttribute<? super X, ?, ?> getMap(String name) {
      throw new RuntimeException("Not implemented");
    }

    @Override
    public MapAttribute<X, ?, ?> getDeclaredMap(String name) {
      throw new RuntimeException("Not implemented");
    }

    @Override
    public String toString() {
      return javaType.getName();
    }
  }

  public void testSupertypesFirst() throws Exception {
    ManagedType<?> parent = MockManagedType.of(ParentConcreteEntity.class);
    ManagedType<?> child = MockManagedType.of(ChildOfConcreteParentEntity.class);
    ManagedType<?> object = MockManagedType.of(Object.class);
    List<ManagedType<?>> unsorted = new ArrayList<ManagedType<?>>();
    unsorted.add(parent);
    unsorted.add(child);
    unsorted.add(object);

    List<ManagedType<?>> sorted = ClassSorter.supertypesFirst(unsorted);

    assertSame(object, sorted.get(0));
    assertSame(parent, sorted.get(1));
    assertSame(child, sorted.get(2));
  }
}
