package org.jboss.errai.bus.server.util;

import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.api.MessageBus;
import org.jboss.errai.bus.client.protocols.BusCommands;
import org.jboss.errai.bus.server.MessageDeliveryFailure;
import org.mvel2.util.StringAppender;

/**
 * The <tt>ErrorHelper</tt> class facilitates handling and sending error messages to the correct place
 */
public class ErrorHelper {

    /**
     * Creates the stacktrace for the error message and sends it via conversation to the <tt>ClientBusErrors</tt>
     * subject
     *
     * @param bus - the <tt>MessageBus</tt> that has received the <tt>message</tt> and <tt>errorMessage</tt>
     * @param message - the message that has encountered the error
     * @param errorMessage - the error message produced
     * @param e - the exception received
     */
    public static void sendClientError(MessageBus bus, Message message, String errorMessage, Throwable e) {

        if (e != null) {
            StringAppender a = new StringAppender("<br/>").append(e.getClass().getName() + ": " + e.getMessage()).append("<br/>");

            // Let's build-up the stacktrace.
            boolean first = true;
            for (StackTraceElement sel : e.getStackTrace()) {
                a.append(first ? "" : "&nbsp;&nbsp;").append(sel.toString()).append("<br/>");
                first = false;
            }

            // And add the entire causal chain.
            while ((e = e.getCause()) != null) {
                first = true;
                a.append("Caused by:<br/>");
                for (StackTraceElement sel : e.getStackTrace()) {
                    a.append(first ? "" : "&nbsp;&nbsp;").append(sel.toString()).append("<br/>");
                    first = false;
                }
            }
            sendClientError(bus, message, errorMessage, a.toString());

        }
        else {
            sendClientError(bus, message, errorMessage, "No additional details.");
        }
    }

    /**
     * Sends the error message via conversation to the <tt>ClientBusErrors</tt> subject
     *
     * @param bus - the <tt>MessageBus</tt> that has received the <tt>message</tt> and <tt>errorMessage</tt>
     * @param message - the message that has encountered the error
     * @param errorMessage - the error message produced
     * @param additionalDetails - the stacktrace represented as a <tt>String</tt>
     */
    public static void sendClientError(MessageBus bus, Message message, String errorMessage, String additionalDetails) {
        MessageBuilder.createConversation(message)
                .toSubject("ClientBusErrors").signalling()
                .with("ErrorMessage", errorMessage)
                .with("AdditionalDetails", additionalDetails)
                .noErrorHandling().sendNowWith(bus);

    }

    /**
     * Sends a disconnect command message to the client bus
     *
     * @param bus - the bus responsible for sending messages for the server
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
     * @param bus - the <tt>MessageBus</tt> that has received the <tt>message</tt> and <tt>errorMessage</tt>
     * @param message - the message that has encountered the error
     * @param errorMessage - the error message produced
     * @param e - the exception received
     * @param disconnect - true if the bus should be disconnected after the error has been sent
     */
    public static void handleMessageDeliveryFailure(MessageBus bus, Message message, String errorMessage, Throwable e, boolean disconnect) {
        try {
            if (message.getErrorCallback() != null) {
                if (!message.getErrorCallback().error(message, e)) {
                    return;
                }
            }
            sendClientError(bus, message, errorMessage, e);

            if (e != null) throw new MessageDeliveryFailure(e);
        }
        finally {
            if (disconnect) disconnectRemoteBus(bus, message);
        }
    }


}
