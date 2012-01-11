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

import org.jboss.errai.bus.client.ErraiBus;
import org.jboss.errai.bus.client.api.Consumer;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.framework.RequestDispatcher;
import org.jboss.errai.common.client.protocols.MessageParts;

final class ErraiMessageConsumer<T> implements Consumer<T> {
  private RequestDispatcher requestDispatcher = ErraiBus.getDispatcher();
  private final Class<T> valueType;
  private final String toSubject;
  private final String replyTo;

  private ErraiMessageConsumer(String toSubject, String replyTo, Class<T> valueType) {
    this.toSubject = toSubject;
    this.replyTo = replyTo;
    this.valueType = valueType;
  }

  public static <U> ErraiMessageConsumer<U> of(String toSubject, String replyTo, Class<U> valueType) {
    return new ErraiMessageConsumer<U>(toSubject, replyTo, valueType);
  }

  @Override
  public void consume(Object value) {
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

  public void setRequestDispatcher(RequestDispatcher dispatcher) {
    if (requestDispatcher != null) throw new IllegalStateException("requestDispatcher is already set");
    this.requestDispatcher = dispatcher;
  }

  @Override
  public Class<T> getValueType() {
    return valueType;
  }

  @Override
  public <U> Consumer<U> select(String subjectName, String replyTo, Class<U> valueType) {
    return of(subjectName, replyTo, valueType);
  }
}