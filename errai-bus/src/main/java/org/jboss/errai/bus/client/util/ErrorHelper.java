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

package org.jboss.errai.bus.client.util;

import org.jboss.errai.bus.client.api.RoutingFlag;
import org.jboss.errai.bus.client.api.base.DefaultErrorCallback;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.api.base.MessageCallbackFailure;
import org.jboss.errai.bus.client.api.base.MessageDeliveryFailure;
import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.bus.client.api.messaging.MessageBus;
import org.jboss.errai.bus.client.protocols.BusCommand;
import org.jboss.errai.common.client.protocols.MessageParts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The <tt>ErrorHelper</tt> class facilitates handling and sending error messages to the correct
 * place
 */
public class ErrorHelper {

  private static final Logger logger = LoggerFactory.getLogger(ErrorHelper.class);

  /**
   * Creates the stacktrace for the error message and sends it via conversation to the
   * <tt>ClientBusErrors</tt> subject
   *
   * @param bus
   *          - the <tt>MessageBus</tt> that has received the <tt>message</tt> and
   *          <tt>errorMessage</tt>
   * @param message
   *          - the message that has encountered the error
   * @param errorMessage
   *          - the error message produced
   * @param e
   *          - the exception received
   */
  public static void sendClientError(MessageBus bus, Message message, String errorMessage, Throwable e) {
    if (DefaultErrorCallback.CLIENT_ERROR_SUBJECT.equals(message.getSubject())) {
      /**
       * Trying to send an error to the client when the client obviously can't receive it!
       */

      logger.error("*** An error occured that could not be delivered to the client.");
      logger.error("Error Message: " + message.get(String.class, "ErrorMessage"));
      logger.error("Details      : "
          + message.get(String.class, "AdditionalDetails").replaceAll("<br/>", "\n").replaceAll("&nbsp;", " "));
    }
    else {

      if (e != null) {
        /*
         * Some code calling this method properly set the value, where others incorrectly set it to
         * null (because they do not check the cause). This check is here for the case that the
         * exception has not been correctly set so that it is still marshaled to the client.
         */
        if (!message.hasResource("Exception") || message.getResource(Object.class, "Exception") == null) {
          message.setResource("Exception", maybeUnwrap(e));
        }

        StringBuilder a =
            new StringBuilder("<tt><br/>").append(e.getClass().getName()).append(": ").append(e.getMessage()).append(
                "<br/>");

        String str;
        // Let's build-up the stacktrace.
        for (StackTraceElement sel : e.getStackTrace()) {
          str = sel.toString();
          if (str != null && str.trim().length() != 0)
            a.append("&nbsp;&nbsp;&nbsp;&nbsp;at ").append(str).append("<br/>");
        }

        // And add the entire causal chain.
        while ((e = e.getCause()) != null) {
          String msg = e.getMessage();
          if (msg == null)
            msg = "<No Message>";

          a.append("Caused by: ").append(e.getClass().getName()).append(": ").append(msg.trim()).append("<br/>");
          for (StackTraceElement sel : e.getStackTrace()) {
            str = sel.toString();
            if (str != null && str.trim().length() != 0)
              a.append("&nbsp;&nbsp;&nbsp;&nbsp;at ").append(str).append("<br/>");
          }
        }

        sendClientError(bus, message, errorMessage, a.append("</tt>").toString());

      }
      else {
        sendClientError(bus, message, errorMessage, "No additional details.");
      }
    }
  }

  private static Throwable maybeUnwrap(Throwable e) {
    while ((e instanceof MessageDeliveryFailure || e instanceof MessageCallbackFailure) && e.getCause() != null) {
      e = e.getCause();
    }

    return e;
  }

