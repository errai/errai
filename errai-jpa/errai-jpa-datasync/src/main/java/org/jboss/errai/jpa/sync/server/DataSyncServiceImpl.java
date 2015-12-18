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

package org.jboss.errai.jpa.sync.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.SingularAttribute;

import org.jboss.errai.common.client.api.Assert;
import org.jboss.errai.jpa.sync.client.shared.ConflictResponse;
import org.jboss.errai.jpa.sync.client.shared.DataSyncService;
import org.jboss.errai.jpa.sync.client.shared.DeleteResponse;
import org.jboss.errai.jpa.sync.client.shared.EntityComparator;
import org.jboss.errai.jpa.sync.client.shared.IdChangeResponse;
import org.jboss.errai.jpa.sync.client.shared.JpaAttributeAccessor;
import org.jboss.errai.jpa.sync.client.shared.NewRemoteEntityResponse;
import org.jboss.errai.jpa.sync.client.shared.SyncRequestOperation;
import org.jboss.errai.jpa.sync.client.shared.SyncResponse;
import org.jboss.errai.jpa.sync.client.shared.SyncableDataSet;
import org.jboss.errai.jpa.sync.client.shared.UpdateResponse;

public class DataSyncServiceImpl implements DataSyncService {

  private final EntityManager em;
  private final JpaAttributeAccessor attributeAccessor;
  private final EntityComparator entityComparator;

  public DataSyncServiceImpl(EntityManager em, JpaAttributeAccessor attributeAccessor) {
    this.em = Assert.notNull(em);
    this.attributeAccessor = Assert.notNull(attributeAccessor);
    this.entityComparator = new EntityComparator(em.getMetamodel(), attributeAccessor);
  }

  @Override
  public <E> List<SyncResponse<E>> coldSync(SyncableDataSet<E> dataSet, List<SyncRequestOperation<E>> syncRequestOps) {
    TypedQuery<E> query = dataSet.createQuery(em);
    Map<Object, E> localResults = new HashMap<Object, E>();
    for (E localEntity : query.getResultList()) {
      localResults.put(id(localEntity), localEntity);
    }

    // maps the old remote ID -> new local persistent entity
    Map<Object, E> newLocalEntities = new HashMap<Object, E>();

    // the response we will return
    List<SyncResponse<E>> syncResponse = new ArrayList<SyncResponse<E>>();

    for (SyncRequestOperation<E> syncReq : syncRequestOps) {

      // the new state desired by the client. Can be null (for example, entity was remotely deleted).
      final E remoteNewState = syncReq.getEntity();

      // the expected state (last thing this client saw from us). Can be null (for example, entity was remotely created).
      final E remoteExpectedState = syncReq.getExpectedState();

      // the JPA ID of the remote entity, whether new to us or known before
      final Object remoteId;
      if (remoteNewState != null) {
        remoteId = id(remoteNewState);
      }
      else if (remoteExpectedState != null) {
        remoteId = id(remoteExpectedState);
      }
      else {
        throw new IllegalArgumentException("New and Expected states can't both be null");
      }

      // our actual local copy of the entity (null if it has been deleted)
      final E localState = localResults.get(remoteId);

      // TODO handle related entities reachable from the given ones

      switch (syncReq.getType()) {
      case UPDATED:
        localResults.remove(remoteId);
        if (entityComparator.isDifferent(localState, remoteExpectedState)) {
          syncResponse.add(new ConflictResponse<E>(remoteExpectedState, localState, remoteNewState));
        }
        else {
          syncResponse.add(new UpdateResponse<E>(em.merge(remoteNewState)));
        }
        break;

      case NEW:
        clearId(remoteNewState);
        em.persist(remoteNewState);
        newLocalEntities.put(remoteId, remoteNewState);
        break;

      case UNCHANGED:
        if (localState == null) {
          syncResponse.add(new DeleteResponse<E>(remoteExpectedState));
        }
        else {
          localResults.remove(remoteId);
          if (entityComparator.isDifferent(localState, remoteExpectedState)) {
            syncResponse.add(new UpdateResponse<E>(localState));
          }
        }
        break;

      case DELETED:
        // have to check for null in case someone else already deleted this entity
        if (localState != null) {
          // FIXME need to compare expected state with actual; issue conflict if they differ
          localResults.remove(remoteId);
          em.remove(localState);
          syncResponse.add(new DeleteResponse<E>(localState));
        }
        break;

      default:
        throw new UnsupportedOperationException("Unknown sync request type: " + syncReq.getType());
      }
    }

    em.flush();

    // pick up new IDs (this has to be done after the flush)
    for (Map.Entry<Object, E> newLocalEntity : newLocalEntities.entrySet()) {
      syncResponse.add(new IdChangeResponse<E>(newLocalEntity.getKey(), newLocalEntity.getValue()));
    }

    for (E newOnThisSide : localResults.values()) {
      syncResponse.add(new NewRemoteEntityResponse<E>(newOnThisSide));
    }
    return syncResponse;
  }

  /**
   * Returns the ID of the given object, which must be a JPA entity.
   *
   * @param entity
   *          the JPA entity whose ID value to retrieve
   * @return The ID of the given entity. If the entity ID type is primitive (for
   *         example, {@code int} as opposed to {@code Integer}), the
   *         corresponding boxed value will be returned.
   */
  private <X> Object id(X entity) {
    // XXX probably need to pass in the actual entity class rather than this cast
    // (because dynamic proxies will fool it)
    @SuppressWarnings("unchecked")
    EntityType<X> type = em.getMetamodel().entity((Class<X>) entity.getClass());
    SingularAttribute<? super X, ?> attr = type.getId(type.getIdType().getJavaType());
    return attributeAccessor.get(attr, entity);
  }

  /**
   * Sets the ID of the given object, which must be a JPA entity, to its default
   * value. The default value for reference types is {@code null}; the default
   * value for primitive types is the same as the JVM default value for an
   * uninitialized field.
   *
   * @param entity
   *          the JPA entity whose ID value to clear
   */
  private <X> void clearId(X entity) {
    // XXX probably need to pass in the actual entity class rather than this cast
    // (because dynamic proxies will fool it)
    @SuppressWarnings("unchecked")
    EntityType<X> type = em.getMetamodel().entity((Class<X>) entity.getClass());
    SingularAttribute<? super X, ?> attr = type.getId(type.getIdType().getJavaType());
    attributeAccessor.set(attr, entity, null);
  }

}
