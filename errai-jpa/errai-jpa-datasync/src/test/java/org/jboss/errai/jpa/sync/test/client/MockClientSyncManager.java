/*
 * Copyright (C) 2014 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.jpa.sync.test.client;

import java.util.Collections;
import java.util.Map;

import org.jboss.errai.common.client.api.ErrorCallback;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.jpa.sync.client.local.ClientSyncManager;

/**
 * A subclass of the real ClientSyncManager that overrides
 * {@link #coldSync(String, Class, Map, RemoteCallback, ErrorCallback)} so it
 * just captures its arguments and does not attempt to communicate with the
 * server.
 * <p>
 * Note that this bean is denylisted in ErraiApp.properties so that it does not
 * get injected in place of the real ClientSyncManager.
 */
@SuppressWarnings("rawtypes")
public class MockClientSyncManager extends ClientSyncManager {
  private int coldSyncCallCount;

  @SuppressWarnings("unchecked")
  @Override
  public void coldSync(String queryName, Class queryResultType, Map queryParams, RemoteCallback onCompletion,  ErrorCallback onError) {
    coldSyncCallCount++;
    onCompletion.callback(Collections.emptyList());
  }

  public int getColdSyncCallCount() {
    return coldSyncCallCount;
  }
}
