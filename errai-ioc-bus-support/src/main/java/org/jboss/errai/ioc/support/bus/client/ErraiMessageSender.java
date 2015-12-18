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

package org.jboss.errai.ioc.support.bus.client;

import org.jboss.errai.common.client.api.ErrorCallback;
import org.jboss.errai.bus.client.api.messaging.MessageCallback;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.api.messaging.MessageBus;
import org.jboss.errai.common.client.protocols.MessageParts;

public final class ErraiMessageSender<T> implements Sender<T> {
  private final MessageBus messageBus;
  private final String toSubject;
  private final String replyTo;

  private ErraiMessageSender(String toSubject, String replyTo, MessageBus messageBus) {
    this.toSubject = toSubject;
    this.replyTo = replyTo;
    this.messageBus = messageBus;
  }

  public static <U> ErraiMessageSender<U> of(String toSubject, String replyTo,
                                             MessageBus messageBus) {
    return new ErraiMessageSender<U>(toSubject, replyTo, messageBus);
  }

  @Override
  public void send(T value) {
    if (replyTo != null) {
      MessageBuilder.createMessage()
              .toSubject(toSubject)
              .with(MessageParts.ReplyTo, replyTo)
              .with(MessageParts.Value, value)
              .done().sendNowWith(messageBus);
    }
    else {
      MessageBuilder.createMessage()
              .toSubject(toSubject)
              .with(MessageParts.Value, value)
              .done().sendNowWith(messageBus);
    }
  }

  @Override
  public void send(T value, ErrorCallback errorCallback) {
    if (replyTo != null) {
      MessageBuilder.createMessage()
              .toSubject(toSubject)
              .with(MessageParts.ReplyTo, replyTo)
              .with(MessageParts.Value, value)
              .errorsHandledBy(errorCallback)
              .sendNowWith(messageBus);
    }
    else {
      MessageBuilder.createMessage()
              .toSubject(toSubject)
              .with(MessageParts.Value, value)
              .errorsHandledBy(errorCallback)
              .sendNowWith(messageBus);
    }
  }

  public void send(T value, MessageCallback replyTo) {
    MessageBuilder.createMessage()
        .toSubject(toSubject)
        .withValue(value)
        .done().repliesTo(replyTo).sendNowWith(messageBus);
  }

  @Override
  public void send(T value, MessageCallback replyTo, ErrorCallback errorCallback) {
    MessageBuilder.createMessage()
        .toSubject(toSubject)
        .withValue(value)
        .errorsHandledBy(errorCallback)
        .repliesTo(replyTo).sendNowWith(messageBus);
  }
}
