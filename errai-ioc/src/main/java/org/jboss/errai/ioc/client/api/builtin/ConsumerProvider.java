/*
 * Copyright 2011 JBoss, a divison Red Hat, Inc
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
import org.jboss.errai.bus.client.protocols.MessageParts;
import org.jboss.errai.ioc.client.api.ContextualTypeProvider;
import org.jboss.errai.ioc.client.api.IOCProvider;

import java.lang.annotation.Annotation;

/**
 * @author Mike Brock .
 */
@IOCProvider
public class ConsumerProvider implements ContextualTypeProvider<Consumer<?>> {
  public Consumer<?> provide(Class[] typeargs, Annotation[] qualifiers) {
    return new Consumer<Object>() {
      private RequestDispatcher requestDispatcher = ErraiBus.getDispatcher();
      private String toSubject = null;
      private String replyTo = null;

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

      public void setToSubject(String subjectName) {
        if (this.toSubject != null) throw new IllegalStateException("toSubject is already set");
        this.toSubject = subjectName;
      }

      public void setReplyTo(String subjectName) {
        if (this.replyTo != null) throw new IllegalStateException("replyTo is already set");
        this.replyTo = subjectName;
      }
    };
  }
}
