/*
 * Copyright 2013 JBoss, by Red Hat, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.otec.client;

import org.jboss.errai.otec.client.operation.OTOperation;

/**
 * @author Mike Brock
 */
public class OTQueuedOperation implements Comparable<OTQueuedOperation> {
  private final int version;
  private final OTOperation operation;
  private final String peerId;
  private final int entityId;

  public OTQueuedOperation(int version, final OTOperation operation, final String peerId, final int entityId) {
    this.version = version;
    this.operation = operation;
    this.peerId = peerId;
    this.entityId = entityId;
  }

  public int getVersion() {
    return version;
  }

  public OTOperation getOperation() {
    return operation;
  }

  public String getPeerId() {
    return peerId;
  }

  public int getEntityId() {
    return entityId;
  }

  @Override
  public int compareTo(OTQueuedOperation o) {
    return version - o.getVersion();
  }
}
