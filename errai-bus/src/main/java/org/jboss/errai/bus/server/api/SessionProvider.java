/*
 * Copyright (C) 2012 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.bus.server.api;

import javax.servlet.http.HttpSession;

import org.jboss.errai.bus.client.api.QueueSession;

/**
 * Allows retrieval and creation of {@link QueueSession}s for a specific type of communication channel.
 */
public interface SessionProvider<T> {

  /**
   * Looks up or creates the {@link QueueSession} that identifies a communication channel with a specific queue in a
   * specific remote Errai Bus.
   * 
   * @param externSessRef
   *          the session object (for example, an {@link HttpSession}) that identifies a communications link to a remote
   *          Errai Bus.
   * @param remoteQueueId
   *          the ID of the queue in the remote bus
   * @return the QueueSession that identifies the communication link to the given remote queue in the given remote bus.
   *         If none already exists, one is created.
   */
  public QueueSession createOrGetSession(T externSessRef, String remoteQueueId);
}
