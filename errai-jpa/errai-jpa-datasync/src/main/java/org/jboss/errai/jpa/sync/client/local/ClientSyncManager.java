package org.jboss.errai.jpa.sync.client.local;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.persistence.TypedQuery;

import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.jpa.client.local.ErraiEntityManager;
import org.jboss.errai.jpa.client.local.ErraiEntityType;
import org.jboss.errai.jpa.client.local.ErraiIdGenerator;
import org.jboss.errai.jpa.client.local.ErraiSingularAttribute;
import org.jboss.errai.jpa.client.local.Key;
import org.jboss.errai.jpa.client.local.backend.StorageBackend;
import org.jboss.errai.jpa.client.local.backend.StorageBackendFactory;
import org.jboss.errai.jpa.client.local.backend.WebStorageBackend;
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

@ApplicationScoped
public class ClientSyncManager {

  /**
   * Temporarily public so we can override the caller from within the tests. Will find a better way in the future!
   */
  public @Inject Caller<DataSyncService> dataSyncService;

  @Inject Event<DataSyncCompleteEvent<?>> completeEvent;

  /**
   * This is the entity manager that client code interacts with. From a data
   * sync point of view, it contains the "desired state" of the entities.
   */
  @Inject private ErraiEntityManager desiredStateEm;

  /**
   * This entity manager tracks the state of entities according to the most
   * recent information we've received from the server side. From a data sync
   * point of view, it contains the "expected state" of the entities we're
   * trying to update.
   */
  private ErraiEntityManager expectedStateEm;

  private EntityComparator entityComparator;

  private final JpaAttributeAccessor attributeAccessor = new ErraiAttributeAccessor();

  /**
   * These are all the data sets we're currently keeping in sync.
   */
  //private final List<SyncableDataSet<?>> activeSyncSets = new ArrayList<SyncableDataSet<?>>();

  @PostConstruct
  private void setup() {
    expectedStateEm = new ErraiEntityManager(desiredStateEm, new StorageBackendFactory() {
      @Override
      public StorageBackend createInstanceFor(ErraiEntityManager em) {
        return new WebStorageBackend(em, "expected-state:");
      }
    });
    entityComparator = new EntityComparator(desiredStateEm.getMetamodel(), attributeAccessor);
  }

  public <E> void coldSync(String queryName, Class<E> queryResultType, Map<String, Object> queryParams) {
    final TypedQuery<E> query = desiredStateEm.createNamedQuery(queryName, queryResultType);
    final TypedQuery<E> expectedQuery = expectedStateEm.createNamedQuery(queryName, queryResultType);
    for (Map.Entry<String, Object> param : queryParams.entrySet()) {
      query.setParameter(param.getKey(), param.getValue());
      expectedQuery.setParameter(param.getKey(), param.getValue());
    }

    final Map<Key<E, Object>, E> expectedResults = new HashMap<Key<E, Object>, E>();
    for (E expectedState : expectedQuery.getResultList()) {
      expectedResults.put((Key<E, Object>) expectedStateEm.keyFor(expectedState), expectedState);
    }

    final List<SyncRequestOperation<E>> syncRequests = new ArrayList<SyncRequestOperation<E>>();
    for (E desiredState : query.getResultList()) {
      Key<E, ?> key = desiredStateEm.keyFor(desiredState);
      E expectedState = expectedResults.remove(key);
      if (expectedState == null) {
        syncRequests.add(SyncRequestOperation.created(desiredState));
      }
      else if (entityComparator.isDifferent(desiredState, expectedState)) {
        syncRequests.add(SyncRequestOperation.updated(desiredState, expectedState));
      }
      else /* desiredState == expectedState */ {
        syncRequests.add(SyncRequestOperation.unchanged(expectedState));
      }
    }

    for (Map.Entry<Key<E, Object>, E> remainingEntry : expectedResults.entrySet()) {
      syncRequests.add(SyncRequestOperation.deleted(remainingEntry.getValue()));
    }

    final SyncableDataSet<E> syncSet = SyncableDataSet.from(queryName, queryResultType, queryParams);
    dataSyncService.call(new RemoteCallback<List<SyncResponse<E>>>() {
      @Override
      public void callback(List<SyncResponse<E>> syncResponse) {
        applyResults(syncResponse);
        completeEvent.fire(new DataSyncCompleteEvent<E>(true, syncResponse));
      }
    }).coldSync(syncSet, syncRequests);
  }

