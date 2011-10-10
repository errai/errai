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

package org.jboss.errai.bus.client.util;

import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.api.base.MessageDeliveryFailure;
import org.jboss.errai.bus.client.framework.MessageBus;
import org.jboss.errai.bus.client.protocols.BusCommands;
import org.jboss.errai.bus.client.protocols.MessageParts;

/**
 * The <tt>ErrorHelper</tt> class facilitates handling and sending error messages to the correct place
 */
public class ErrorHelper {

  /**
   * Creates the stacktrace for the error message and sends it via conversation to the <tt>ClientBusErrors</tt>
   * subject
   *
   * @param bus          - the <tt>MessageBus</tt> that has received the <tt>message</tt> and <tt>errorMessage</tt>
   * @param message      - the message that has encountered the error
   * @param errorMessage - the error message produced
   * @param e            - the exception received
   */
  public static void sendClientError(MessageBus bus, Message message, String errorMessage, Throwable e) {
    if ("ClientBusErrors".equals(message.getSubject())) {
      /**
       * Trying to send an error to the client when the client obviously can't receive it!
       */

      System.err.println("*** An error occured that could not be delivered to the client.");
      System.err.println("Error Message: " + message.get(String.class, "ErrorMessage"));
      System.err.println("Details      : " + message.get(String.class, "AdditionalDetails").replaceAll("<br/>", "\n").replaceAll("&nbsp;", " "));
      //  System.err.println("---");
      //  e.printStackTrace(System.err);
    }
    else {

      if (e != null) {
        StringBuilder a = new StringBuilder("<tt><br/>").append(e.getClass().getName()).append(": ").append(e.getMessage()).append("<br/>");

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
          if (msg == null) msg = "<No Message>";

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

  /**
   * Sends the error message via conversation to the <tt>ClientBusErrors</tt> subject
   *
   * @param bus               - the <tt>MessageBus</tt> that has received the <tt>message</tt> and <tt>errorMessage</tt>
   * @param message           - the message that has encountered the error
   * @param errorMessage      - the error message produced
   * @param additionalDetails - the stacktrace represented as a <tt>String</tt>
   */
  public static void sendClientError(MessageBus bus, Message message, String errorMessage, String additionalDetails) {
    if ("ClientBusErrors".equals(message.getSubject())) {
      /**
       * Trying to send an error to the client when the client obviously can't receive it!
       */

      System.err.println("*** An error occured that could not be delivered to the client.");
      System.err.println("Error Message: " + message.get(String.class, "ErrorMessage"));
      System.err.println("Details      : " + message.get(String.class, "AdditionalDetails").replaceAll("<br/>", "\n").replaceAll("&nbsp;", " "));

    }
    else {

      MessageBuilder.createConversation(message)
              .toSubject("ClientBusErrors")
              .with("ErrorMessage", errorMessage)
              .with("AdditionalDetails", additionalDetails)
              .with(MessageParts.ErrorTo, message.get(String.class, MessageParts.ErrorTo))
              .with(MessageParts.Throwable, message.getResource(Object.class, "Exception"))
              .noErrorHandling().sendNowWith(bus);
    }
  }

  public static void sendClientError(MessageBus bus, String queueId, String errorMessage, String additionalDetails) {
    MessageBuilder.createMessage()
            .toSubject("ClientBusErrors")
            .with("ErrorMessage", errorMessage)
            .with("AdditionalDetails", additionalDetails)
            .with(MessageParts.SessionID, queueId)
            .noErrorHandling().sendNowWith(bus);
  }


  /**
   * Sends a disconnect command message to the client bus
   *
   * @param bus     - the bus responsible for sending messages for the server
   * @param message - the message that has encountered the error
   */
  public static void disconnectRemoteBus(MessageBus bus, Message message) {
    MessageBuilder.createConversation(message)
            .toSubject("ClientBus")
            .command(BusCommands.Disconnect)
            .noErrorHandling().sendNowWith(bus);
  }


  /**
   * Handles the failed delivery of a message, and sends the error to the appropriate place
   *
   * @param bus          - the <tt>MessageBus</tt> that has received the <tt>message</tt> and <tt>errorMessage</tt>
   * @param message      - the message that has encountered the error
   * @param errorMessage - the error message produced
   * @param e            - the exception received
   * @param disconnect   - true if the bus should be disconnected after the error has been sent
   */
  public static void handleMessageDeliveryFailure(MessageBus bus, Message message, String errorMessage, Throwable e, boolean disconnect) {
    try {
      if (message.getErrorCallback() != null) {
        if (!message.getErrorCallback().error(message, e)) {
          return;
        }
      }

      sendClientError(bus, message, errorMessage, e);

 //     if (e != null) throw new MessageDeliveryFailure(e);
    }
    finally {
      if (disconnect) disconnectRemoteBus(bus, message);
    }
  }


}
