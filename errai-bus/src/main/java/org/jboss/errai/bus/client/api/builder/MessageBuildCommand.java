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

import org.jboss.errai.bus.client.api.base.MessageBuilder;

/**
 * This interface is a template for building the command part of a message. This ensures that the call is constructed
 * properly.
 * <p>
 * Part of the fluent API centered around {@link MessageBuilder}.
 */
public interface MessageBuildCommand<R> extends MessageBuildParms<R> {

  /**
   * Sets the command for the message, and returns an instance of <tt>MessageBuildParms</tt>, which needs to be
   * constructed following setting the command.
   * 
   * @param command
   *          the command to set for this message. Could be one of
   *          {@link org.jboss.errai.bus.client.protocols.BusCommand}
   * @return an instance of <tt>MessageBuildParms</tt>
   */
  public MessageBuildParms<R> command(Enum<?> command);

  /**
   * Sets the command for the message, and returns an instance of <tt>MessageBuildParms</tt>, which needs to be
   * constructed following setting the command.
   * 
   * @param command
   *          the command to set for this message.
   * @return an instance of <tt>MessageBuildParms</tt>
   */
  public MessageBuildParms<R> command(String command);

  /**
   * If <tt>signalling</tt> is called, the service is only signalled as opposed to sending a specific command.
   * 
   * @return an instance of <tt>MessageBuildParms</tt>
   */
  public MessageBuildParms<R> signalling();

}
