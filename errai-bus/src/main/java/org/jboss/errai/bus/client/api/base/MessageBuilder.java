/*
 * Copyright (C) 2011 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.bus.client.api.base;

import org.jboss.errai.bus.client.api.BusErrorCallback;
import org.jboss.errai.bus.client.api.HasEncoded;
import org.jboss.errai.bus.client.api.RoutingFlag;
import org.jboss.errai.bus.client.api.builder.DefaultRemoteCallBuilder;
import org.jboss.errai.bus.client.api.builder.MessageBuildCommand;
import org.jboss.errai.bus.client.api.builder.MessageBuildSendableWithReply;
import org.jboss.errai.bus.client.api.builder.MessageBuildSubject;
import org.jboss.errai.bus.client.api.builder.MessageReplySendable;
import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.bus.client.api.messaging.MessageProvider;
import org.jboss.errai.common.client.api.RemoteCallback;

/**
 * The MessageBuilder API provides a fluent method of building Messages.
 *
 * <h3>Example Message:</h3>
 *
 * <pre>
 * Message m = MessageBuilder
 *     .createMessage()
 *     .toSubject(&quot;TheMessageSubject&quot;)
 *     .withValue(&quot;Hello everyone&quot;).done();
 * </pre>
 * <p>
 * You can transmit a message using the the <tt>sendNowWith(RequestDispatcher)</tt> method by providing an instance of
 * {@link org.jboss.errai.bus.client.api.messaging.MessageBus}.
 * <p>
 * Messages can be constructed using user-defined standard protocols through the use of enumerations. Both
 * <tt>commandType</tt> and message parts can be defined through the use of enumerations. This helps create
 * strongly-defined protocols for communicating with services. For instance:
 *
 * <pre>
 * public enum LoginParts {
 *    Username, Password
 * }
 * </pre>
 *
 * .. and ..
 *
 * <pre>
 * public enum LoginCommands {
 *    Login, Logout
 * }
 * </pre>
 *
 * A service can then use these enumerations to build and decode messages. For example:
 *
 * <pre>
 * MessageBuilder
 *     .createMessage()
 *     .toSubject(&quot;LoginService&quot;)
 *     .command(LoginCommands.Login)
 *     .set(LoginParts.Username, &quot;foo&quot;)
 *     .set(LoginParts.Password, &quot;bar&quot;)
 *     .sendNowWith(busInstance);
 * </pre>
 *
 * Messages may contain serialized objects that are annotated with
 * {@link org.jboss.errai.common.client.api.annotations.Portable} and can be marshalled by the built-in Errai
 * marshallers or by user-provided marshallers that have been registered with the system.
 *
 * @author Mike Brock
 */
public class MessageBuilder {
  private static MessageProvider provider = new MessageProvider() {
    @Override
    public Message get() {
      return CommandMessage.create();
    }
  };

  /**
   * Creates a new message.
   *
   * @return a <tt>MessageBuildSubject</tt> which essentially is a <tt>Message</tt>, but ensures that the user
   *         constructs messages properly
   */
  @SuppressWarnings({ "unchecked" })
  public static MessageBuildSubject<MessageBuildSendableWithReply> createMessage() {
    return new DefaultMessageBuilder(provider.get()).start();
  }

  /**
   * Creates a new message for the provided subject.
   *
   * @param subject
   *          the subject the message should be sent to
   * @return a <tt>MessageBuildSubject</tt> which essentially is a <tt>Message</tt>, but ensures that the user
   *         constructs messages properly
   */
  @SuppressWarnings({ "unchecked" })
  public static MessageBuildCommand<MessageBuildSendableWithReply> createMessage(final String subject) {
    return new DefaultMessageBuilder(provider.get()).start().toSubject(subject);
  }

  /**
   * Creates a conversational message.
   *
   * @param message
   *          reference message to create conversation from
   * @return a <tt>MessageBuildSubject</tt> which essentially is a <tt>Message</tt>, but ensures that the user
   *         constructs messages properly
   */
  public static MessageBuildSubject<MessageReplySendable> createConversation(final Message message) {
    final Message newMessage = provider.get();
    newMessage.setFlag(RoutingFlag.NonGlobalRouting);
    if (newMessage instanceof HasEncoded) {
      return new DefaultMessageBuilder<MessageReplySendable>(new HasEncodedConvMessageWrapper(message, newMessage))
          .start();
    }
    else {
      return new DefaultMessageBuilder<MessageReplySendable>(new ConversationMessageWrapper(message, newMessage))
          .start();
    }
  }

  /**
   * Creates a conversational message for the provided subject.
   *
   * @param message
   *          reference message to create conversation from
   * @param subject
   *          the subject the message should be sent to
   * @return a <tt>MessageBuildSubject</tt> which essentially is a <tt>Message</tt>, but ensures that the user
   *         constructs messages properly
   */
  public static MessageBuildCommand<MessageReplySendable> createConversation(final Message message, final String subject) {
    final Message newMessage = provider.get();
    if (newMessage instanceof HasEncoded) {
      return new DefaultMessageBuilder<MessageReplySendable>(new HasEncodedConvMessageWrapper(message, newMessage))
          .start()
          .toSubject(subject);
    }
    else {
      return new DefaultMessageBuilder<MessageReplySendable>(new ConversationMessageWrapper(message, newMessage))
          .start()
          .toSubject(subject);
    }
  }

  /**
   * Creates an <tt>AbstractRemoteCallBuilder</tt> to construct a call.
   *
   * @return an instance of <tt>AbstractRemoteCallBuilder</tt>
   */
  public static DefaultRemoteCallBuilder createCall() {
    return new DefaultRemoteCallBuilder(CommandMessage.create());
  }

  /**
   * Creates an RPC call, with no error handling.
   *
   * @param callback
   *          The remote callback that receives the return value from the call. Cannot not be null.
   * @param service
   *          The remote interface.
   * @param <T>
   *          The type of the remote service.
   * @param <R>
   *          The return type of the invoked method.
   * @return A proxy for the remote service. Methods invoked on this object will communicate with the remote service
   *         over the message bus.
   */
  public static <R, T> T createCall(final RemoteCallback<R> callback, final Class<T> service) {
    return new DefaultRemoteCallBuilder(CommandMessage.create()).call(callback, service);
  }

  /**
   * Creates an RPC call with error handling.
   *
   * @param callback
   *          The remote callback that receives the return value from the call. Cannot not be null.
   * @param errorCallback
   *          The error callback that receives transmission errors and exceptions thrown by the remote service. Cannot not be null.
   * @param service
   *          The remote interface.
   * @param <T>
   *          The type of the remote service.
   * @param <R>
   *          The return type of the invoked method.
   * @return A proxy for the remote service. Methods invoked on this object will communicate with the remote service
   *         over the message bus.
   */
  public static <R, T> T createCall(final RemoteCallback<R> callback, final BusErrorCallback errorCallback, final Class<T> service) {
    return new DefaultRemoteCallBuilder(CommandMessage.create()).call(callback, errorCallback, service);
  }

  /**
   * Sets the message provider for this instance of <tt>MessageBuilder</tt>.
   *
   * @param provider  to set this' provider to
   */
  public static void setMessageProvider(final MessageProvider provider) {
    MessageBuilder.provider = provider;
  }

  /**
   * Returns the message provider.
   *
   * @return the message provider used by this instance of <tt>MessageBuilder</tt>.
   */
  public static MessageProvider getMessageProvider() {
    return provider;
  }
}
