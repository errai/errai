package org.jboss.errai.jpa.sync.client.shared;

import java.util.List;

import org.jboss.errai.bus.server.annotations.Remote;

@Remote
public interface DataSyncService {

  //<X> List<SyncResponse<X>> coldSync(SyncableDataSet<X> dataSet, List<SyncRequestOperation<X>> remoteResults);
  // XXX would like to use the above signature, but RPC proxy generator doesn't support type variables (yet)
  List<SyncResponse> coldSync(SyncableDataSet dataSet, List<SyncRequestOperation> remoteResults);
}
