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

package org.jboss.errai.otec.mutation;


public interface OTEngine {
  public String getId();

  public ReceiveHandler getReceiveHandler(String peerId, Integer entityId);

  public InitialStateReceiveHandler getInitialStateReceiveHandler(String peerId, Integer entityId);

  public void syncRemoteEntity(String peerId, Integer entityId, EntitySyncCompletionCallback callback);

  public void applyOperationsLocally(OTOperationList operationList);

  public void notifyOperations(OTOperationList operationList);

  public OTEntityState getEntityStateSpace();

  public OTOperationsFactory getOperationsFactory();

  public void registerPeer(OTPeer peer);
}
