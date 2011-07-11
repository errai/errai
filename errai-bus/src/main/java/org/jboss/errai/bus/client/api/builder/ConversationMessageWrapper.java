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

package org.jboss.errai.bus.client.api.builder;

import org.jboss.errai.bus.client.api.ErrorCallback;
import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.api.ResourceProvider;
import org.jboss.errai.bus.client.api.base.MessageDeliveryFailure;
import org.jboss.errai.bus.client.framework.MessageBus;
import org.jboss.errai.bus.client.framework.RequestDispatcher;
import org.jboss.errai.bus.client.framework.RoutingFlags;
import org.jboss.errai.bus.client.protocols.MessageParts;

import java.util.Map;

public class ConversationMessageWrapper implements Message {
  protected Message message;
  protected Message newMessage;

  public ConversationMessageWrapper(Message inReplyTo, Message newMessage) {
    this.message = inReplyTo;
    this.newMessage = newMessage;
  }

  public Message toSubject(String subject) {
    newMessage.toSubject(subject);
    return this;
  }

  public String getSubject() {
    return newMessage.getSubject();
  }

  public Message command(String type) {
    newMessage.command(type);
    return this;
  }

  public Message command(Enum type) {
    newMessage.command(type);
    return this;
  }

  public String getCommandType() {
    return newMessage.getCommandType();
  }

  public Message set(String part, Object value) {
    newMessage.set(part, value);
    return this;
  }

  public Message set(Enum part, Object value) {
    newMessage.set(part, value);
    return this;
  }

  public Message setProvidedPart(String part, ResourceProvider provider) {
    newMessage.setProvidedPart(part, provider);
    return this;
  }

  public Message setProvidedPart(Enum part, ResourceProvider provider) {
    newMessage.setProvidedPart(part, provider);
    return this;
  }

  public boolean hasPart(String part) {
    return newMessage.hasPart(part);
  }

  public boolean hasPart(Enum part) {
    return newMessage.hasPart(part);
  }

  public void remove(String part) {
    newMessage.remove(part);
  }

  public void remove(Enum part) {
    newMessage.remove(part);
  }

  public Message copy(String part, Message m) {
    newMessage.copy(part, m);
    return this;
  }

  public Message copy(Enum part, Message m) {
    newMessage.copy(part, m);
    return this;
  }

  public Message setParts(Map<String, Object> parts) {
    newMessage.setParts(parts);
    return this;
  }

  public Message addAllParts(Map<String, Object> parts) {
    newMessage.addAllParts(parts);
    return this;
  }

  public Message addAllProvidedParts(Map<String, ResourceProvider> provided) {
    newMessage.addAllProvidedParts(provided);
    return this;
  }

  public Map<String, ResourceProvider> getProvidedParts() {
    return newMessage.getProvidedParts();
  }

  public Map<String, Object> getParts() {
    return newMessage.getParts();
  }

  public void addResources(Map<String, ?> resources) {
    newMessage.addResources(resources);
  }

  public Message setResource(String key, Object res) {
    newMessage.setResource(key, res);
    return this;
  }

  public <T> T getResource(Class<T> type, String key) {
    return newMessage.getResource(type, key);
  }

  public boolean hasResource(String key) {
    return newMessage.hasResource(key);
  }

  public Message copyResource(String key, Message m) {
    newMessage.copyResource(key, m);
    return this;
  }

  public Message errorsCall(ErrorCallback callback) {
    newMessage.errorsCall(callback);
    return this;
  }

  public ErrorCallback getErrorCallback() {
    return newMessage.getErrorCallback();
  }

  public <T> T get(Class<T> type, String part) {
    return newMessage.get(type, part);
  }

  public <T> T get(Class<T> type, Enum<?> part) {
    return newMessage.get(type, part);
  }

  public void setFlag(RoutingFlags flag) {
    newMessage.setFlag(flag);
  }

  public void unsetFlag(RoutingFlags flag) {
    newMessage.unsetFlag(flag);
  }

  public boolean isFlagSet(RoutingFlags flag) {
    return newMessage.isFlagSet(flag);
  }

  public void sendNowWith(MessageBus viaThis) {
    viaThis.send(this);
  }

  public void sendNowWith(RequestDispatcher viaThis) {
    try {
      viaThis.dispatch(this);
    }
    catch (Exception e) {
      throw new MessageDeliveryFailure("unable to deliver message: " + e.getMessage(), e);
    }
  }

  public Message getIncomingMessage() {
    return message;
  }

  boolean committed = false;

  public void commit() {
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
      throw new RuntimeException("cannot have a conversation.  the incoming message has not session data associated with it.");
    }

    committed = true;
  }

  public boolean isCommited() {
    return committed;
  }
}
