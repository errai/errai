package org.jboss.errai.jpa.sync.client.shared;

import org.jboss.errai.common.client.api.Assert;
import org.jboss.errai.common.client.api.annotations.MapsTo;
import org.jboss.errai.common.client.api.annotations.Portable;

/**
 * Sync response that indicates the deletion of an entity.
 *
 * @author Jonathan Fuerth <jfuerth@redhat.com>
 *
 * @param <X> the entity type
 */
@Portable
public class DeleteResponse<X> extends SyncResponse<X> {

  private final X entity;

  public DeleteResponse(@MapsTo("entity") X entity) {
    this.entity = Assert.notNull(entity);
  }

  /**
   * Returns the entity that was deleted.
   *
   * @return
   */
  public X getEntity() {
    return entity;
  }

  @Override
  public String toString() {
    return "Delete " + entity;
  }
}
