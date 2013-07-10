package org.jboss.errai.jpa.sync.client.shared;

import java.util.List;

import org.jboss.errai.bus.server.annotations.Remote;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.jpa.sync.client.local.ClientSyncManager;
import org.jboss.errai.jpa.sync.server.DataSyncServiceImpl;

/**
 * An Errai RPC service which is called by ClientSyncManager when it wishes to
 * synchronize JPA data sets data between itself and the server. Applications
 * that use Errai JPA DataSync are required to implement this interface on the
 * server side; usually with an EJB which injects the correct
 * {@code @PersistenceContext} for synchronizing with.
 */
@Remote
public interface DataSyncService {

  /**
   * Performs a cold synchronization, usually by delegating to
   * {@link DataSyncServiceImpl#coldSync(SyncableDataSet, List)}. This method is
   * not normally invoked directly by application code; rather, application code calls
   * {@link ClientSyncManager#coldSync(String, Class, java.util.Map, org.jboss.errai.common.client.api.RemoteCallback, org.jboss.errai.common.client.api.ErrorCallback)}
   * and that method calls this one via an Errai RPC {@link Caller}.
   *
   * @param dataSet
   *          The SyncableDataSet to synchronize between client and server.
   * @param remoteResults
   *          The list of SyncRequestOperations produced by the
   *          ClientSyncManager for the given dataset.
   * @return the list of sync responses produced by the server-side DataSyncServiceImpl.
   */
  <X> List<SyncResponse<X>> coldSync(SyncableDataSet<X> dataSet, List<SyncRequestOperation<X>> remoteResults);
}
