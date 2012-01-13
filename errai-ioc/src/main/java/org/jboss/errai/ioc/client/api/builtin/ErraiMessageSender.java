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

package org.jboss.errai.ioc.client.api.builtin;

import org.jboss.errai.ioc.client.api.Sender;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.framework.RequestDispatcher;
import org.jboss.errai.common.client.protocols.MessageParts;

public final class ErraiMessageSender<T> implements Sender<T> {
  private final RequestDispatcher requestDispatcher;
  private final Class<T> valueType;
  private final String toSubject;
  private final String replyTo;

  private ErraiMessageSender(String toSubject, String replyTo, Class<T> valueType, RequestDispatcher dispatcher) {
    this.toSubject = toSubject;
    this.replyTo = replyTo;
    this.valueType = valueType;
    this.requestDispatcher = dispatcher;
  }

  public static <U> ErraiMessageSender<U> of(String toSubject, String replyTo, Class<U> valueType,
                                               RequestDispatcher dispatcher) {
    return new ErraiMessageSender<U>(toSubject, replyTo, valueType, dispatcher);
  }

  @Override
  public void send(T value) {
    if (replyTo != null) {
      MessageBuilder.createMessage()
          .toSubject(toSubject)
          .with(MessageParts.ReplyTo, replyTo)
          .with(MessageParts.Value, value)
          .done().sendNowWith(requestDispatcher);
    }
    else {
      MessageBuilder.createMessage()
          .toSubject(toSubject)
          .with(MessageParts.Value, value)
          .done().sendNowWith(requestDispatcher);
    }
  }
}