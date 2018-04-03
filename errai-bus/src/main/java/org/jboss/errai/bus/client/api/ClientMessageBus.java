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

package org.jboss.errai.bus.client.api;

import java.util.Set;

import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.bus.client.api.messaging.MessageBus;
import org.jboss.errai.bus.client.api.messaging.MessageCallback;
import org.jboss.errai.bus.client.api.messaging.MessageInterceptor;

/**
 * An extended client-specific/in-browser interface of {@link org.jboss.errai.bus.client.api.messaging.MessageBus}, which defines client-specific functionality.
 *
 * @author Mike Brock
 */
public interface ClientMessageBus extends MessageBus {

  /**
   * Declares a new shadow subscription. Shadow subscriptions are specialized services
   * that are responsible for handling the request of remote services when the bus
   * is in a disconnected state.
   * <p>
   * When a message is sent by an application component to a remote service while the
   * bus is physically disconnected from the server, the bus will consider shadow
   * subscriptions with a matching subject name to deliver the message to.
   * <p>
   * Shadow subscriptions are considered routable in all bus states except
   * <tt>CONNECTING</tt>.
   *
   * @param subject the subject name.
   * @param callback
   * @return
   */
  public Subscription subscribeShadow(String subject, MessageCallback callback);

  /**
   * Adds the given listener instance to this bus. The listener will be notified
   * each time the bus transitions to a different lifecycle state.
   *
   * @param l
   *          The listener that wants to receive lifecycle notifications. Must
   *          not be null. If the same listener is added more than once, it will
   *          receive the corresponding number of callbacks upon each lifecycle
   *          event.
   */
  public void addLifecycleListener(BusLifecycleListener l);

  /**
   * Removes the given listener from this bus. The listener will no longer
   * receive lifecycle events from this bus.
   *
   * @param l
   *          The listener to remove. If the listener was added more than one
   *          time, removing it will decrease by one the number of notifications
   *          that listener receices for each event. If the listener was not
   *          already registered to receive events, this method has no effect.
   */
  public void removeLifecycleListener(BusLifecycleListener l);

  /**
   * Takes this bus out of the "local only" state, causing it to try and connect
   * with the server (unless remote communication is globally disabled).
   *
   * @see org.jboss.errai.bus.client.util.BusToolsCli#isRemoteCommunicationEnabled()
   */
  public void init();

  /**
   * Takes this bus into the "local only" state.
   *
   * @param sendDisconnectToServer
   *          if true, the server will be notified that we are breaking the
   *          connection. Else, no attempt will be made to notify the server.
   */
  public void stop(boolean sendDisconnectToServer);

  /**
   * Returns a set of all reject subjects in the bus.
   *
   * @return
   *        a set of all registered subjects.
   */
  public Set<String> getAllRegisteredSubjects();

  /**
   * Adds a global transport error handler to deal with any errors which arise
   * from communication between the bus and the server.
   *
   * @param errorHandler
   *          the error handler to add.
   */
  public void addTransportErrorHandler(TransportErrorHandler errorHandler);

  /**
   * Removes the given global transport error handler from this bus. Once
   * removed, the given handler does not receive transport error notifications
   * anymore.
   * 
   * @param errorHandler
   *          the error handler to remove. This method has no effect if the
   *          given handler is {@code null} or it was not already registered.
   */
  public void removeTransportErrorHandler(TransportErrorHandler errorHandler);

  /**
   * Register a global message interceptor to intercept all message bus messages.
   */
  public void addInterceptor(MessageInterceptor interceptor);

  /**
   * Remove a global message interceptor.
   */
  public void removeInterceptor(MessageInterceptor interceptor);

  /**
   * Sets a property on the bus.
   *
   * @param name
   *        the property name
   * @param value
   *        the property value
   */
  public void setProperty(String name, String value);

  public void clearProperties();

  /**
   * Delivers the given message to all local callbacks that subscribe to its
   * subject. Does not transmit the message to other buses.
   *
   * @param message
   *          The message to deliver to local subscribers.
   */
  public void sendLocal(Message message);

  String getSessionId();

  String getClientId();
}
