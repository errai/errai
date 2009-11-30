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

package org.jboss.errai.bus.client;


/**
 * A message bus is capable of transmitting and receiving messages using the publisher/subscriber
 * mode. All implementations of the bus should implement this interface for both the client-side and the server-side.
 */
public interface MessageBus {
    public static final String WS_SESSION_ID = "WSSessionID";

    /**
     * Transmits the message to all directly-peered buses (global in relation to this bus only).
     * @param message - The message to be sent.
     */
    public void sendGlobal(CommandMessage message);

    /**
     * Transmits a message.
     * @param message
     */
    public void send(CommandMessage message);

    /**
     * Transmits a message and may optionally supress message listeners from firing.  This is useful if you are
     * modifying a message from within a listener itself, and wish to retransmit the message.
     * @param message
     * @param fireListeners
     */
    public void send(CommandMessage message, boolean fireListeners);


    /**
     * Have a conversation with a remote service.
     * @param message
     * @param callback
     */
    public void conversationWith(CommandMessage message, MessageCallback callback);

    /**
     * Subscribe a listener to the specified subject.
     * @param subject
     * @param receiver
     */
    public void subscribe(String subject, MessageCallback receiver);

    /*
     ¥ Unsubscribe all listeners registered for the specified subject.
     */
    public void unsubscribeAll(String subject);


    /**
     * Returns true if there the specified subject has one or more listeners registered.
     * @param subject
     * @return
     */
    public boolean isSubscribed(String subject);

    /**
     * Registers a global listener, that can intercept all messages before they are transmitted.
     * @param listener
     */
    public void addGlobalListener(MessageListener listener);

    /**
     * Registers a subscription listener, which is fired whenever a new subscription is created.
     * @param listener
     */
    public void addSubscribeListener(SubscribeListener listener);

    /**
     * Registers an un-subscribe listener, which is fired whenever a subscription is cancelled.
     * @param listener
     */
    public void addUnsubscribeListener(UnsubscribeListener listener);
}
