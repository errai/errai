package org.jboss.errai.bus.client;

import org.jboss.errai.bus.client.api.builder.AbstractMessageBuilder;
import org.jboss.errai.bus.client.api.builder.AbstractRemoteCallBuilder;
import org.jboss.errai.bus.client.api.builder.MessageBuildSubject;

/**
 * The MessageBuilder API provides a fluent method of building Messages.
 */
public class MessageBuilder {
    /**
     * Create a new message.
     * @return
     */
    public static MessageBuildSubject createMessage() {
        return new AbstractMessageBuilder(CommandMessage.create()).start();
    }

    /**
     * Create a conversational messages
     * @param message
     * @return
     */
    public static MessageBuildSubject createConversation(Message message) {
        return new AbstractMessageBuilder(ConversationMessage.create(message)).start();
    }

    public static AbstractRemoteCallBuilder createCall() {
        return new AbstractRemoteCallBuilder(CommandMessage.create());
    }
}