  /**
   * Sends the error message via conversation to the <tt>ClientBusErrors</tt> subject
   *
   * @param bus
   *          - the <tt>MessageBus</tt> that has received the <tt>message</tt> and
   *          <tt>errorMessage</tt>
   * @param message
   *          - the message that has encountered the error
   * @param errorMessage
   *          - the error message produced
   * @param additionalDetails
   *          - the stacktrace represented as a <tt>String</tt>
   */
  public static void sendClientError(MessageBus bus, Message message, String errorMessage, String additionalDetails) {
    try {
      if (DefaultErrorCallback.CLIENT_ERROR_SUBJECT.equals(message.getSubject())) {
        /**
         * Trying to send an error to the client when the client obviously can't receive it!
         */

        logger.error("*** An error occured that could not be delivered to the client.");
        logger.error("Error Message: " + message.get(String.class, "ErrorMessage"));
        logger.error("Details      : "
            + message.get(String.class, "AdditionalDetails").replaceAll("<br/>", "\n").replaceAll("&nbsp;", " "));

      }
      else  {
        final String errorTo = message.get(String.class, MessageParts.ErrorTo);
        if (errorTo != null) {
          final String subject = errorTo;

          MessageBuilder.createConversation(message)
          .toSubject(subject)
          .with("ErrorMessage", errorMessage)
          .with("AdditionalDetails", additionalDetails)
          .with(MessageParts.ErrorTo, message.get(String.class, MessageParts.ErrorTo))
          .with(MessageParts.Throwable, message.getResource(Object.class, "Exception"))
          .noErrorHandling().sendNowWith(bus);
        }
      }
    }
    catch (RuntimeException e) {
      // note: this is handled this way, because this is shared server and client code.
      if (e.getClass().getName().equals("org.jboss.errai.bus.server.QueueUnavailableException")) {
        // ignore.
      }
      throw e;
    }
  }

  public static void sendClientError(MessageBus bus, String queueId, String errorMessage, String additionalDetails) {
    try {
      MessageBuilder.createMessage()
          .toSubject(DefaultErrorCallback.CLIENT_ERROR_SUBJECT)
          .with("ErrorMessage", errorMessage)
          .with("AdditionalDetails", additionalDetails)
          .with(MessageParts.SessionID, queueId)
          .flag(RoutingFlag.NonGlobalRouting)
          .noErrorHandling().sendNowWith(bus);
    }
    catch (RuntimeException e) {
      // note: this is handled this way, because this is shared server and client code.
      if (e.getClass().getName().equals("org.jboss.errai.bus.server.QueueUnavailableException")) {
        // ignore.
      }
      throw e;
    }
  }

  /**
   * Sends a disconnect command message to the client bus
   *
   * @param bus
   *          - the bus responsible for sending messages for the server
   * @param message
   *          - the message that has encountered the error
   */
  public static void disconnectRemoteBus(MessageBus bus, Message message) {
    MessageBuilder.createConversation(message)
        .toSubject("ClientBus")
        .command(BusCommand.Disconnect)
        .noErrorHandling().sendNowWith(bus);
  }

  /**
   * Handles the failed delivery of a message, and sends the error to the appropriate place
   *
   * @param bus
   *          - the <tt>MessageBus</tt> that has received the <tt>message</tt> and
   *          <tt>errorMessage</tt>
   * @param message
   *          - the message that has encountered the error
   * @param errorMessage
   *          - the error message produced
   * @param e
   *          - the exception received
   * @param disconnect
   *          - true if the bus should be disconnected after the error has been sent
   */
  public static void handleMessageDeliveryFailure(MessageBus bus, Message message, String errorMessage, Throwable e,
      boolean disconnect) {
    String logMessage =
        "*** Message delivery failure ***" +
            "\nBus: " + bus.toString() +
            "\nMessage: " + message +
            "\nerrorMessage: " + errorMessage +
            "\nexception: " + e +
            "\ndisconnect: " + disconnect;

    if (!(e instanceof MessageDeliveryFailure && ((MessageDeliveryFailure) e).isRpcEndpointException())) {
      if (e == null) {
        logger.error(logMessage);
      }
      else {
        logger.error(logMessage, e);
      }
    }

    try {
      if (message.getErrorCallback() != null) {
        if (!message.getErrorCallback().error(message, e)) {
          return;
        }
      }

      sendClientError(bus, message, errorMessage, e);

      // if (e != null) throw new MessageDeliveryFailure(e);
    }
    finally {
      if (disconnect)
        disconnectRemoteBus(bus, message);
    }
  }

}
