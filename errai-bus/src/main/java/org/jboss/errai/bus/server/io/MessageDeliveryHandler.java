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

package org.jboss.errai.bus.server.io;

import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.bus.server.api.MessageQueue;

import java.io.IOException;

/**
 * The <tt>MessageDeliveryHandler</tt> defines the behavior of the message bus relative to the transport layer.
 *
 * @author Mike Brock
 */
public interface MessageDeliveryHandler {
  /**
   * This method is responsible for delivering a message into the transport layer. The contract between the message
   * bus and the transport layer is completely governed by the implementation of the method.
   *
   * @param queue
   *     the {@link MessageQueue} to deliver from.
   * @param message
   *     the {@link Message} to deliver.
   *
   * @return true if the message was successfully accepted.
   *
   * @throws IOException
   *     an IOException may be thrown if there is a problem interacting with the underlying transport.
   */
  public boolean deliver(MessageQueue queue, Message message) throws IOException;

  /**
   * Sends a NOOP (No-Operation) to the remote connected
   * @param queue
   * @throws IOException
   */
  public void noop(MessageQueue queue) throws IOException;
}
