package org.jboss.errai.jpa.sync.client.shared;

import org.jboss.errai.common.client.api.Assert;
import org.jboss.errai.common.client.api.annotations.MapsTo;
import org.jboss.errai.common.client.api.annotations.Portable;

@Portable
public class NewRemoteEntityResponse<X> extends SyncResponse<X> {

  private final X entity;

  public NewRemoteEntityResponse(@MapsTo("entity") X entity) {
    this.entity = Assert.notNull(entity);
  }

  public X getEntity() {
    return entity;
  }

  @Override
  public String toString() {
    return "New Entity: " + entity;
  }
}
