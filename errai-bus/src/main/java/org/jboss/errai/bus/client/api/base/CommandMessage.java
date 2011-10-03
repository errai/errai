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

import org.jboss.errai.bus.client.api.ErrorCallback;
import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.api.ResourceProvider;
import org.jboss.errai.bus.client.framework.MessageBus;
import org.jboss.errai.bus.client.framework.RequestDispatcher;
import org.jboss.errai.bus.client.framework.RoutingFlags;
import org.jboss.errai.bus.client.protocols.MessageParts;
import org.jboss.errai.common.client.types.DecodingContext;
import org.jboss.errai.common.client.types.TypeHandlerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * CommandMessage represents a message payload.  It implements a builder (or fluent) API which is used for constructing
 * sendable messages.  It is the core messageing API for ErraiBus, and will be the foremost used class within ErraiBus
 * by most users.
 * <p/>
 * <strong>Example Message:</strong>
 * <tt><pre>
 * CommandMessage msg = CommandMessage.create()
 *                          .toSubject("Foo")
 *                          .set("Text", "I like chocolate cake.");
 * </pre></tt>
 * You can transmit a message using the the <tt>sendNowWith()</tt> method by providing an instance of
 * {@link org.jboss.errai.bus.client.framework.MessageBus}.
 * <p/>
 * Messages can be contructed using user-defined standard protocols through the use of enumerations. Both
 * <tt>commandType</tt> and message parts can be defined through the use of enumerations.  This helps create
 * strongly-defined protocols for communicating with services.  For instance:
 * <tt><pre>
 * public enum LoginParts {
 *    Username, Password
 * }
 * </pre></tt>
 * .. and ..
 * <tt><pre>
 * public enum LoginCommands {
 *    Login, Logout
 * }
 * </pre></tt>
 * These enumerations can than be directly used to build messages and decode incoming messages by a service. For example:
 * <tt><pre>
 *  CommandMessage.create()
 *      .command(LoginCommands.Login)
 *      .set(LoginParts.Username, "foo")
 *      .set(LoginParts.Password, "bar )
 *      .sendNowWith(busInstance);
 * </pre></tt>
 * Messages may contain serialized objects provided they meet the following criteria:
 * <ol>
 * <li>The class is annotated with {@link org.jboss.errai.bus.server.annotations.ExposeEntity}</li>
 * <li>The class implements {@link java.io.Serializable}.
 * <li>The class contains a default, no-argument constructor.
 * </ol>
 *
 * @see ConversationMessage
 */
@SuppressWarnings({"GwtInconsistentSerializableClass", "UnusedDeclaration"})
public class CommandMessage implements Message {
  protected transient Map<String, ResourceProvider> providedParts;
  protected Map<String, Object> parts;
  protected transient Map<String, Object> resources;
  protected ErrorCallback errorsCall;
  protected int routingFlags;

  /**
   * @param commandType -
   * @return -
   * @deprecated Please use the MessageBuilder class.
   */
  @Deprecated
  static CommandMessage create(String commandType) {
    return new CommandMessage(commandType);
  }

  /**
   * @param commandType -
   * @return -
   * @deprecated Please use the MessageBuilder class.
   */
  @Deprecated
  static CommandMessage create(Enum commandType) {
    return new CommandMessage(commandType);
  }

  /**
   * Create a new CommandMessage.
   *
   * @return a new instance of CommandMessage
   */

  static CommandMessage create() {
    return new CommandMessage();
  }

  /**
   * For internal use. This method should not be directly used.
   *
   * @param parts -
   * @return -
   */
  public static CommandMessage createWithParts(Map<String, Object> parts) {
    return new CommandMessage(parts);
  }

  public static CommandMessage createWithParts(Map<String, Object> parts, Map<String, ResourceProvider> provided) {
    return new CommandMessage(parts, provided);
  }

  public CommandMessage() {
    this.parts = new HashMap<String, Object>();
    this.providedParts = new HashMap<String, ResourceProvider>();
  }

  private CommandMessage(Map<String, Object> parts) {
    this.parts = parts;
    this.providedParts = new HashMap<String, ResourceProvider>();
  }

  private CommandMessage(Map<String, Object> parts, Map<String, ResourceProvider> providers) {
    this.parts = parts;
    this.providedParts = providers;
  }

  private CommandMessage(String commandType) {
    command(commandType);
  }

  private CommandMessage(Enum commandType) {
    command(commandType.name());
  }

  private CommandMessage(String subject, String commandType) {
    toSubject(subject).command(commandType);
  }


  /**
   * Return the specified command type.  Returns <tt>null</tt> if not specified.
   *
   * @return - String representing the command type.
   */
  public String getCommandType() {
    return (String) parts.get(MessageParts.CommandType.name());
  }

  /**
   * Return the specified message subject.
   * @return -
   */
  public String getSubject() {
    return String.valueOf(parts.get(MessageParts.ToSubject.name()));
  }

  /**
   * Set the subject which is the intended recipient of the message.
   *
   * @param subject - subject name.
   * @return -
   */
  public Message toSubject(String subject) {
    parts.put(MessageParts.ToSubject.name(), subject);
    return this;
  }

  /**
   * Set the optional command type.
   *
   * @param type -
   * @return -
   */
  public Message command(Enum type) {
    parts.put(MessageParts.CommandType.name(), type.name());
    return this;
  }

  /**
   * Set the optional command type.
   *
   * @param type -
   * @return -
   */
  public Message command(String type) {
    parts.put(MessageParts.CommandType.name(), type);
    return this;
  }

  /**
   * Set a message part to the specified value.
   *
   * @param part  - Mesage part
   * @param value - Value instance
   * @return -
   */
  public Message set(Enum part, Object value) {
    return set(part.name(), value);
  }

  /**
   * Set a message part to the specified value.
   *
   * @param part  - Mesage part
   * @param value - Value instance
   * @return -
   */
  public Message set(String part, Object value) {
    parts.put(part, value);
    return this;
  }

  public Message setProvidedPart(String part, ResourceProvider provider) {
    providedParts.put(part, provider);
    return this;
  }

  public Message setProvidedPart(Enum part, ResourceProvider provider) {
    return setProvidedPart(part.name(), provider);
  }

  public void remove(String part) {
    parts.remove(part);
  }

  public void remove(Enum part) {
    parts.remove(part.name());
  }

  /**
   * Copy the same value from the specified message into this message.
   *
   * @param part    - Part to copy
   * @param message - CommandMessage to copy from
   * @return -
   */
  public Message copy(Enum part, Message message) {
    set(part, message.get(Object.class, part));
    return this;
  }

  /**
   * Copy the same value from the specified message into this message.
   *
   * @param part    - Part to copy
   * @param message - CommandMessage to copy from
   * @return -
   */
  public Message copy(String part, Message message) {
    set(part, message.get(Object.class, part));
    return this;
  }

  /**
   * Set flags for this message
   *
   * @param flag - <tt>RoutingFlags</tt> can be set to NonGlobalRouting or PriorityProcessing
   */
  public void setFlag(RoutingFlags flag) {
    routingFlags |= flag.flag();
  }

  /**
   * Unset flags for this message
   *
   * @param flag - <tt>RoutingFlags</tt> can be set to NonGlobalRouting or PriorityProcessing
   */
  public void unsetFlag(RoutingFlags flag) {
    if ((routingFlags & flag.flag()) != 0) {
      routingFlags ^= flag.flag();
    }
  }

  /**
   * Checks if a flag is setfor this message
   *
   * @param flag - <tt>RoutingFlags</tt> can be set to NonGlobalRouting or PriorityProcessing
   * @return true if the flag is set
   */
  public boolean isFlagSet(RoutingFlags flag) {
    return (routingFlags & flag.flag()) != 0;
  }


  /**
   * Get the specified message part in the specified type.  A <tt>ClassCastException</tt> is thrown if the value
   * cannot be coerced to the specified type.
   *
   * @param type - Type to be returned.
   * @param part - Message part.
   * @param <T>  - Type to be returned.
   * @return - Value in the specified type.
   */
  @SuppressWarnings({"UnusedDeclaration"})
  public <T> T get(Class<T> type, Enum<?> part) {
    return get(type, part.toString());
  }

  /**
   * Get the specified message part in the specified type.  A <tt>ClassCastException</tt> is thrown if the value
   * cannot be coerced to the specified type.
   *
   * @param type - Type to be returned.
   * @param part - Message part.
   * @param <T>  - Type to be returned.
   * @return - Value in the specified type.
   */
  @SuppressWarnings({"UnusedDeclaration"})
  public <T> T get(Class<T> type, String part) {
    Object value = parts.get(part);
    return value == null ? null : TypeHandlerFactory.convert(value.getClass(), type, value, new DecodingContext());
  }

  /**
   * Returns true if the specified part is defined in the message.
   *
   * @param part - Message part.
   * @return - boolean value indiciating whether or not specified part is present in the message.
   */
  public boolean hasPart(Enum part) {
    return hasPart(part.name());
  }

  /**
   * Returns true if the specified part is defined in the message.
   *
   * @param part - Message part.
   * @return - boolean value indiciating whether or not specified part is present in the message.
   */
  public boolean hasPart(String part) {
    return parts.containsKey(part);
  }

  /**
   * Return a Map of all the specified parts.
   *
   * @return - A Map of parts.
   */
  public Map<String, Object> getParts() {
    return parts;
  }

  public Map<String, ResourceProvider> getProvidedParts() {
    return providedParts;
  }

  /**
   * Set the message to contain the specified parts.  Note: This overwrites any existing message contents.
   *
   * @param parts - Parts to be used in the message.
   * @return -
   */
  public Message setParts(Map<String, Object> parts) {
    this.parts = parts;
    return this;
  }

  /**
   * Add the specified parts to the message.
   *
   * @param parts - Parts to be added to the message.
   * @return -
   */
  public Message addAllParts(Map<String, Object> parts) {
    this.parts.putAll(parts);
    return this;
  }

  /**
   * Add the specified parts to the message.
   *
   * @param parts - Parts to be added to the message.
   * @return -
   */
  public Message addAllProvidedParts(Map<String, ResourceProvider> parts) {
    this.providedParts.putAll(parts);
    return this;
  }

  /**
   * Set a transient resource.  A resource is not transmitted beyond the current bus scope.  It can be used for
   * managing the lifecycle of a message within a bus.
   *
   * @param key - Name of resource
   * @param res - Instance of resouce
   * @return -
   */
  public Message setResource(String key, Object res) {
    if (this.resources == null) this.resources = new HashMap<String, Object>();
    this.resources.put(key, res);
    return this;
  }

  /**
   * Obtain a transient resource based on the specified key.
   *
   * @param key - Name of resource.
   * @return - Instancee of resource.
   */
  @SuppressWarnings("unchecked")
  public <T> T getResource(Class<T> type, String key) {
    return (T) (this.resources == null ? null : this.resources.get(key));
  }

  /**
   * Copy a transient resource to this mesage from the specified message.
   *
   * @param key      - Name of resource.
   * @param copyFrom - Message to copy from.
   * @return -
   */
  public Message copyResource(String key, Message copyFrom) {
    if (!copyFrom.hasResource(key)) {
      throw new RuntimeException("Cannot copy resource '" + key + "': no such resource.");
    }
    setResource(key, copyFrom.getResource(Object.class, key));
    return this;
  }

  /**
   * Sets the error callback for this message
   *
   * @param callback - error callback
   * @return this
   */
  public Message errorsCall(ErrorCallback callback) {
    if (this.errorsCall != null) {
      throw new RuntimeException("An ErrorCallback is already registered");
    }
    this.errorsCall = callback;
    return this;
  }

  /**
   * Gets the error callback for this message
   *
   * @return the error callback
   */
  public ErrorCallback getErrorCallback() {
    return errorsCall;
  }

  /**
   * Returns true if the specified transient resource is present.
   *
   * @param key - Name of resouce
   * @return - boolean value indicating if the specified resource is present in the message.
   */
  public boolean hasResource(String key) {
    return this.resources != null && this.resources.containsKey(key);
  }

  /**
   * Add the Map of resources to the message.
   *
   * @param resources - Map of resource
   */
  public void addResources(Map<String, ?> resources) {
    if (this.resources == null) {
      this.resources = new HashMap<String, Object>(resources);
    }
    else {
      this.resources.putAll(resources);
    }
  }

  public void commit() {
    if (!providedParts.isEmpty()) {
      for (Map.Entry<String, ResourceProvider> entry : providedParts.entrySet())
        set(entry.getKey(), entry.getValue().get());
    }
  }

  public boolean isCommited() {
    return isFlagSet(RoutingFlags.Committed);
  }

  /**
   * Transmit this message to the specified {@link org.jboss.errai.bus.client.framework.MessageBus} instance.
   * @param viaThis -
   */
  public void sendNowWith(MessageBus viaThis) {
    viaThis.send(this);
  }

  /**
   * Transmit this message using the specified {@link org.jboss.errai.bus.client.framework.RequestDispatcher}.
   * @param viaThis -
   */
  public void sendNowWith(RequestDispatcher viaThis) {
    try {
      viaThis.dispatch(this);
    }
    catch (Exception e) {
      throw new MessageDeliveryFailure("could not deliver message: " + e.getMessage(), e);
    }
  }

  /**
   * Gets a <tt>String</tt> representation of this message, which contains the subject and type
   *
   * @return String representation of message
   */
  @Override
  public String toString() {
    return buildDescription();
  }

  public Map<String, Object> getResources() {
    return resources;
  }

  public void setResources(Map<String, Object> resources) {
    this.resources = resources;
  }

  public ErrorCallback getErrorsCall() {
    return errorsCall;
  }

  public void setErrorsCall(ErrorCallback errorsCall) {
    this.errorsCall = errorsCall;
  }

  public int getRoutingFlags() {
    return routingFlags;
  }

  public void setRoutingFlags(int routingFlags) {
    this.routingFlags = routingFlags;
  }

  private String buildDescription() {
    //  if (parts == null) return "";
    StringBuilder append = new StringBuilder();
    boolean f = false;
    for (Map.Entry<String, Object> entry : parts.entrySet()) {
      if (f) append.append(", ");
      append.append(entry.getKey()).append("=").append(String.valueOf(entry.getValue()));
      f = true;
    }
    return append.toString();
  }
}
