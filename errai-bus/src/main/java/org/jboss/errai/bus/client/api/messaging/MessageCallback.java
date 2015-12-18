/*
 * Copyright (C) 2011 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.bus.client.api.messaging;

import org.jboss.errai.bus.server.annotations.Service;

/**
 * Callback interface for receiving messages on the bus. To receive messages on
 * the message bus, create an implementation of this interface, then either
 * annotate it with {@link Service @Service} or register it with the bus via a
 * call to {@link MessageBus#subscribe(String, MessageCallback)} or
 * {@link MessageBus#subscribeLocal(String, MessageCallback)}.
 * <p>
 * The subject of messages that a particular MessageCallback is interested in is
 * specified when it is registered with the bus, however it is common practice
 * for a MessageCallback class to have the same name as the bus subject it
 * receives messages for. The {@link Service @Service} annotation makes this
 * approach quite natural.
 *
 * @see Service
 * @see MessageBus
 */
public interface MessageCallback {

  /**
   * Called by the Message Bus every time it processes a message with the
   * subject this callback is registered for.
   *
   * @param message
   *          The message on the bus. Avoid making changes to this object,
   *          because it will continue to be reused by the framework and the
   *          same Message instance will be passed to other callbacks.
   */
  public void callback(Message message);
}
