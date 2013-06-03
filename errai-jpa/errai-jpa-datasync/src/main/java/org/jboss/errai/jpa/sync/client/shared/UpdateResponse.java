package org.jboss.errai.jpa.sync.client.shared;

import org.jboss.errai.common.client.api.Assert;
import org.jboss.errai.common.client.api.annotations.Portable;
import org.jboss.errai.marshalling.client.api.annotations.MapsTo;

/**
 * Sync response that indicates that the given entity has been modified since
 * the last sync response.
 *
 * @author Jonathan Fuerth <jfuerth@redhat.com>
 *
 * @param <X>
 *          the entity type
 */
@Portable
public class UpdateResponse<X> extends SyncResponse<X> {

  private final X entity;

  public UpdateResponse(@MapsTo("entity") X entity) {
    this.entity = Assert.notNull(entity);
  }

  /**
   * Returns the entity that was updated.
   *
   * @return
   */
  public X getEntity() {
    return entity;
  }

  @Override
  public String toString() {
    return "Updated: " + entity;
  }
}
