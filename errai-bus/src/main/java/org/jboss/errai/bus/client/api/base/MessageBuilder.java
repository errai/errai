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

package org.jboss.errai.bus.client.api.base;

import org.jboss.errai.bus.client.api.HasEncoded;
import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.api.RemoteCallback;
import org.jboss.errai.bus.client.api.builder.*;
import org.jboss.errai.bus.client.framework.MessageProvider;

/**
 * The MessageBuilder API provides a fluent method of building Messages.
 */
public class MessageBuilder {
    private static MessageProvider provider = JSONMessage.PROVIDER;

    /**
     * Create a new message.
     *
     * @return a <tt>MessageBuildSubject</tt> which essentially is a <tt>Message</tt>, but ensures that the user
     *         constructs messages properly
     */
    @SuppressWarnings({"unchecked"})
    public static MessageBuildSubject<MessageBuildSendableWithReply> createMessage() {
        //    Message message = provider.get();
        return new AbstractMessageBuilder(provider.get()).start();
    }

    /**
     * Create a conversational messages
     *
     * @param message - reference message to create conversation from
     * @return a <tt>MessageBuildSubject</tt> which essentially is a <tt>Message</tt>, but ensures that the user
     *         constructs messages properly
     */
    @SuppressWarnings({"unchecked"})
    public static MessageBuildSubject<MessageReplySendable> createConversation(Message message) {
        Message newMessage = provider.get();
        if (newMessage instanceof HasEncoded) {
            return new AbstractMessageBuilder<MessageReplySendable>(new HasEncodedConvMessageWrapper(message, newMessage)).start();
        } else {
            return new AbstractMessageBuilder<MessageReplySendable>(new ConversationMessageWrapper(message, newMessage)).start();
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
     * Creates a RPC call.
     *
     * @param callback -
     * @param service  -
     * @param <T>      -
     * @param <R>      -
     * @return -
     */
    public static <R, T> T createCall(RemoteCallback<R> callback, Class<T> service) {
        return new AbstractRemoteCallBuilder(CommandMessage.create()).call(callback, service);
    }

    /**
     * Sets the message provide for this instance of <tt>MessageBuilder</tt>
     *
     * @param provider - to set this' provider to
     */
    public static void setMessageProvider(MessageProvider provider) {
        MessageBuilder.provider = provider;
    }

    public static MessageProvider getMessageProvider() {
        return provider;
    }
}
