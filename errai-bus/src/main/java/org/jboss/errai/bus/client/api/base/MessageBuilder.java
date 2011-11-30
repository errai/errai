/*
 * Copyright 2011 JBoss, by Red Hat, Inc
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

import org.jboss.errai.bus.client.api.ErrorCallback;
import org.jboss.errai.bus.client.api.HasEncoded;
import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.api.RemoteCallback;
import org.jboss.errai.bus.client.api.builder.AbstractMessageBuilder;
import org.jboss.errai.bus.client.api.builder.AbstractRemoteCallBuilder;
import org.jboss.errai.bus.client.api.builder.ConversationMessageWrapper;
import org.jboss.errai.bus.client.api.builder.HasEncodedConvMessageWrapper;
import org.jboss.errai.bus.client.api.builder.MessageBuildCommand;
import org.jboss.errai.bus.client.api.builder.MessageBuildSendableWithReply;
import org.jboss.errai.bus.client.api.builder.MessageBuildSubject;
import org.jboss.errai.bus.client.api.builder.MessageReplySendable;
import org.jboss.errai.bus.client.framework.MessageProvider;

/**
 * The MessageBuilder API provides a fluent method of building Messages.
 *
 * @author Mike Brock
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
    return new AbstractMessageBuilder(provider.get()).start();
  }
  
  /**
   * Create a new message for the provided subject.
   *
   * @param subject - the subject the message should be sent to
   * @return a <tt>MessageBuildSubject</tt> which essentially is a <tt>Message</tt>, but ensures that the user
   *         constructs messages properly
   */
  @SuppressWarnings({"unchecked"})
  public static MessageBuildCommand<MessageBuildSendableWithReply> createMessage(String subject) {
    return new AbstractMessageBuilder(provider.get()).start().toSubject(subject);
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
    }
    else {
      return new AbstractMessageBuilder<MessageReplySendable>(new ConversationMessageWrapper(message, newMessage)).start();
    }
  }
  
  /**
   * Create a conversational messages for the provided subject
   *
   * @param message - reference message to create conversation from
   * @param subject - the subject the message should be sent to
   * @return a <tt>MessageBuildSubject</tt> which essentially is a <tt>Message</tt>, but ensures that the user
   *         constructs messages properly
   */
  @SuppressWarnings({"unchecked"})
  public static MessageBuildCommand<MessageReplySendable> createConversation(Message message, String subject) {
    Message newMessage = provider.get();
    if (newMessage instanceof HasEncoded) {
      return new AbstractMessageBuilder<MessageReplySendable>(new HasEncodedConvMessageWrapper(message, newMessage))
        .start()
        .toSubject(subject);
    }
    else {
      return new AbstractMessageBuilder<MessageReplySendable>(new ConversationMessageWrapper(message, newMessage))
        .start()
      . toSubject(subject);
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
   * Creates an RPC call.
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
   * Creates an RPC call with an ErrorCallback.
   *
   * @param callback -
   * @param errorcallback
   * @param service  -
   * @param <T>      -
   * @param <R>      -
   * @return -
   */
  public static <R, T> T createCall(RemoteCallback<R> callback, ErrorCallback errorCallback, Class<T> service) {
    return new AbstractRemoteCallBuilder(CommandMessage.create()).call(callback, errorCallback, service);
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
