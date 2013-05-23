package org.jboss.errai.jpa.sync.client.local;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.TypedQuery;

import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.ErrorCallback;
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

/**
 * The main contact point for applications that want to initiate data sync
 * operations from the client side of an Errai application.
 *
 * @author Jonathan Fuerth <jfuerth@redhat.com>
 */
@ApplicationScoped
public class ClientSyncManager {

  /**
   * Temporarily public so we can override the caller from within the tests. Will find a better way in the future!
   */
  public @Inject Caller<DataSyncService> dataSyncService;

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

  /**
   * The entity comparator that detects differences between desired state and expected state.
   */
  private EntityComparator entityComparator;

  /**
   * The attribute accessor for reading and writing attribute values in JPA
   * entities. Since this is a client-side class, this is always an
   * ErraiAttributeAccessor.
   */
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

  /**
   * Performs a "cold" synchronization on the results of the given query with
   * the given parameters. After a successful synchronization, both the expected
   * state and desired state entity managers will yield the same results as the
   * server-side entity manager does for the given query with the given set of
   * parameters.
   *
   * @param queryName
   *          The name of a JPA named query. This query must be defined in a
   *          {@link NamedQuery} annotation that is visible to both the client
   *          and server applications. This usually means it is defined on an
   *          entity in the <code>shared</code> package.
   * @param queryResultType
   *          The result type returned by the query. Must be a JPA entity type
   *          known to both the client and server applications.
   * @param queryParams
   *          The name-value pairs to use for filling in the named parameters in
   *          the query.
   * @param onCompletion
   *          Called when the data sync response has been received from the
   *          server, and the sync response operations have been applied to the
   *          expected state and desired state entity managers. Must not be
   *          null.
   * @param onError
   *          Called when the data sync fails: either because the remote service
   *          threw an exception, or because of a communication error. Can be
   *          null, in which case the default error handling for the
   *          {@code Caller<DataSyncService>} will apply.
   */
  public <E> void coldSync(
          String queryName, Class<E> queryResultType, Map<String, Object> queryParams,
          final RemoteCallback<List<SyncResponse<E>>> onCompletion,
          final ErrorCallback<?> onError) {
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

    System.out.println("Sending sync requests:");
    for (SyncRequestOperation<?> sro : syncRequests) {
      System.out.println("   " + sro);
    }

    final SyncableDataSet<E> syncSet = SyncableDataSet.from(queryName, queryResultType, queryParams);
    RemoteCallback<List<SyncResponse<E>>> onSuccess = new RemoteCallback<List<SyncResponse<E>>>() {
      @Override
      public void callback(List<SyncResponse<E>> syncResponse) {
        applyResults(syncResponse);
        onCompletion.callback(syncResponse);
      }
    };

    if (onError != null) {
      dataSyncService.call(onSuccess, onError).coldSync(syncSet, syncRequests);
    }
    else {
      dataSyncService.call(onSuccess).coldSync(syncSet, syncRequests);
    }
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
      System.out.println("SSS Handling Sync response " + response.getClass());
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
        System.out.println("    -> Delete " + dr.getEntity());
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
        System.out.println("    -> ID Change from " + icr.getOldId() + " to " + icr.getEntity());
        E newEntity = icr.getEntity();
        newEntity = expectedStateEm.merge(newEntity);

        @SuppressWarnings("unchecked")
        ErraiEntityType<E> entityType = desiredStateEm.getMetamodel().entity((Class<E>) newEntity.getClass());
        ErraiSingularAttribute<? super E, Object> idAttr = entityType.getId(Object.class);
        changeId(entityType, icr.getOldId(), idAttr.get(newEntity));
        desiredStateEm.merge(newEntity);
      }
      else if (response instanceof NewRemoteEntityResponse) {
        NewRemoteEntityResponse<E> nrer = (NewRemoteEntityResponse<E>) response;
        System.out.println("    -> New " + nrer.getEntity());

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
        System.out.println("    -> Update " + ur.getEntity());
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

  /**
   * Clears all expected state and actual state data (essentially wiping out all
   * localStorage data that Errai cares about). This operation will destroy any
   * local data that has not been synced to the server, including data that the
   * sync manager was never told about.
   */
  public void clear() {
    desiredStateEm.removeAll();
    expectedStateEm.removeAll();
  }
}
