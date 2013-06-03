package org.jboss.errai.jpa.sync.client.shared;

import java.util.List;

import org.jboss.errai.bus.server.annotations.Remote;

@Remote
public interface DataSyncService {

  <X> List<SyncResponse<X>> coldSync(SyncableDataSet<X> dataSet, List<SyncRequestOperation<X>> remoteResults);
}
