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

import java.util.Map;

import org.jboss.errai.bus.client.api.RoutingFlag;
import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.bus.client.api.messaging.MessageBus;
import org.jboss.errai.bus.client.api.messaging.RequestDispatcher;
import org.jboss.errai.common.client.api.Assert;
import org.jboss.errai.common.client.api.ErrorCallback;
import org.jboss.errai.common.client.api.ResourceProvider;
import org.jboss.errai.common.client.protocols.MessageParts;

/**
 * Internal wrapper class that makes any type of given message (the <i>wrapped
 * message</i>) conversational.
 */
class ConversationMessageWrapper implements Message {
  protected Message message;
  protected Message newMessage;

  /**
   * Creates a new wrapper that makes newMessage a reply to the given message.
   *  
   * @param inReplyTo
   *          The message this wrapper is in reply to. Not null.
   * @param newMessage
   *          The new message to be wrapped. Not null.
   */
  public ConversationMessageWrapper(final Message inReplyTo, final Message newMessage) {
    this.message = Assert.notNull(inReplyTo);
    this.newMessage = Assert.notNull(newMessage);
  }

  @Override
  public Message toSubject(final String subject) {
    newMessage.toSubject(subject);
    return this;
  }

  @Override
  public String getSubject() {
    return newMessage.getSubject();
  }

  @Override
  public Message command(final String type) {
    newMessage.command(type);
    return this;
  }

  @Override
  public Message command(final Enum<?> type) {
    newMessage.command(type);
    return this;
  }

  @Override
  public String getCommandType() {
    return newMessage.getCommandType();
  }

  @Override
  public Message set(final String part, final Object value) {
    newMessage.set(part, value);
    return this;
  }

  @Override
  public Message set(final Enum<?> part, final Object value) {
    newMessage.set(part, value);
    return this;
  }

  @Override
  public Message setProvidedPart(final String part, final ResourceProvider<?> provider) {
    newMessage.setProvidedPart(part, provider);
    return this;
  }

  @Override
  public Message setProvidedPart(final Enum<?> part, final ResourceProvider<?> provider) {
    newMessage.setProvidedPart(part, provider);
    return this;
  }

  @Override
  public boolean hasPart(final String part) {
    return newMessage.hasPart(part);
  }

  @Override
  public boolean hasPart(final Enum<?> part) {
    return newMessage.hasPart(part);
  }

  @Override
  public void remove(final String part) {
    newMessage.remove(part);
  }

  @Override
  public void remove(final Enum<?> part) {
    newMessage.remove(part);
  }

  @Override
  public Message copy(final String part, final Message m) {
    newMessage.copy(part, m);
    return this;
  }

  @Override
  public Message copy(final Enum<?> part, final Message m) {
    newMessage.copy(part, m);
    return this;
  }

  @Override
  public Message setParts(final Map<String, Object> parts) {
    newMessage.setParts(parts);
    return this;
  }

  @Override
  public Message addAllParts(final Map<String, Object> parts) {
    newMessage.addAllParts(parts);
    return this;
  }

  @Override
  public Message addAllProvidedParts(final Map<String, ResourceProvider<?>> provided) {
    newMessage.addAllProvidedParts(provided);
    return this;
  }

  @Override
  public Map<String, ResourceProvider<?>> getProvidedParts() {
    return newMessage.getProvidedParts();
  }

  @Override
  public Map<String, Object> getParts() {
    return newMessage.getParts();
  }

  @Override
  public void addResources(final Map<String, ?> resources) {
    newMessage.addResources(resources);
  }

  @Override
  public Message setResource(final String key, final Object res) {
    newMessage.setResource(key, res);
    return this;
  }

  @Override
  public <T> T getResource(final Class<T> type, final String key) {
    return newMessage.getResource(type, key);
  }

  @Override
  public boolean hasResource(final String key) {
    return newMessage.hasResource(key);
  }

  @Override
  public Message copyResource(final String key, final Message m) {
    newMessage.copyResource(key, m);
    return this;
  }

  @Override
  public Message errorsCall(final ErrorCallback callback) {
    newMessage.errorsCall(callback);
    return this;
  }

  @Override
  public ErrorCallback<Message> getErrorCallback() {
    return newMessage.getErrorCallback();
  }


  @Override
  public <T> T getValue(final Class<T> type) {
    return newMessage.getValue(type);
  }

  @Override
  public <T> T get(final Class<T> type, final String part) {
    return newMessage.get(type, part);
  }

  @Override
  public <T> T get(final Class<T> type, final Enum<?> part) {
    return newMessage.get(type, part);
  }

  @Override
  public Message setFlag(final RoutingFlag flag) {
    newMessage.setFlag(flag);
    return this;
  }

  @Override
  public void unsetFlag(final RoutingFlag flag) {
    newMessage.unsetFlag(flag);
  }

  @Override
  public boolean isFlagSet(final RoutingFlag flag) {
    return newMessage.isFlagSet(flag);
  }

  @Override
  public void sendNowWith(final MessageBus viaThis) {
    if (ConversationHelper.hasConversationCallback(this)) {
      ConversationHelper.createConversationService(viaThis, this);
    }
    viaThis.send(this);
  }

  @Override
  public void sendNowWith(final RequestDispatcher viaThis) {
    try {
      viaThis.dispatch(this);
    }
    catch (Exception e) {
      throw new MessageDeliveryFailure("unable to deliver message: " + e.getMessage(), e);
    }
  }

  /**
   * Returns the Message instance that this message is in response to.
   *
   * @return The originating message that this Message is in reply to.
   */
  public Message getIncomingMessage() {
    return message;
  }

  boolean committed = false;

  /**
   * Replaces the behaviour of the delegate message's {@code commit()} method
   * with the following:
   *
   * <ul>
   *   <li>Sets the {@code toSubject} to the wrapped message's {@code replyTo}
   *   unless the wrapped message already has a {@code toSubject}.
   *   <li>Sets the {@code Session} for this message to the same session as the
   *   incoming message.
   * </ul>
   *
   * @throws RuntimeException
   *           if the originating message does not have a Session resource, or
   *           if this message has no {@code toSubject} and the incoming message
   *           has no {@code replyTo}.
   */
  @Override
  public void commit() {
    // XXX why is this logic deferred until commit()? why not do these things in the constructor?
    if (!hasPart(MessageParts.ToSubject)) {
      if (message.hasPart(MessageParts.ReplyTo)) {
        toSubject(message.get(String.class, MessageParts.ReplyTo));
      }
      else {
        throw new RuntimeException("cannot have a conversation.  the incoming message does not specify a recipient ReplyTo subject and you have not specified one.");
      }
    }

    if (message.hasResource("Session")) {
      newMessage.copyResource("Session", message);
    }
    else {
      throw new RuntimeException("cannot have a conversation.  the incoming message has no session data associated with it.");
    }

    committed = true;
  }

  @Override
  public boolean isCommited() {
    return committed;
  }
}
