/*
 * Copyright 2011 JBoss, by Red Hat, Inc
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

package org.jboss.errai.bus.client.api.base;

import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.api.MessageCallback;
import org.jboss.errai.bus.client.framework.MessageBus;
import org.jboss.errai.bus.client.framework.RoutingFlag;
import org.jboss.errai.common.client.protocols.MessageParts;

public class ConversationHelper {
  private static final String RES_NAME = "MessageReplyCallback";

  public static void makeConversational(Message message, MessageCallback callback) {
    message.setResource(RES_NAME, callback);
    message.setFlag(RoutingFlag.Conversational);
  }

  public static void createConversationService(MessageBus bus, Message m) {
    if (m.isFlagSet(RoutingFlag.Conversational)) {
      final String replyService = m.getSubject() + ":RespondTo:" + count();
      bus.subscribe(replyService, m.getResource(MessageCallback.class, RES_NAME));
      bus.subscribe(replyService, new ServiceCanceller(replyService, bus));
      m.set(MessageParts.ReplyTo, replyService);
    }
  }

  static int counter = 0;

  static int count() {
    if (++counter > 1000) {
      return counter = 0;
    }
    return counter;
  }
}
