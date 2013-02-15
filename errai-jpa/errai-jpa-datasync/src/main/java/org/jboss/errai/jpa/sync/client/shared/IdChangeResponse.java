package org.jboss.errai.jpa.sync.client.shared;

import org.jboss.errai.common.client.api.Assert;
import org.jboss.errai.marshalling.client.api.annotations.MapsTo;

public class IdChangeResponse<X> extends SyncResponse<X> {

  private X entity;
  private Object oldId;

  public IdChangeResponse(
          @MapsTo("oldId") Object oldId,
          @MapsTo("entity") X entity) {
    this.entity = Assert.notNull(entity);
    this.oldId = Assert.notNull(oldId);
  }

  public X getEntity() {
    return entity;
  }

  public Object getOldId() {
    return oldId;
  }
}