  /**
   * Performs operations on the desired and expected state entity managers to
   * reconcile them with the new information in the given sync response
   * operations.
   *
   * @param syncResponses
   *          a list of sync response operations that was received from the
   *          server.
   */
  private <E> void applyResults(List<SyncResponse<E>> syncResponses) {
    // XXX could we factor this decision tree into apply() methods on the sync response objects?
    for (SyncResponse<E> response : syncResponses) {
      if (response instanceof ConflictResponse) {
        ConflictResponse<E> cr = (ConflictResponse<E>) response;
        E actualNew = cr.getActualNew();
        E requestedNew = cr.getRequestedNew();
        System.out.println("Got a conflict for " + actualNew);
        System.out.println("              was: " + cr.getExpected());
        System.out.println("           wanted: " + requestedNew);
        System.out.println(" ... accepting server's version of reality for now");

        if (actualNew == null) {
          E resolved = expectedStateEm.find(expectedStateEm.keyFor(requestedNew), Collections.<String,Object>emptyMap());
          expectedStateEm.remove(resolved);

          resolved = desiredStateEm.find(desiredStateEm.keyFor(requestedNew), Collections.<String,Object>emptyMap());
          desiredStateEm.remove(resolved);
        }
        else {
          expectedStateEm.merge(actualNew);
          desiredStateEm.merge(actualNew);
        }
        // TODO (need transaction support in client)
//        desiredStateEm.getTransaction().setRollbackOnly();
//        expectedStateEm.merge(cr.getActualNew());
//        throw new RuntimeException("TODO: notify conflict listeners");
      }
      else if (response instanceof DeleteResponse) {
        DeleteResponse<E> dr = (DeleteResponse<E>) response;
        E resolved = expectedStateEm.find(expectedStateEm.keyFor(dr.getEntity()), Collections.<String,Object>emptyMap());
        expectedStateEm.remove(resolved);

        // the DeleteResponse could be a reaction to our own delete request, in which case resolved == null
        resolved = desiredStateEm.find(desiredStateEm.keyFor(dr.getEntity()), Collections.<String,Object>emptyMap());
        if (resolved != null) {
          desiredStateEm.remove(resolved);
        }
      }
      else if (response instanceof IdChangeResponse) {
        IdChangeResponse<E> icr = (IdChangeResponse<E>) response;
        E newEntity = icr.getEntity();
        expectedStateEm.persist(newEntity);

        @SuppressWarnings("unchecked")
        ErraiEntityType<E> entityType = desiredStateEm.getMetamodel().entity((Class<E>) newEntity.getClass());
        ErraiSingularAttribute<? super E, Object> idAttr = entityType.getId(Object.class);
        changeId(entityType, icr.getOldId(), idAttr.get(newEntity));
        desiredStateEm.merge(newEntity);
      }
      else if (response instanceof NewRemoteEntityResponse) {
        NewRemoteEntityResponse<E> nrer = (NewRemoteEntityResponse<E>) response;

        @SuppressWarnings("unchecked")
        Class<E> entityClass = (Class<E>) nrer.getEntity().getClass();

        Key<E, ?> conflictingKey = desiredStateEm.keyFor(nrer.getEntity());
        E inTheWay = desiredStateEm.find(conflictingKey, Collections.<String,Object>emptyMap());
        if (inTheWay != null) {
          ErraiEntityType<E> entityType = desiredStateEm.getMetamodel().entity(entityClass);
          ErraiSingularAttribute<? super E, Object> idAttr = entityType.getId(Object.class);
          ErraiIdGenerator<Object> idGenerator = idAttr.getValueGenerator();
          if (idGenerator != null && idGenerator.hasNext(desiredStateEm)) {
            Object newLocalId = idGenerator.next(desiredStateEm);
            changeId(entityType, conflictingKey.getId(), newLocalId);
          }
          else {
            throw new IllegalStateException(
                    "New entity from server would clobber local entity with same id, and we are unable to generate a new ID." +
                    " Conflict is for: " + conflictingKey);
          }
        }

        // these are really "persist" operations, but using merge so each entity manager gets its own instance
        expectedStateEm.merge(nrer.getEntity());
        desiredStateEm.merge(nrer.getEntity());
      }
      else if (response instanceof UpdateResponse) {
        UpdateResponse<E> ur = (UpdateResponse<E>) response;
        expectedStateEm.merge(ur.getEntity());
        desiredStateEm.merge(ur.getEntity());
      }
      else {
        throw new RuntimeException("Unexpected kind of sync response: " + response);
      }
    }
  }

  /**
   * Changes the ID of an existing entity in desiredStateEm.
   *
   * @param entityType The metamodel type for the entity whose ID is to be changed
   * @param oldId The ID that the entity currently has.
   * @param newId The ID that the entity will have when this method returns.
   */
  private <E> void changeId(ErraiEntityType<E> entityType, Object oldId, Object newId) {
    // XXX this routine is probably better handled internally by the ErraiEntityManager
    // TODO what about related entities that refer to this one? (needs tests)

    E entity = desiredStateEm.find(entityType.getJavaType(), oldId);
    desiredStateEm.remove(entity);
    desiredStateEm.flush();
    desiredStateEm.detach(entity);
    entityType.getId(Object.class).set(entity, newId);
    desiredStateEm.persist(entity);
  }

  /**
   * Returns the persistence context that holds the "expected state" of this
   * Client-side Sync Manager (the state that we believe the entities have on
   * the server). This method exists mostly to promote testability, and is
   * rarely needed by applications at runtime.
   */
  public ErraiEntityManager getExpectedStateEm() {
    return expectedStateEm;
  }

  /**
   * Returns the persistence context that holds the "desired state" of this
   * Client-side Sync Manager (the state that the application has set up, which
   * we will eventually sync to the server). This method exists mostly to
   * promote testability, and is rarely needed by applications at runtime.
   */
  public ErraiEntityManager getDesiredStateEm() {
    return desiredStateEm;
  }
}
