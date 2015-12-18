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

package org.jboss.errai.bus.client.api.base;

import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.bus.client.api.messaging.MessageCallback;
import org.jboss.errai.bus.client.api.messaging.MessageBus;
import org.jboss.errai.bus.client.api.RoutingFlag;
import org.jboss.errai.common.client.protocols.MessageParts;

/**
 * Utilities for creating conversational messages on the bus.
 *
 * @author Mike Brock
 * @author Jonathan Fuerth <jfuerth@redhat.com>
 * @author Christian Sadilek <csadilek@redhat.com>
 * @author Max Barkley <mbarkley@redhat.com>
 */
class ConversationHelper {
  private static final String RES_NAME = "MessageReplyCallback";

  private static volatile int counter = 0;
  private static final Object counterLock = new Object();

  static void makeConversational(Message message, MessageCallback callback) {
    message.setResource(RES_NAME, callback);
    message.setFlag(RoutingFlag.Conversational);
  }

  static void createConversationService(MessageBus bus, Message m) {
    if (m.isFlagSet(RoutingFlag.Conversational)) {
      final String replyService = m.getSubject() + ":" + count() + ":RespondTo:RPC";
      bus.subscribe(replyService, new ServiceCanceller(bus.subscribe(replyService, m.getResource(MessageCallback.class, RES_NAME))));

      m.set(MessageParts.ReplyTo, replyService);
    }
  }
  
  static boolean hasConversationCallback(Message message) {
    return message.hasResource(RES_NAME);
  }

  static int count() {
    synchronized (counterLock) {
      return ++counter;
    }
  }
}
