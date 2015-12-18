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

package org.jboss.errai.bus.client.protocols;

/**
 * Defines the standard protocol commands for interaction between federated {@link org.jboss.errai.bus.client.api.messaging.MessageBus}
 * instances.
 */
public enum BusCommand {
  /**
   * The first command sent from a client bus to a remote bus.  This message indicates the bus would like to
   * establish a connection to the queue.  There is no authentication associated with this command.  Any
   * unauthenticated bus can connect to the bus, as establishing a communication channel is necessary
   * for all communication, including authentication.
   */
  Associate,

  /**
   * The command sent from the remote bus back to the client bus to tell it that it should transition to a
   * normal communications mode, as all remote subscription data will have been sent at this point.
   */
  FinishAssociation,

  /**
   * Notifies the remote bus that a local subscription has been registered.  This is an advertisement to the
   * remote bus, so it can update it's routing tables.
   * <p/>
   * Parameters Accepted:
   * <p/>
   * <table style="border: 1px solid gray" cellpadding="3">
   * <thead style="font-weight: bold;">
   * <tr>
   * <td>Part</td>
   * <td>Type</td>
   * <td>Description</td>
   * </tr>
   * <thead>
   * <tbody>
   * <tr>
   * <td>{@link org.jboss.errai.common.client.protocols.MessageParts#Subject}</td>
   * <td>{@link String}</td>
   * <td>The name of the subject being subscribed to</td>
   * </tr>
   * </tbody>
   * </table>
   */
  RemoteSubscribe,

  /**
   * Notifies the remote bus that a particular subject is no longer subscribed to locally.
   * <p/>
   * Parameters Accepted:
   * <p/>
   * <table style="border: 1px solid gray" cellpadding="3">
   * <thead style="font-weight: bold;">
   * <tr>
   * <td>Part</td>
   * <td>Type</td>
   * <td>Description</td>
   * </tr>
   * <thead>
   * <tbody>
   * <tr>
   * <td>{@link org.jboss.errai.common.client.protocols.MessageParts#Subject}</td>
   * <td>{@link String}</td>
   * <td>The name of the subject being subscribed to</td>
   * </tr>
   * </tbody>
   * </table>
   */
  RemoteUnsubscribe,

  Heartbeat,

  Disconnect,

  Resend,

  SessionExpired,

  WebsocketChannelVerify,

  WebsocketChannelOpen,

  WebsocketNegotiationFailed,

  Unknown;
}
