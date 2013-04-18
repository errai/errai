package org.jboss.errai.jpa.sync.client.local;

import java.util.List;

import org.jboss.errai.common.client.api.Assert;
import org.jboss.errai.jpa.sync.client.shared.SyncResponse;

/**
 * A client-local event that's fired every time a data sync operation has completed.
 *
 * @author Jonathan Fuerth <jfuerth@redhat.com>
 */
public class DataSyncCompleteEvent<E> {

  private final boolean successful;
  private final List<SyncResponse<E>> syncResponse;

  public DataSyncCompleteEvent(boolean successful, List<SyncResponse<E>> syncResponse) {
    this.successful = successful;
    this.syncResponse = Assert.notNull(syncResponse);
  }

  public boolean isSuccessful() {
    return successful;
  }

  public List<SyncResponse<E>> getSyncResponse() {
    return syncResponse;
  }
}
