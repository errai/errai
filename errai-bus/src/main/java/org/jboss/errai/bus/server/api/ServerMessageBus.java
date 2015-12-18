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

package org.jboss.errai.bus.server.api;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import org.jboss.errai.bus.client.api.QueueSession;
import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.bus.client.api.messaging.MessageBus;
import org.jboss.errai.bus.client.api.messaging.MessageCallback;
import org.jboss.errai.bus.server.service.ErraiServiceConfigurator;

/**
 * This interface, <tt>ServerMessageBus</tt>, extends the client's {@link org.jboss.errai.bus.client.api.messaging.MessageBus},
 * and adds functionality so the server can transmit messages to a client and vice versa
 *
 * @author Mike Brock
 */
public interface ServerMessageBus extends MessageBus {


  /**
   * Gets the queue containing the messages that are waiting to be transmitted
   *
   * @param session - the session of the queue
   * @return the message queue needed
   */
  public MessageQueue getQueue(QueueSession session);

  /**
   * Closes the queue associated with the <tt>sessionId</tt>
   *
   * @param sessionId - the session id of the message queue
   */
  public void closeQueue(String sessionId);

  /**
   * Closes the specified message queue
   *
   * @param queue - the message queue to close
   */
  public void closeQueue(MessageQueue queue);

  /**
   * Returns the associated scheduler which keeps track of timed events
   *
   * @return the <tt>Scheduler</tt> associated with this bus
   */
  public ExecutorService getScheduler();

  /**
   * Register a {@link org.jboss.errai.bus.server.api.QueueClosedListener} with the bus.
   *
   * @param listener a instance of the listener
   */
  public void addQueueClosedListener(QueueClosedListener listener);

  //  public TimedTask scheduleForSession(QueueSession session, TimeUnit unit, int time, Runnable task);

  /**
   * Configures the bus using the configuration specified
   *
   * @param service - the configuration to use
   */
  public void configure(ErraiServiceConfigurator service);

  /**
   * Get a collection of all receivers registered for a specificed subject
   *
   * @param subject The subject.
   * @return
   */
  public Collection<MessageCallback> getReceivers(String subject);

  public boolean hasRemoteSubscriptions(String subject);

  public boolean hasRemoteSubscription(String sessionId, String subject);

  public Map<QueueSession, MessageQueue> getMessageQueues();

  public MessageQueue getQueueBySession(String id);

  public QueueSession getSessionBySessionId(String id);

  public void associateNewQueue(QueueSession oldSession, QueueSession newSession);

  public Message getDeadLetterMessage(String messageId);

  public boolean removeDeadLetterMessage(String messageId);

  /**
   * Stop the MessateBus.
   */
  public void stop();
}
