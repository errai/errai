package org.jboss.errai.bus.client;

import org.jboss.errai.bus.client.api.builder.AbstractMessageBuilder;
import org.jboss.errai.bus.client.api.builder.AbstractRemoteCallBuilder;
import org.jboss.errai.bus.client.api.builder.MessageBuildSubject;

/**
 * The MessageBuilder API provides a fluent method of building Messages.
 */
public class MessageBuilder {
    private static MessageProvider provider = new MessageProvider() {
        @Override
        public Message get() {
            return JSONMessage.create();
        }
    };

    /**
     * Create a new message.
     * @return
     */
    public static MessageBuildSubject createMessage() {
        return new AbstractMessageBuilder(provider.get()).start();
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

    public static void setProvider(MessageProvider provider) {
        MessageBuilder.provider = provider;
    }
}
