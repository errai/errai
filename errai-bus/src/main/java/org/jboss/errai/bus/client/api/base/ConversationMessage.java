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
import org.jboss.errai.common.client.protocols.MessageParts;

/**
 * A message that is automatically routed back to the originating bus of a
 * reference message (usually called the <i>incoming message</i>). When a
 * ConversationMessage is dispatched, it will only be delivered to the bus that
 * the incoming message originated from.
 * <p/>
 * <p/>
 * <pre>
 * public class SomeService implements MessageCallback {
 *   public void callback(CommandMessage message) {
 *     ConversationMessage.create(message) // create a ConversationMessage that
 *                                         // references the incoming message
 *             .setSubject(&quot;ClientService&quot;) // specify the service on the sending
 *                                          // bus that should receive the message
 *             .set(&quot;Text&quot;, &quot;Hello, World!&quot;).sendNowWith(messageBusInstance); // send
 *                                                                            // the
 *                                                                            // message
 *   }
 * }
 * </pre>
 * <p/>
 * It is possible for a message sender to specify a
 * {@link org.jboss.errai.common.client.protocols.MessageParts#ReplyTo ReplyTo}
 * message component, which by default will be used to route the message. We
 * refer to this as a <em>sender-driven conversation</em> as opposed to a
 * <em>receiver-driven conversation</em> which is demonstrated in the code
 * example above.
 */
public class ConversationMessage extends CommandMessage {

  /**
   * Creates a new ConversationMessage using an incoming message as a reference.
   *
   * @param inReplyTo
   *     the incoming message.
   *
   * @return a ConversationMessage that will be routed to the MessageBus that
   *         sent the {@code inReplyTo} message.
   */
  public static ConversationMessage create(Message inReplyTo) {
    return new ConversationMessage(inReplyTo);
  }

  /**
   * Creates a new ConversationMessage with the specified command type and
   * reference message.
   *
   * @param commandType
   *     The command type for this message. Command is an optional
   *     extension for creating services that can respond to different
   *     specific commands. Must not be null.
   * @param inReplyTo
   *     the incoming message. Must not be null.
   */
  public static ConversationMessage create(Enum<?> commandType, Message inReplyTo) {
    ConversationMessage message = new ConversationMessage(inReplyTo);
    message.command(commandType.name());
    return message;
  }

  /**
   * Creates a new ConversationMessage with the specified command type and
   * reference message.
   *
   * @param commandType
   *     The command type for this message. Command is an optional
   *     extension for creating services that can respond to different
   *     specific commands. Must not be null.
   * @param inReplyTo
   *     the incoming message. Must not be null.
   */
  public static ConversationMessage create(String commandType, Message inReplyTo) {
    ConversationMessage message = new ConversationMessage(inReplyTo);
    message.command(commandType);
    return message;
  }

  private ConversationMessage(Message inReplyTo) {
    super();
    if (inReplyTo.hasResource("Session")) {
      setResource("Session", inReplyTo.getResource(Object.class, "Session"));
    }
    if (inReplyTo.hasPart(MessageParts.ReplyTo)) {
      set(MessageParts.ToSubject, inReplyTo.get(String.class, MessageParts.ReplyTo));
    }

    if (!inReplyTo.hasResource("Session") && !inReplyTo.hasPart(MessageParts.ReplyTo)) {
      throw new RuntimeException(
          "cannot have a conversation. there is no session data or ReplyTo field." +
              " Are you sure you referenced an incoming message?");
    }
  }
}
