/*
 * Copyright 2010 JBoss, a divison Red Hat, Inc
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

import static org.jboss.errai.bus.client.api.base.MessageBuilder.createConversation;
import static org.jboss.errai.bus.client.api.base.MessageBuilder.createMessage;

import org.jboss.errai.bus.client.ErraiBus;
import org.jboss.errai.bus.client.api.ErrorCallback;
import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.api.builder.MessageBuildParms;
import org.jboss.errai.bus.client.protocols.MessageParts;

/**
 * The default error callback implementation, used when {@link MessageBuildParms#defaultErrorHandling()} was invoked. 
 * 
 * @author Mike Brock
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class DefaultErrorCallback implements ErrorCallback {
  public static final DefaultErrorCallback INSTANCE = new DefaultErrorCallback();
  public static final String CLIENT_ERROR_SUBJECT = "ClientBusErrors";

  public boolean error(Message message, final Throwable e) {
    if (e != null) {
      StringBuilder a =
          new StringBuilder("<br/>").append(e.getClass().getName()).append(": ").append(e.getMessage()).append("<br/>");

      // Let's build-up the stacktrace.
      boolean first = true;
      for (StackTraceElement sel : e.getStackTrace()) {
        a.append(first ? "" : "&nbsp;&nbsp;").append(sel.toString()).append("<br/>");
        first = false;
      }

      // And add the entire causal chain.
      Throwable t = e;
      while ((t = t.getCause()) != null) {
        first = true;
        a.append("Caused by:<br/>");
        for (StackTraceElement sel : t.getStackTrace()) {
          a.append(first ? "" : "&nbsp;&nbsp;").append(sel.toString()).append("<br/>");
          first = false;
        }
      }

      if (message == null) {
        createMessage(CLIENT_ERROR_SUBJECT)
            .with(MessageParts.ErrorMessage, e.getMessage())
            .with("AdditionalDetails", a.toString())
            .with(MessageParts.Throwable, e)
            .noErrorHandling().sendNowWith(ErraiBus.get());
      }
      else {
        createConversation(message)
            .toSubject(CLIENT_ERROR_SUBJECT)
            .with(MessageParts.ErrorMessage, e.getMessage())
            .with("AdditionalDetails", a.toString())
            .noErrorHandling().reply();
      }
    }
    else {
      if (message == null) {
        createMessage(CLIENT_ERROR_SUBJECT)
            .with("ErrorMessage", "Null exception reference")
            .with("AdditionalDetails", "No additional details")
            .noErrorHandling().sendNowWith(ErraiBus.get());
      }
      else {
        createConversation(message)
            .toSubject(CLIENT_ERROR_SUBJECT)
            .with("ErrorMessage", "Null exception reference")
            .with("AdditionalDetails", "No additional details")
            .noErrorHandling().reply();
      }
    }

    return false;
  }
}
