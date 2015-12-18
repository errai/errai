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

package org.jboss.errai.jpa.sync.client.shared;

import java.util.Collections;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.metamodel.EntityType;

import org.jboss.errai.common.client.api.Assert;
import org.jboss.errai.common.client.api.annotations.MapsTo;
import org.jboss.errai.common.client.api.annotations.Portable;

@Portable
public class SyncableDataSet<E> {

  private final String queryName;
  private final Map<String, Object> params;

  /** Stored as a string so to keep SyncableDataSet marshallable. */
  private final String resultTypeFqcn;

  // FIXME need to allow app to specify TemporalType for date params
  public static <E> SyncableDataSet<E> from(String queryName, Class<E> resultType, Map<String, Object> params) {
    return new SyncableDataSet<E>(queryName, resultType, params);
  }

  private SyncableDataSet(String queryName, Class<E> resultType, Map<String, Object> params) {
    this(queryName, resultType.getName(), params);
  }

  // Errai Marshalling constructor
  private SyncableDataSet(
          @MapsTo("queryName") String queryName,
          @MapsTo("resultTypeFqcn") String resultTypeFqcn,
          @MapsTo("params") Map<String, Object> params) {
    this.queryName = Assert.notNull(queryName);
    this.resultTypeFqcn = resultTypeFqcn;
    this.params = Collections.unmodifiableMap(params);
  }

  public TypedQuery<E> createQuery(EntityManager em) {
    TypedQuery<E> query = em.createNamedQuery(queryName, getResultType(em));
    for (Map.Entry<String, Object> param : params.entrySet()) {
      // FIXME should use TemporalType arg here when necessary
      query.setParameter(param.getKey(), param.getValue());
    }
    return query;
  }

  @SuppressWarnings("unchecked")
  private Class<E> getResultType(EntityManager em) {
    // We support this so users don't have to specify the query return type when using @Sync.
    if (resultTypeFqcn.equals("java.lang.Object")) {
      return (Class<E>) Object.class;
    }
    
    for (EntityType<?> et : em.getMetamodel().getEntities()) {
      if (et.getJavaType().getName().equals(resultTypeFqcn)) {
        return ((EntityType<E>) et).getJavaType();
      }
    }

    throw new IllegalStateException("Result type " + resultTypeFqcn + " is not known to the EntityManager.");
  }

  /**
   * Returns the name of the JPA named query this syncable data set is tied to.
   *
   * @return the query name. Never null.
   */
  public String getQueryName() {
    return queryName;
  }

  /**
   * Returns a read-only view of this syncable data set's parameters. These
   * should correspond with the named parameters of the JPA named query this
   * syncable data set is tied to.
   *
   * @return a read-only map of the parameters in use with the query. Never null.
   */
  public Map<String, Object> getParameters() {
    return params;
  }

  @Override
  public String toString() {
    return "SyncableDataSet [queryName=" + queryName + ", params=" + params + ", resultType=" + resultTypeFqcn
            + "]";
  }


}
