/*
 * Copyright (C) 2013 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.bus.server;

import org.jboss.errai.bus.client.api.messaging.Message;

/**
* @author Christian Sadilek
* @author Mike Brock
*/
public class ClusterWaitEntry {
  final long time;
  final Message message;
  final Runnable timeoutCallback;

  public ClusterWaitEntry(final long time, final Message message, final Runnable timeoutCallback) {
    this.timeoutCallback = timeoutCallback;
    this.message = message;
    this.time = time;
  }

  public long getTime() {
    return time;
  }

  public Message getMessage() {
    return message;
  }

  public boolean isStale() {
    return (System.currentTimeMillis() - time) > (10 * 1000);
  }

  public void notifyTimeout() {
    if (timeoutCallback != null) {
      timeoutCallback.run();
    }
  }
}
