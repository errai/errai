package org.jboss.errai.jpa.sync.client.shared;

import org.jboss.errai.common.client.api.annotations.MapsTo;
import org.jboss.errai.common.client.api.annotations.Portable;

/**
 * A SyncResponse that represents conflicting state due to a change that happened on the responding end.
 *
 * @author Jonathan Fuerth <jfuerth@redhat.com>
 * @param <X> The entity type that was used in the corresponding SyncRequest
 */
@Portable
public class ConflictResponse<X> extends SyncResponse<X> {

  private final X expected;
  private final X actualNew;
  private final X requestedNew;

  public ConflictResponse(
          @MapsTo("expected") X expected,
          @MapsTo("actualNew") X actualNew,
          @MapsTo("requestedNew") X requestedNew) {
    this.expected = expected;
    this.actualNew = actualNew;
    this.requestedNew = requestedNew;
  }

  public X getExpected() {
    return expected;
  }

  public X getActualNew() {
    return actualNew;
  }

  public X getRequestedNew() {
    return requestedNew;
  }

  @Override
  public String toString() {
    return "Conflict: expected=" + expected + " actual=" + actualNew + " requested=" + requestedNew;
  }
}
