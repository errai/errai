package org.jboss.errai.bus.client.util;

import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.api.base.MessageBuilder;

/**
 * User: christopherbrock
 * Date: 18-Jul-2010
 * Time: 10:44:22 AM
 */
public class SimpleMessage {
    public static void send(Message message, String msgText) {
        MessageBuilder.createConversation(message)
                .subjectProvided()
                .with("Message", msgText).done().reply();
    }

    public static String get(Message message) {
        return message.get(String.class, "Message");
    }
}
