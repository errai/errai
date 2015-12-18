/*
 * Copyright (C) 2015 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.bus.client.api;

import org.jboss.errai.bus.client.framework.ClientMessageBusImpl;

/**
 * Errai's {@link ClientMessageBus} has three conceptual states in its
 * lifecycle: <b>local only</b>, <b>connecting</b>, and <b>connected</b>. This
 * interface allows Errai applications to observe lifecycle state transitions in
 * the bus.
 * <p>
 * The bus lifecycle is as follows. Every time the bus transitions from one
 * state to another, the correspondingly named event is delivered to all
 * listeners:<br>
 * <img src="bus_lifecycle.png">
 * <p>
 * The bus begins in the <b>local only</b> state, and automatically transitions
 * to the <b>connecting</b> state unless configured not to. Therefore, unless
 * remote communication is disabled by static global configuration, application
 * code will first observe the bus in its <b>connecting</b> state. In the
 * <b>connecting</b> state, the bus "wants" to be connected to the server, and
 * it actively tries to establish a connection, reconnecting after communication
 * disruptions as necessary. When in the <b>connecting</b> state, the bus will
 * make a configurable number of attempts to reach the <b>connected</b> state.
 * After this many retries, the bus will give up and fall back to the </b>local
 * only</b> state.
 * <p>
 * Once the bus has established a connection with the server and exchanged the
 * list of available topics with the server bus, the bus is in the
 * <b>connected</b> state, and bidirectional communication between client and
 * server is possible.
 * <p>
 * If there is a communication error when the bus is in the <b>connected</b>
 * state, the bus falls back to the <b>connecting</b> state, where it attempts
 * to reconnect to the server.
 *
 * <h3>Recursive event delivery</h3>
 * <p>
 * If you call {ClientMessageBus#stop()} or {ClientMessageBus#init()} from
 * within one of these callback methods, be aware that this can cause another
 * lifecycle event to be delivered before the current event has finished being
 * delivered. This is especially important to avoid if your application employs
 * more than one BusLifecycleListener, because some of these listeners will
 * receive events out of order. An easy workaround for this problem is to wrap
 * your bus.init() or bus.stop() call in a Timer with a delay of 1ms.
 *
 * @author Jonathan Fuerth <jfuerth@redhat.com>
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public interface BusLifecycleListener {

  /**
   * Indicates that the bus is about to transition from the <b>local only</b> to
   * the <b>connecting</b> state. While this event is being delivered, it is
   * still permitted to change the remote endpoint URL of the server bus.
   *
   * @param e
   *          the object describing the event (includes a reference to the bus
   *          that fired the event).
   */
  void busAssociating(BusLifecycleEvent e);

  /**
   * Indicates that the bus is about to transition from the <b>connecting</b> to
   * the <b>local only</b> state. This can happen automatically due to too many
   * failed connection attempts, or because the application stopped the bus
   * explicitly.
   * <p>
   * When you want to try to connect to the server again (for example, to fail
   * over to another server, after a set timeout has elapsed, or in response to
   * the user clicking a "Reconnect" button in the user interface), call
   * {@link ClientMessageBusImpl#init()}. This will transition the bus back to
   * the <b>connecting</b> state.
   *
   * @param e
   *          the object describing the event (includes a reference to the bus
   *          that fired the event).
   */
  void busDisassociating(BusLifecycleEvent e);

  /**
   * Indicates that the bus has just transitioned from the <b>connecting</b> to
   * the <b>connected</b> state. At the time when this event is delivered, it is
   * possible to exchange messages with the remote bus.
   *
   * @param e
   *          the object describing the event (includes a reference to the bus
   *          that fired the event).
   */
  void busOnline(BusLifecycleEvent e);

  /**
   * Indicates that the bus has just transitioned from the <b>connected</b> to
   * the <b>connecting</b> state. In the <b>connecting</b> state, the bus will
   * continue to attempt to reconnect to the server. If the reconnect is
   * successful, you will receive a {@link #busOnline(BusLifecycleEvent)} event.
   * If the bus gives up trying to reconnect, you will receive a
   * {@link #busDisassociating(BusLifecycleEvent)} event.
   * <p>
   * At the time when this event is delivered, messages intended for the remote
   * bus will be enqueued for delivery when (and if) the bus goes back online.
   *
   * @param e
   *          the object describing the event (includes a reference to the bus
   *          that fired the event).
   */
  void busOffline(BusLifecycleEvent e);

}
