package org.jboss.errai.jpa.sync.client.shared;

/**
 * A SyncResponse that represents conflicting state due to a change that happened on the responding end.
 *
 * @author Jonathan Fuerth <jfuerth@redhat.com>
 * @param <X> The entity type that was used in the corresponding SyncRequest
 */
public class ConflictResponse<X> extends SyncResponse<X> {

  private final X expected;
  private final X actualNew;
  private final X requestedNew;

  public ConflictResponse(
          X expected,
          X actualNew,
          X requestedNew) {
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
}
