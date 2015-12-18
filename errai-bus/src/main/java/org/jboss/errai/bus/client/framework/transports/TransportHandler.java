/*
 * Copyright (C) 2012 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.bus.client.framework.transports;

import java.util.Collection;
import java.util.List;

import org.jboss.errai.bus.client.api.messaging.Message;

/**
* @author Mike Brock
*/
public interface TransportHandler {
  public static final String EXTRA_URI_PARMS_RESOURCE = "^ExtraURIParameters";

  static final String UNSUPPORTED_MESSAGE_NO_SERVER_SUPPORT = "Not configured / Server does not support";
  static final String UNSUPPORTED_MESSAGE_NO_CLIENT_SUPPORT = "Browser does not support";

  /**
   * Called to configure the transport. The Message instance passed to the handler, is the initial response message
   * from the server during the ErraiBus protocol handshake, which contains the capabilities data for the remote
   * bus.
   *
   * @param capabilitiesMessage
   *        the capabilities message from the message bus on handshake.
   */
  public void configure(Message capabilitiesMessage);

  /**
   * Called to start the transport.
   */
  public void start();

  /**
   * Called to stop the transport.
   *
   * @param stopAllCurrentRequests
   *        specifying <tt>true</tt> will cause any in-flight messages which have not returned to be immediately
   *        cancelled.
   *
   * @return
   *        a list of messages which were not delivered before the transport was stopped.
   */
  public Collection<Message> stop(boolean stopAllCurrentRequests);

  /**
   * Transmits the specified list of {@link Message} to the remote bus over the transport.
   *
   * @param txMessages
   *        a list of {@link Message} to be transmitted.
   */
  public void transmit(List<Message> txMessages);

  /**
   * Allows extension to the standard ErraiBus Protocol by optionally handling any unknown protocol verbs from the
   * wire.
   *
   * @param message
   */
  public void handleProtocolExtension(Message message);

  /**
   * Indicates whether the transport is usable. This method is called during transport switching by the bus to
   * determine which transport to use. The first usable transport is chosen and started.
   *
   * @return
   *      <tt>true</tt> if the handler is usable.
   */
  public boolean isUsable();

  /**
   * Returns the {@link TransportStatistics} instance.
   *
   * @return
   */
  public TransportStatistics getStatistics();

  /**
   * Permanently closes this transport handler. Once closed, a TransportHandler
   * cannot be used again. It is imperative that a TransportHandler is closed
   * when it is no longer needed; failing to do so may leak resources and lead
   * to client bus malfunctions.
   */
  public void close();
}
