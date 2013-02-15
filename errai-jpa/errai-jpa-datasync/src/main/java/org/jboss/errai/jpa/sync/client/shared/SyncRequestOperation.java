package org.jboss.errai.jpa.sync.client.shared;

import org.jboss.errai.common.client.api.annotations.Portable;
import org.jboss.errai.marshalling.client.api.annotations.MapsTo;

@Portable
public class SyncRequestOperation<X> {

  private final Type type;
  private final X newState;
  private final X expectedState;

  public enum Type {
    /**
     * Indicates an entity instance that was created without the knowledge of
     * the remote system the change list is being sent to.
     */
    NEW,

    /**
     * Indicates an entity instance whose identity is already known to the
     * system the change list is being sent to. This newState's non-identity state
     * may have changed.
     */
    EXISTING
  }

  public SyncRequestOperation(
          @MapsTo("type") Type type,
          @MapsTo("newState") X newState,
          @MapsTo("expectedState") X expectedState) {
    this.type = type;
    this.newState = newState;
    this.expectedState = expectedState;
  }

  public Type getType() {
    return type;
  }

  public X getEntity() {
    return newState;
  }

  public X getExpectedState() {
    return expectedState;
  }
}
