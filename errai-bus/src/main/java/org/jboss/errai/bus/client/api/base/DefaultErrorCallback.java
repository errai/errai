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

import static org.jboss.errai.bus.client.api.base.MessageBuilder.createConversation;
import static org.jboss.errai.bus.client.api.base.MessageBuilder.createMessage;

import org.jboss.errai.bus.client.ErraiBus;
import org.jboss.errai.common.client.api.ErrorCallback;
import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.bus.client.api.builder.MessageBuildParms;
import org.jboss.errai.common.client.protocols.MessageParts;

/**
 * The default error callback implementation, used when {@link MessageBuildParms#defaultErrorHandling()} was invoked
 * (which is the default when there is no explicit mention of error handling to the MessageBuilder).
 *
 * @author Mike Brock
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class DefaultErrorCallback implements ErrorCallback<Message> {
  public static final DefaultErrorCallback INSTANCE = new DefaultErrorCallback();
  public static final String CLIENT_ERROR_SUBJECT = "ClientBusErrors";

  /**
   * Constructs an error message and puts it on the client message bus with the subject {@link #CLIENT_ERROR_SUBJECT}.
   * The error message is constructed with the following parts:
   * <dl>
   * <dt>Subject <dd>{@link #CLIENT_ERROR_SUBJECT}
   * <dt>ErrorMessage <dd>{@code e.getMessage()} if e != null; otherwise, {@code "Null exception reference"}
   * <dt>AdditionalDetails <dd>HTML marked-up stack trace of {@code e} if e != null; otherwise, {@code "No additional details"}
   * <dt>Throwable <dd>A marshalled representation of the exception object {@code e} if e != null; otherwise, this message part is omitted.
   * </dl>
   */
  @Override
  public boolean error(Message message, final Throwable e) {
    try {
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
                  .with(MessageParts.AdditionalDetails, a.toString())
                  .with(MessageParts.Throwable, e)
                  .noErrorHandling().sendNowWith(ErraiBus.get());
        }
        else {
          createConversation(message)
                  .toSubject(CLIENT_ERROR_SUBJECT)
                  .with(MessageParts.ErrorMessage, e.getMessage())
                  .with(MessageParts.AdditionalDetails, a.toString())
                  .with(MessageParts.Throwable, e)
                  .noErrorHandling().reply();
        }
      }
      else {
        if (message == null) {
          createMessage(CLIENT_ERROR_SUBJECT)
                  .with(MessageParts.ErrorMessage, "Null exception reference")
                  .with(MessageParts.AdditionalDetails, "No additional details")
                  .noErrorHandling().sendNowWith(ErraiBus.get());
        }
        else {
          createConversation(message)
                  .toSubject(CLIENT_ERROR_SUBJECT)
                  .with(MessageParts.ErrorMessage, "Null exception reference")
                  .with(MessageParts.AdditionalDetails, "No additional details")
                  .noErrorHandling().reply();
        }
      }

    }
    catch (Throwable t) {
      t.printStackTrace();
      throw new RuntimeException("could not dispatch wrapped exception to error handler", e);
    }
    return false;
  }
}
