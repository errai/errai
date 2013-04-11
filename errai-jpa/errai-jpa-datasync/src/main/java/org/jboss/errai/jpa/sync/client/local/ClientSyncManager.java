package org.jboss.errai.jpa.sync.client.local;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.TypedQuery;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.SingularAttribute;

import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.jpa.client.local.ErraiEntityManager;
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

  @Inject Caller<DataSyncService> dataSyncService;

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

  public <E> void startSyncing(String queryName, Class<E> queryResultType, Map<String, Object> queryParams) {
    final TypedQuery<E> query = desiredStateEm.createNamedQuery(queryName, queryResultType);
    for (Map.Entry<String, Object> param : queryParams.entrySet()) {
      query.setParameter(param.getKey(), param.getValue());
    }
    final Map<String, Object> fetchOptions = new HashMap<String, Object>();
    final List<SyncRequestOperation<E>> localResults = new ArrayList<SyncRequestOperation<E>>();

    for (E desiredState : query.getResultList()) {
      Key<E, ?> key = desiredStateEm.keyFor(desiredState);
      E expectedState = expectedStateEm.find(key, fetchOptions);
      if (expectedState == null) {
        localResults.add(SyncRequestOperation.created(desiredState));
      }
      else if (entityComparator.isDifferent(desiredState, expectedState)) {
        localResults.add(SyncRequestOperation.updated(desiredState, expectedState));
      }
      else /* desiredState == expectedState */ {
        localResults.add(SyncRequestOperation.unchanged(expectedState));
      }
    }

    // TODO find locally deleted entities!

    //final SyncableDataSet<E> syncSet = new SyncableDataSet<E>(queryName, queryResultType, queryParams);
    final SyncableDataSet syncSet = new SyncableDataSet(queryName, queryResultType, queryParams);
    final List rawLocalResults = localResults;
    dataSyncService.call(new RemoteCallback<List<SyncResponse>>() {
      @Override
      public void callback(List<SyncResponse> syncResponse) {
        List rawSyncResponse = syncResponse;
        applyResults(rawSyncResponse);
      }
    }).coldSync(syncSet, rawLocalResults);
  }

  private <E> void applyResults(List<SyncResponse<E>> syncResponses) {
    // XXX could we factor this decision tree into apply() methods on the sync response objects?
    for (SyncResponse<E> response : syncResponses) {
      if (response instanceof ConflictResponse) {
        ConflictResponse<E> cr = (ConflictResponse<E>) response;
        desiredStateEm.getTransaction().setRollbackOnly();
        expectedStateEm.merge(cr.getActualNew());
        throw new RuntimeException("TODO: notify conflict listeners");
      }
      else if (response instanceof DeleteResponse) {
        DeleteResponse<E> dr = (DeleteResponse<E>) response;
        expectedStateEm.remove(dr.getEntity());
        desiredStateEm.remove(dr.getEntity());
      }
      else if (response instanceof IdChangeResponse) {
        IdChangeResponse<E> icr = (IdChangeResponse<E>) response;
        expectedStateEm.persist(icr.getEntity());

        // XXX the following is probably better handled internally by the ErraiEntityManager

        @SuppressWarnings("unchecked")
        Class<E> class1 = (Class<E>) icr.getEntity().getClass();
        E oldEntity = desiredStateEm.find(class1, icr.getOldId());
        desiredStateEm.remove(oldEntity);

        @SuppressWarnings("unchecked")
        EntityType<E> type = desiredStateEm.getMetamodel().entity((Class<E>) oldEntity.getClass());
        SingularAttribute<? super E, ?> idAttr = type.getId(type.getIdType().getJavaType());
        copyAttribtue(idAttr, icr.getEntity(), oldEntity);

        Map<String, Object> hints = new HashMap<String, Object>();
        desiredStateEm.persist(oldEntity);
      }
      else if (response instanceof NewRemoteEntityResponse) {
        NewRemoteEntityResponse<E> nrer = (NewRemoteEntityResponse<E>) response;
        expectedStateEm.persist(nrer.getEntity());
        desiredStateEm.persist(nrer.getEntity());
      }
      else if (response instanceof UpdateResponse) {
        UpdateResponse<E> ur = (UpdateResponse<E>) response;
        expectedStateEm.merge(ur.getEntity());
        expectedStateEm.merge(ur.getEntity());
      }
      else {
        throw new RuntimeException("Unexpected kind of sync response: " + response);
      }
    }
  }

  private <X, Y> void copyAttribtue(SingularAttribute<X, Y> attr, X fromEntity, X toEntity) {
    Y newValue = attributeAccessor.get(attr, fromEntity);
    attributeAccessor.set(attr, toEntity, newValue);
  }
}
