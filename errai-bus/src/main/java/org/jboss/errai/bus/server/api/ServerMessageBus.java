/*
 * Copyright 2009 JBoss, a divison Red Hat, Inc
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

package org.jboss.errai.bus.server.api;

import org.jboss.errai.bus.client.api.MessageCallback;
import org.jboss.errai.bus.client.framework.BooleanRoutingRule;
import org.jboss.errai.bus.client.framework.MessageBus;
import org.jboss.errai.bus.client.framework.Payload;
import org.jboss.errai.bus.server.async.SchedulerService;
import org.jboss.errai.bus.server.service.ErraiServiceConfigurator;

import java.util.List;

/**
 * This interface, <tt>ServerMessageBus</tt>, extends the client's {@link org.jboss.errai.bus.client.framework.MessageBus},
 * and adds functionality so the server can transmit messages to a client and vice versa
 *
 * @author Mike Brock
 */
public interface ServerMessageBus extends MessageBus {

    /**
     * Gets the next message identifiable by the specified <tt>sessionContext</tt>, in the form of a <tt>Payload</tt>.
     * The payload contains all the messages that need to be transmitted.
     *
     * @param sessionContext - key of messages. Only want to obtain messages that have the same <tt>sessionContext</tt>
     * @param wait - set to true if the bus will wait for the next message
     * @return the <tt>Payload</tt> instance containing all the messages that need to be transmitted
     */
    @Deprecated
    public Payload nextMessage(QueueSession sessionContext, boolean wait);

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
     * Adds a rule for a specific subscription. The <tt>BooleanRoutingRule</tt> determines if a message should
     * be routed based on the already specified rules or not.
     *
     * @param subject - the subject of the subscription
     * @param rule - the <tt>BooleanRoutingRule</tt> instance specifying the routing rules
     */
    public void addRule(String subject, BooleanRoutingRule rule);

    /**
     * Returns the associated scheduler which keeps track of timed events
     *
     * @return the <tt>Scheduler</tt> associated with this bus
     */
    public SchedulerService getScheduler();

    /**
     * Register a {@link org.jboss.errai.bus.server.api.QueueClosedListener} with the bus.
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
     * @param subject The subject.
     * @return
     */
    public List<MessageCallback> getReceivers(String subject);
}
