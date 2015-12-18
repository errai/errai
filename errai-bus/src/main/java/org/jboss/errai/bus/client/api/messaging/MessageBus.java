/*
 * Copyright (C) 2013 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.bus.client.api.messaging;

import org.jboss.errai.bus.client.api.BusMonitor;
import org.jboss.errai.bus.client.api.SubscribeListener;
import org.jboss.errai.bus.client.api.Subscription;
import org.jboss.errai.bus.client.api.UnsubscribeListener;

/**
 * A message bus is capable of transmitting and receiving messages using the publisher/subscriber
 * model. All implementations of the bus should implement this interface for both the client-side and the server-side.
 * <p/>
 * The MessageBus architecture uses a flat topology with automatic bus-to-bus routing which is accomplished through
 * automatic cross-subscription between federated buses.  However, each bus-to-bus federated relationship has only
 * a one-degree visibility with the federation.  This means that while, in practice, client buses may be
 * federated wth a server bus, each client bus can only see the server bus directly.
 * <p/>
 * <tt><pre>
 *                         _____________________
 *                        /     (Context B)     \
 *  _________         _________         _________
 * |        |        |        |        |        |
 * | Client | <----> | Server | <----> | Client |
 * |________|        |________|        |________|
 * \_____________________/
 *       (Context A)
 * </pre></tt>
 * The diagram shows two clients federated within the messaging topology.  The contexts indicate the scope by which
 * direct communication is possible.  In order to facilitate client-to-client communication, users must implement
 * relay services in the server manually.
 * <p/>
 * Services always live on the bus with which they a registered.  When a new registration occurs, the service
 * becomes generally available across the entire context.  This is accomplished by notifying the proximate bus--
 * in real-time--that a subscription has been created with a
 * {@link org.jboss.errai.bus.client.protocols.BusCommand#RemoteSubscribe}
 * command containing the subject that has just become routable.  Likewise, when a subject is unsubscribed, an
 * {@link org.jboss.errai.bus.client.protocols.BusCommand#RemoteUnsubscribe} is sent.
 * <p/>
 * Creating a service subscription is straight-forward:
 * <pre><code>
 * busInstance.subscribe("ServiceName",
 *                      new MessageCallback() {
 *                              public void callback(CommandMessage message) {
 *                                  // do something.
 *                              }
 *                      }
 * );
 * </code></pre>
 * The API for creating services is heterogeneous in both client and server code.  The only semantic difference involves
 * obtaining an instance of the <tt>MessageBus</tt> which is done using the
 * {@link org.jboss.errai.bus.client.ErraiBus#get()} method in client code,
 * and by default, is provided by the container using dependency injection in the server code.  For example:
 * <p/>
 * <pre><code>
 *
 * @Service public class MyService {
 * private MessageBus bus;
 * <p/>
 * @Inject public MyService(MessageBus bus) {
 * this.bus = bus;
 * }
 * <p/>
 * ...
 * }
 * </code></pre>
 *
 * @author Mike Brock
 */
public interface MessageBus {
  /**
   * Transmits the message to all directly-peered buses (global in relation to this bus only).
   *
   * @param message - The message to be sent.
   */
  public void sendGlobal(Message message);

  /**
   * Transmits a message.
   *
   * @param message
   */
  public void send(Message message);


  /**
   * Transmits a message and may optionally supress message listeners from firing.  This is useful if you are
   * modifying a message from within a listener itself, and wish to retransmit the message.
   *
   * @param message
   * @param fireListeners
   */
  public void send(Message message, boolean fireListeners);

  /**
   * Subscribe a listener to the specified subject.
   *
   * @param subject
   * @param receiver
   */
  public Subscription subscribe(String subject, MessageCallback receiver);


  /**
   * Subscribe a listern locally, but do not advertise or make available the service to remote buses.
   *
   * @param subject
   * @param receiver
   */
  public Subscription subscribeLocal(String subject, MessageCallback receiver);

  /**
  * Unsubscribe all listeners registered for the specified subject.
  */
  public void unsubscribeAll(String subject);

  /**
   * Returns true if there the specified subject has one or more listeners registered.
   *
   * @param subject
   * @return
   */
  public boolean isSubscribed(String subject);


  /**
   * Registers a subscription listener, which is fired whenever a new subscription is created.
   *
   * @param listener
   */
  public void addSubscribeListener(SubscribeListener listener);

  /**
   * Registers an un-subscribe listener, which is fired whenever a subscription is cancelled.
   *
   * @param listener
   */
  public void addUnsubscribeListener(UnsubscribeListener listener);

  /**
   * Attach a monitor to the bus.
   *
   * @param monitor
   */
  public void attachMonitor(BusMonitor monitor);
}
