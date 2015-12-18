/*
 * Copyright (C) 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.jpa.sync.client.shared;

import org.jboss.errai.common.client.api.annotations.MapsTo;
import org.jboss.errai.common.client.api.annotations.Portable;

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
     * Indicates that an entity instance that the remote side already knows
     * about has been deleted by the requesting side.
     */
    DELETED,

    /**
     * Indicates an entity instance whose identity is already known to the
     * system the change list is being sent to. This newState's non-identity state
     * may have changed.
     */
    UPDATED,

    /**
     * Indicates an entity instance that has not changed since the last sync request.
     */
    UNCHANGED
  }

  public SyncRequestOperation(
          @MapsTo("type") Type type,
          @MapsTo("newState") X newState,
          @MapsTo("expectedState") X expectedState) {
    this.type = type;
    this.newState = newState;
    this.expectedState = expectedState;
  }

  public static <X> SyncRequestOperation<X> created(X newState) {
    // XXX would be better to use a type hierarchy of SyncRequestOperations than to say knownState is null
    return new SyncRequestOperation<X>(Type.NEW, newState, null);
  }

  public static <X> SyncRequestOperation<X> updated(X newState, X expectedState) {
    return new SyncRequestOperation<X>(Type.UPDATED, newState, expectedState);
  }

  public static <X> SyncRequestOperation<X> unchanged(X knownState) {
    // XXX would be better to use a type hierarchy of SyncRequestOperations than to say newState is null
    return new SyncRequestOperation<X>(Type.UNCHANGED, null, knownState);
  }

  public static <X> SyncRequestOperation<X> deleted(X knownState) {
    // XXX would be better to use a type hierarchy of SyncRequestOperations than to say newState is null
    return new SyncRequestOperation<X>(Type.DELETED, null, knownState);
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

  @Override
  public String toString() {
    return type + " newState: " + newState + "; expectedState: " + expectedState;
  }
}
