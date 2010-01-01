package org.jboss.errai.bus.server.util;

import org.jboss.errai.bus.client.ConversationMessage;
import org.jboss.errai.bus.client.Message;
import org.jboss.errai.bus.client.MessageBus;
import org.mvel2.util.StringAppender;

public class ErrorHelper {
    public static final void sendClientError(MessageBus bus, Message message, String errorMessage, Throwable e) {
        StringAppender a = new StringAppender("<br/>").append(e.getClass().getName() + ": " + e.getMessage()).append("<br/>");

        boolean first = true;
        for (StackTraceElement sel : e.getStackTrace()) {
            a.append(first ? "" : "&nbsp;&nbsp;").append(sel.toString()).append("<br/>");
            first = false;
        }

        if (e.getCause() != null) {
            first = false;
            a.append("Caused by:<br/>");
            for (StackTraceElement sel : e.getCause().getStackTrace()) {
                a.append(first ? "" : "&nbsp;&nbsp;").append(sel.toString()).append("<br/>");
                first = false;
            }
        }

        sendClientError(bus, message, errorMessage, a.toString());
    }

    public static final void sendClientError(MessageBus bus, Message message, String errorMessage, String additionalDetails) {
        ConversationMessage.create(message)
                .toSubject("ClientBusErrors")
                .set("ErrorMessage", errorMessage)
                .set("AdditionalDetails", additionalDetails)
                .sendNowWith(bus);
    }
}
