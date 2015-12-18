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

import java.util.List;

import org.jboss.errai.bus.server.annotations.Remote;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.jpa.sync.client.local.ClientSyncManager;
import org.jboss.errai.jpa.sync.server.DataSyncServiceImpl;

/**
 * An Errai RPC service which is called by ClientSyncManager when it wishes to
 * synchronize JPA data sets data between itself and the server. Applications
 * that use Errai JPA DataSync are required to implement this interface on the
 * server side; usually with an EJB which injects the correct
 * {@code @PersistenceContext} for synchronizing with.
 */
@Remote
public interface DataSyncService {

  /**
   * Performs a cold synchronization, usually by delegating to
   * {@link DataSyncServiceImpl#coldSync(SyncableDataSet, List)}. This method is
   * not normally invoked directly by application code; rather, application code calls
   * {@link ClientSyncManager#coldSync(String, Class, java.util.Map, org.jboss.errai.common.client.api.RemoteCallback, org.jboss.errai.common.client.api.ErrorCallback)}
   * and that method calls this one via an Errai RPC {@link Caller}.
   *
   * @param dataSet
   *          The SyncableDataSet to synchronize between client and server.
   * @param remoteResults
   *          The list of SyncRequestOperations produced by the
   *          ClientSyncManager for the given dataset.
   * @return the list of sync responses produced by the server-side DataSyncServiceImpl.
   */
  <X> List<SyncResponse<X>> coldSync(SyncableDataSet<X> dataSet, List<SyncRequestOperation<X>> remoteResults);
}
