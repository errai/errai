package org.jboss.errai.bus.client;

import org.jboss.errai.bus.client.api.builder.AbstractMessageBuilder;

/**
 * The MessageBuilder API provides a fluent method of building Messages.
 */
public class MessageBuilder {
    /**
     * Create a new message.
     * @return
     */
    public static AbstractMessageBuilder createMessage() {
        return new AbstractMessageBuilder(CommandMessage.create());
    }

    /**
     * Create a conversational messages
     * @param message
     * @return
     */
    public static AbstractMessageBuilder createConversation(Message message) {
        return new AbstractMessageBuilder(ConversationMessage.create(message));
    }

}
