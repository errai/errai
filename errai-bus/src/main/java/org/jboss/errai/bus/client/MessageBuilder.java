package org.jboss.errai.bus.client;

import org.jboss.errai.bus.client.api.builder.*;

/**
 * The MessageBuilder API provides a fluent method of building Messages.
 */
public class MessageBuilder {
    private static MessageProvider provider = new MessageProvider() {
        public Message get() {
            return JSONMessage.create();
        }
    };

    /**
     * Create a new message.
     *
     * @return a <tt>MessageBuildSubject</tt> which essentially is a <tt>Message</tt>, but ensures that the user
     *         constructs messages properly
     */
    public static MessageBuildSubject createMessage() {
        return new AbstractMessageBuilder(provider.get()).start();
    }

    /**
     * Create a conversational messages
     *
     * @param message - reference message to create conversation from
     * @return a <tt>MessageBuildSubject</tt> which essentially is a <tt>Message</tt>, but ensures that the user
     *         constructs messages properly
     */
    public static MessageBuildSubject createConversation(Message message) {
        Message newMessage = provider.get();
        if (newMessage instanceof HasEncoded) {
            return new AbstractMessageBuilder(new HasEncodedConvMessageWrapper(message, newMessage)).start();
        }
        else {
            return new AbstractMessageBuilder(new ConversationMessageWrapper(message, newMessage)).start();
        }
    }

    /**
     * Creates an <tt>AbstractRemoteCallBuilder</tt> to construct a call
     *
     * @return an instance of <tt>AbstractRemoteCallBuilder</tt>
     */
    public static AbstractRemoteCallBuilder createCall() {
        return new AbstractRemoteCallBuilder(CommandMessage.create());
    }

    /**
     * Sets the message provide for this instance of <tt>MessageBuilder</tt>
     *
     * @param provider - to set this' provider to
     */
    public static void setProvider(MessageProvider provider) {
        MessageBuilder.provider = provider;
    }
}
