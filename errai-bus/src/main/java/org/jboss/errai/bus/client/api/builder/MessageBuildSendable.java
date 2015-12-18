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

package org.jboss.errai.bus.client.api.builder;

import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.api.messaging.MessageBus;

/**
 * A template for sending a message. This ensures that the message is constructed properly.
 * <p>
 * Part of the fluent API centered around {@link MessageBuilder}.
 */
public interface MessageBuildSendable extends Sendable {

  /**
   * Sends the message with the specified <tt>MessageBus</tt>
   * 
   * @param viaThis
   *          the message bus to send the message with
   */
  public void sendNowWith(MessageBus viaThis);

  /**
   * Sends the message with the specified <tt>MessageBus</tt>
   * 
   * @param viaThis
   *          the message bus to send the message with
   * @param fireMessageListener
   *          true if the message listeners should be notified
   */
  public void sendNowWith(MessageBus viaThis, boolean fireMessageListener);

  /**
   * Sends the message globally with the specified <tt>MessageBus</tt>
   * 
   * @param viaThis
   *          the message bus to send the message with
   */
  public void sendGlobalWith(MessageBus viaThis);
}
