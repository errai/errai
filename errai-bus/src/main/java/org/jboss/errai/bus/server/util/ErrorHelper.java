package org.jboss.errai.bus.server.util;

import org.jboss.errai.bus.client.ConversationMessage;
import org.jboss.errai.bus.client.Message;
import org.jboss.errai.bus.client.MessageBuilder;
import org.jboss.errai.bus.client.MessageBus;
import org.jboss.errai.bus.client.protocols.BusCommands;
import org.jboss.errai.bus.server.MessageDeliveryFailure;
import org.mvel2.util.StringAppender;

import static org.jboss.errai.bus.client.MessageBuilder.createMessage;

public class ErrorHelper {
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


    public static void sendClientError(MessageBus bus, Message message, String errorMessage, String additionalDetails) {
        MessageBuilder.createConversation(message)
                .toSubject("ClientBusErrors").signalling()
                .with("ErrorMessage", errorMessage)
                .with("AdditionalDetails", additionalDetails)
                .noErrorHandling().sendNowWith(bus);

    }

    public static void disconnectRemoteBus(MessageBus bus, Message message) {
        MessageBuilder.createConversation(message)
                .toSubject("ClientBus")
                .command(BusCommands.Disconnect)
                .noErrorHandling().sendNowWith(bus);
    }

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
