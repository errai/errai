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

package org.jboss.errai.jpa.client.local;

import javax.persistence.Parameter;
import javax.persistence.TypedQuery;

import org.jboss.errai.common.client.api.Assert;

import com.google.common.collect.ImmutableBiMap;

/**
 * Factory class for creating TypedQuery instances. Used internally by
 * EntityManager's <i>createXXXQuery</i> methods.
 *
 * @author Jonathan Fuerth <jfuerth@gmail.com>
 */
public abstract class TypedQueryFactory {
  protected final Class<?> actualResultType;
  protected final ImmutableBiMap<String, Parameter<?>> parameters;


  public TypedQueryFactory(
          Class<?> actualResultType,
          ErraiParameter<?>[] parameters) {
    this.actualResultType = Assert.notNull(actualResultType);

    ImmutableBiMap.Builder<String, Parameter<?>> pb = ImmutableBiMap.builder();
    for (Parameter<?> p : parameters) {
      pb.put(p.getName(), p);
    }
    this.parameters = pb.build();
  }

  /**
   * Creates an instance of the TypedQuery associated with this factory if its
   * result type is assignable to the given type.
   *
   * @param resultType
   *          The expected result type
   * @param entityManager
   *          the EntityManager the query will be executed in. Must not be null.
   * @param <T>
   *          The result type of the queries produced by this factory
   * @return A new instance of TypedQuery, whose result type is assignable to
   *         {@code resultType}.
   * @throws IllegalArgumentException
   *           if the query's result type is not assignable to the given type.
   */
  public <T> TypedQuery<T> createIfCompatible(Class<T> resultType, ErraiEntityManager entityManager) {
    Class<?> resultSupertype = actualResultType;
    while (resultSupertype != null) {
      if (resultType == resultSupertype) {
        return createQuery(entityManager);
      }
      resultSupertype = resultSupertype.getSuperclass();
    }
    throw new IllegalArgumentException("Expected return type " + resultType + " is not assignable from actual return type " + actualResultType);
  }

  /**
   * Subclasses must implement this method by returning a new instance of
   * ErraiTypedQuery that implements the query logic for the JPA query handled
   * by this factory.
   *
   * @param the
   *          EntityManager the query will be executed in. Must not be null.
   * @return a new instance of ErraiTypedQuery.
   */
  protected abstract <T> TypedQuery<T> createQuery(ErraiEntityManager entityManager);

}
