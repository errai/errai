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

package org.jboss.errai.bus.server;

import org.jboss.errai.bus.client.api.BooleanRoutingRule;
import org.jboss.errai.bus.client.api.MessageBus;
import org.jboss.errai.bus.client.api.Payload;
import org.jboss.errai.bus.server.service.ErraiServiceConfigurator;

/**
 * This interface, <tt>ServerMessageBus</tt>, extends the client's {@link org.jboss.errai.bus.client.api.MessageBus},
 * and adds functionality so the server can transmit messages to a client and vice versa
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
    public Payload nextMessage(Object sessionContext, boolean wait);

    /**
     * Gets the queue containing the messages that are waiting to be transmitted
     *
     * @param sessionId - the session id of the queue
     * @return the message queue needed
     */
    public MessageQueue getQueue(String sessionId);

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
    public Scheduler getScheduler();

    /**
     * Configures the bus using the configuration specified
     *
     * @param service - the configuration to use
     */
    public void configure(ErraiServiceConfigurator service);
}
