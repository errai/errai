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


// TODO create tests for usage of this interface

/**
 * Callback interface for observing every message on the bus before that message
 * is sent to any {@link MessageCallback message callbacks}. A MessageListener
 * can cancel delivery of a message.
 * <p>
 * If your goal is to implement security policies around messages to a
 * particular subject, {@link org.jboss.errai.bus.client.api.BooleanRoutingRule} is a better choice than
 * MessageListener.
 */
public interface MessageListener {

  /**
   * Called by the Message Bus before the given message is routed to any
   * MessageListeners.
   *
   * @param message
   *          The message being processed by the bus.
   * @return true if delivery of {@code message} should be allowed to proceed;
   *         false if {@code message} must not be delivered.
   */
  public boolean handleMessage(Message message);
}
