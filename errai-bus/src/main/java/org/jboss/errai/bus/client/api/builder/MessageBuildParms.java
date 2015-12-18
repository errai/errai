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

package org.jboss.errai.bus.client.api.builder;

import org.jboss.errai.common.client.api.ErrorCallback;
import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.bus.client.api.base.DefaultErrorCallback;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.api.RoutingFlag;
import org.jboss.errai.common.client.api.ResourceProvider;

/**
 * A template for building the different parameters of a message. This ensures that they are constructed properly.
 * <p>
 * Part of the fluent API centered around {@link MessageBuilder}.
 */
public interface MessageBuildParms<R> extends MessageBuild {

  /**
   * Include a default value with the message. This is the same as with(MessageParts.Value, value). This offers a
   * convenience method for sending messages which contain a single object payload.
   * 
   * @param value
   * @return the updated {@link MessageBuildParms} for chaining additional calls
   */
  public MessageBuildParms<R> withValue(Object value);

  /**
   * Sets the message part to the specified value, replacing any old value associated with that part.
   * 
   * @param part
   *          the message part to add or replace
   * @param value
   *          the value of the message part
   * @return the updated {@link MessageBuildParms} for chaining additional calls
   */
  public MessageBuildParms<R> with(String part, Object value);

  /**
   * Sets a {@link RoutingFlag} on the underlying message.
   * 
   * @param flag
   *          the {@link RoutingFlag} to set
   * @return the updated {@link MessageBuildParms} for chaining additional calls
   */
  public MessageBuildParms<R> flag(RoutingFlag flag);

  /**
   * Sets the message part to the specified value, replacing any old value associated with that part.
   * 
   * @param part
   *          the message part to add or replace
   * @param value
   *          the value of the message part
   * @return the updated {@link MessageBuildParms} for chaining additional calls
   */
  public MessageBuildParms<R> with(Enum<?> part, Object value);

  /**
   * Sets the message part to be generated at message transmission time by the given provider, replacing any old value
   * associated with that part.
   * 
   * @param part
   *          the message part to add or replace
   * @param provider
   *          the provider that generates the value for this message part each time the message is transmitted.
   * @return the updated {@link MessageBuildParms} for chaining additional calls
   */
  public MessageBuildParms<R> withProvided(String part, ResourceProvider<?> provider);

  /**
   * Sets the message part to be generated at message transmission time by the given provider, replacing any old value
   * associated with that part.
   * 
   * @param part
   *          the message part to add or replace
   * @param provider
   *          the provider that generates the value for this message part each time the message is transmitted.
   * @return the updated {@link MessageBuildParms} for chaining additional calls
   */
  public MessageBuildParms<R> withProvided(Enum<?> part, ResourceProvider<?> provider);

  /**
   * Copies the message part to the specified message
   * 
   * @param part
   *          the message part
   * @param m
   *          the target message to receive the copied part.
   * @return the updated {@link MessageBuildParms} for chaining additional calls
   */
  public MessageBuildParms<R> copy(String part, Message m);

  /**
   * Copies a message part to the specified message, replacing that part in the target message if it already exists.
   * 
   * @param part
   *          the message part to copy from this builder's message.
   * @param m
   *          the target message to receive the copied part.
   * @return the updated {@link MessageBuildParms} for chaining additional calls
   */
  public MessageBuildParms<R> copy(Enum<?> part, Message m);

  /**
   * Copies a message resource to the specified message, replacing that resource in the target message if it already exists.
   * 
   * @param part
   *          the message resource to copy from this builder's message.
   * @param m
   *          the target message to receive the copied resource.
   * @return the updated {@link MessageBuildParms} for chaining additional calls
   */
  public MessageBuildParms<R> copyResource(String part, Message m);

  /**
   * Sets the error callback function for the message.
   * 
   * @param callback
   *          the function to be called when an error occurs.
   * @return the underlying Sendable from this builder.
   */
  public R errorsHandledBy(ErrorCallback callback);

  /**
   * Specifies that any errors encountered while handling or transmitting this builder's message should be silently
   * ignored.
   * 
   * @return the underlying Sendable from this builder.
   */
  public R noErrorHandling();

  /**
   * Specifies that the default error handler should be notified when there are errors in transmitting this builder's
   * message (see {@link DefaultErrorCallback}).
   * 
   * @return the underlying Sendable from this builder.
   */
  public R defaultErrorHandling();

  /**
   * No-op method for returning the underlying message being built.
   * 
   * @return the underlying Sendable from this builder.
   */
  public R done();
}
