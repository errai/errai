package org.jboss.errai.bus.client.api;

import org.jboss.errai.bus.client.framework.ClientMessageBus;
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
   * The state of the bus is reset just before this event is delivered. If you
   * want local message delivery to continue working (as opposed to having all
   * message delivery--including local--deferred until the bus is connected
   * again) then use {@link ClientMessageBusImpl#setInitialized(boolean)} with a
   * value of <tt>true</tt> when receiving this event.
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
