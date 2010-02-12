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

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.jboss.errai.bus.client.*;
import org.jboss.errai.bus.client.protocols.BusCommands;
import org.jboss.errai.bus.client.protocols.MessageParts;
import org.jboss.errai.bus.server.io.JSONMessageServer;
import org.jboss.errai.bus.server.service.ErraiServiceConfigurator;
import org.jboss.errai.bus.server.util.ErrorHelper;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.Queue;

import static org.jboss.errai.bus.client.MessageBuilder.createConversation;
import static org.jboss.errai.bus.client.protocols.MessageParts.ReplyTo;
import static org.jboss.errai.bus.client.protocols.SecurityCommands.MessageNotDelivered;
import static org.jboss.errai.bus.server.util.ErrorHelper.handleMessageDeliveryFailure;
import static org.jboss.errai.bus.server.util.ServerBusUtils.encodeJSON;

/**
 * The <tt>ServerMessageBusImpl</tt> implements the <tt>ServerMessageBus</tt>, making it possible for the server to
 * send and receive messages
 */
@Singleton
public class ServerMessageBusImpl implements ServerMessageBus {
    private static final String ERRAI_BUS_SHOWMONITOR = "errai.bus.showmonitor";
    private static final String ERRAI_BUS_QUEUESIZE = "errai.bus.queuesize";

    private final static int DEFAULT_QUEUE_SIZE = 25;

    private int queueSize = DEFAULT_QUEUE_SIZE;

    private final List<MessageListener> listeners = new ArrayList<MessageListener>();

    private final Map<String, List<MessageCallback>> subscriptions = new HashMap<String, List<MessageCallback>>();
    private final Map<String, Set<MessageQueue>> remoteSubscriptions = new HashMap<String, Set<MessageQueue>>();

    private final Map<Object, MessageQueue> messageQueues = new HashMap<Object, MessageQueue>();

    private final List<SubscribeListener> subscribeListeners = new LinkedList<SubscribeListener>();
    private final List<UnsubscribeListener> unsubscribeListeners = new LinkedList<UnsubscribeListener>();

    private final Scheduler houseKeeper = new Scheduler();

    /**
     * Sets up the <tt>ServerMessageBusImpl</tt> with the configuration supplied. Also, initializes the bus' callback
     * functions, scheduler, and monitor
     * <p/>
     * When deploying services on the server-side, it is possible to obtain references to the
     * <tt>ErraiServiceConfigurator</tt> by declaring it as injection dependencies
     *
     * @param config - the configuration used to initialize the server message bus
     */
    @Inject
    public ServerMessageBusImpl(ErraiServiceConfigurator config) {
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    JFrame frame = new JFrame() {
                        {
                            setTitle("Errai Bus Monitor");
                            setResizable(true);
                        }
                    };

                    final JTextArea area = new JTextArea();
                    area.setDisabledTextColor(Color.BLACK);
                    area.setEnabled(false);
                    area.setFont(area.getFont().deriveFont(9f));

                    frame.getContentPane().add(area);

                    frame.pack();
                    frame.setVisible(true);
                    frame.setSize(400, 600);

                    StringBuilder builder;

                    //noinspection InfiniteLoopStatement
                    while (true) {
                        Thread.sleep(200);

                        builder = new StringBuilder().append("LOCAL ENDPOINTS :")
                                .append(subscriptions.size()).append("\n");

                        for (String endPointName : subscriptions.keySet()) {
                            builder.append(" [").append(subscriptions.get(endPointName).size()).append("] ")
                                    .append(endPointName).append("\n");
                        }

                        builder.append("REMOTE ENDPOINTS: ")
                                .append(remoteSubscriptions.size()).append("\n");
                        for (String endPointName : remoteSubscriptions.keySet()) {
                            builder.append(" [").append(remoteSubscriptions.get(endPointName).size()).append("] ")
                                    .append(endPointName).append("\n");

                        }

                        builder.append("\nQUEUES\n");

                        for (Object queue : messageQueues.keySet()) {
                            MessageQueue mq = messageQueues.get(queue);

                            builder.append("   __________________________").append("\n");
                            Queue<MarshalledMessage> q = mq.getQueue();

                            builder.append("   Queue: ").append(queue).append(" (size:").append(q.size()).append("; active:")
                                    .append(mq.isActive()).append("; stale:").append(mq.isStale()).append(")")
                                    .append(q.size() == DEFAULT_QUEUE_SIZE ? " ** QUEUE FULL (BLOCKING) **" : "").append("\n");
                            for (MarshalledMessage message : q) {
                                builder.append("     -> @").append(message.getSubject()).append(" = ").append(message.getMessage()).append("\n");
                            }

                        }

                        area.setText(builder.append("\n").toString());
                    }
                }
                catch (InterruptedException e) {
                    return;
                }
                catch (ConcurrentModificationException e) {
                    run();
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        /**
         * If we're in development mode, start the monitor.
         */
        if (Boolean.getBoolean(ERRAI_BUS_SHOWMONITOR)) {
            thread.setPriority(Thread.MIN_PRIORITY);
            thread.start();
        }

        final ServerMessageBusImpl busInst = this;

        subscribe("ServerBus", new MessageCallback() {
            public void callback(Message message) {
                String sessionId = getSessionId(message);
                MessageQueue queue;

                switch (BusCommands.valueOf(message.getCommandType())) {
                    case Heartbeat:
                        if (messageQueues.containsKey(sessionId)) {
                            messageQueues.get(sessionId).heartBeat();
                        }
                        break;

                    case RemoteSubscribe:
                        remoteSubscribe(sessionId, messageQueues.get(sessionId),
                                message.get(String.class, MessageParts.Subject));
                        break;

                    case RemoteUnsubscribe:
                        remoteUnsubscribe(sessionId, messageQueues.get(sessionId),
                                message.get(String.class, MessageParts.Subject));
                        break;

                    case ConnectToQueue:

                        if (messageQueues.containsKey(sessionId)) {
                            messageQueues.get(sessionId).stopQueue();
                        }

                        messageQueues.put(sessionId,
                                queue = new MessageQueue(queueSize, busInst));

                        remoteSubscribe(sessionId, queue, "ClientBus");

                        for (String service : subscriptions.keySet()) {
                            if (service.startsWith("local:")) {
                                continue;
                            }

                            createConversation(message)
                                    .toSubject("ClientBus")
                                    .command(BusCommands.RemoteSubscribe)
                                    .with(MessageParts.Subject, service)
                                    .noErrorHandling().sendNowWith(busInst, false);
                        }

                        createConversation(message)
                                .toSubject("ClientBus")
                                .command(BusCommands.FinishStateSync)
                                .noErrorHandling().sendNowWith(busInst, false);


                        /**
                         * Now the session is established, turn WindowPolling on.
                         */
                        getQueue(sessionId).setWindowPolling(true);

                        break;
                }
            }
        });

        houseKeeper.addTask(new TimedTask() {
            {
                this.period = (1000 * 10);
            }

            public void run() {
                boolean houseKeepingPerformed = false;
                List<MessageQueue> endSessions = new LinkedList<MessageQueue>();

                while (!houseKeepingPerformed) {
                    try {
                        Iterator<MessageQueue> iter = busInst.messageQueues.values().iterator();
                        MessageQueue q;
                        while (iter.hasNext()) {
                            if ((q = iter.next()).isStale()) {
                                iter.remove();
                                endSessions.add(q);
                            }
                        }

                        houseKeepingPerformed = true;
                    }
                    catch (ConcurrentModificationException cme) {
                        // fall-through and try again.
                    }
                }

                for (MessageQueue ref : endSessions) {
                    for (String subject : new HashSet<String>(busInst.remoteSubscriptions.keySet())) {
                        busInst.remoteUnsubscribe("Housekeeper", ref, subject);
                    }

                    busInst.messageQueues.remove(ref);
                }
            }

            @Override
            public String toString() {
                return "Bus Housekeeper";
            }
        });

        houseKeeper.start();
    }

    /**
     * Configures the server message bus with the specified <tt>ErraiServiceConfigurator</tt>. It only takes the queue
     * size specified by the configuration
     *
     * @param config
     */
    public void configure(ErraiServiceConfigurator config) {
        queueSize = DEFAULT_QUEUE_SIZE;
        if (config.hasProperty(ERRAI_BUS_QUEUESIZE)) {
            queueSize = Integer.parseInt(config.getProperty(ERRAI_BUS_QUEUESIZE));
        }
    }

    /**
     * Sends a message globally to all subscriptions containing the same subject as the specified message.
     *
     * @param message - The message to be sent.
     */
    public void sendGlobal(final Message message) {
        message.commit();
        final String subject = message.getSubject();
        if (!subscriptions.containsKey(subject) && !remoteSubscriptions.containsKey(subject)) {
            throw new NoSubscribersToDeliverTo("for: " + subject + " [commandType:" + message.getCommandType() + "]");
        }

        if (!fireGlobalMessageListeners(message)) {
            if (message.hasPart(ReplyTo) && message.hasResource("Session")) {
                /**
                 * Inform the sender that we did not dispatchGlobal the message.
                 */

                Map<String, Object> rawMsg = new HashMap<String, Object>();
                rawMsg.put(MessageParts.CommandType.name(), MessageNotDelivered.name());

                try {
                enqueueForDelivery(getSessionId(message),
                        message.get(String.class, ReplyTo),
                        encodeJSON(rawMsg));
                }
                catch (NoSubscribersToDeliverTo nstdt) {
                    handleMessageDeliveryFailure(this, message, nstdt.getMessage(), nstdt, false);
                }
            }

            return;
        }

        final String jsonMessage = message instanceof HasEncoded ? ((HasEncoded) message).getEncoded() :
                encodeJSON(message.getParts());

        if (subscriptions.containsKey(subject)) {
            for (MessageCallback c : subscriptions.get(subject)) {
                c.callback(message);
            }
        }

        if (remoteSubscriptions.containsKey(subject)) {
            for (Map.Entry<Object, MessageQueue> entry : messageQueues.entrySet()) {
                if (remoteSubscriptions.get(subject).contains(entry.getValue())) {
                    messageQueues.get(entry.getKey()).offer(new MarshalledMessage() {
                        public String getSubject() {
                            return subject;
                        }

                        public Object getMessage() {
                            return jsonMessage;
                        }
                    });
                }
            }
        }
    }

    /**
     * Sends the <tt>message</tt>
     *
     * @param message - the message to send
     */
    public void send(Message message) {
        message.commit();
        if (message.hasResource("Session")) {
            send(getSessionId(message), message, true);
        } else if (message.hasPart(MessageParts.SessionID)) {
            send(message.get(String.class, MessageParts.SessionID), message, true);
        } else {
            sendGlobal(message);
        }
    }

    /**
     * Parses the message appropriately and enqueues it for delivery
     *
     * @param message       - the message to be sent
     * @param fireListeners - true if all listeners attached should be notified of delivery
     */
    public void send(Message message, boolean fireListeners) {
        message.commit();
        if (!message.hasResource("Session")) {
            handleMessageDeliveryFailure(this, message, "cannot automatically route message. no session contained in message.", null, false);
        }

        String sessionId = getSessionId(message);

        if (sessionId == null) {
            handleMessageDeliveryFailure(this, message, "cannot automatically route message. no session contained in message.", null, false);

        }

        send(message.hasPart(MessageParts.SessionID) ? message.get(String.class, MessageParts.SessionID) :
                sessionId, message, fireListeners);
    }

    private void send(String sessionid, Message message, boolean fireListeners) {
        try {
            if (fireListeners && !fireGlobalMessageListeners(message)) {
                if (message.hasPart(ReplyTo)) {
                    Map<String, Object> rawMsg = new HashMap<String, Object>();
                    rawMsg.put(MessageParts.CommandType.name(), MessageNotDelivered.name());
                    enqueueForDelivery(sessionid, message.get(String.class, ReplyTo),
                            encodeJSON(rawMsg));
                }
                return;
            }

            enqueueForDelivery(sessionid, message.getSubject(), message instanceof HasEncoded ?
                    ((HasEncoded) message).getEncoded() :
                    encodeJSON(message.getParts()));
        }
        catch (NoSubscribersToDeliverTo nstdt) {
            // catch this so we can get a full trace
            handleMessageDeliveryFailure(this, message, nstdt.getMessage(), nstdt, false);
        }
    }

    private void enqueueForDelivery(final String sessionId, final String subject, final Object message) {
        MessageQueue queue = messageQueues.get(sessionId);
        if (queue != null && isAnyoneListening(queue, subject)) {
            queue.offer(new MarshalledMessage() {
                public String getSubject() {
                    return subject;
                }

                public Object getMessage() {
                    return message;
                }
            });
        } else {
            throw new NoSubscribersToDeliverTo("for: " + subject);
        }
    }

    /**
     * Gets the next message in the form of a <tt>Payload</tt>, which contains one-or-more messages that need to be
     * sent.
     *
     * @param sessionContext - key of messages. Only want to obtain messages that have the same <tt>sessionContext</tt>
     * @param wait           - set to true if the bus will wait for the next message
     * @return the <tt>Payload</tt> containing the next messages to be sent
     */
    public Payload nextMessage(Object sessionContext, boolean wait) {
        try {
            return messageQueues.get(sessionContext).poll(wait);
        }
        catch (MessageQueueExpired e) {
            MessageQueue mq = messageQueues.get(sessionContext);

            if (mq != null) {
                // terminate the queue
                messageQueues.remove(sessionContext);
            }
            throw e;
        }
    }

    /**
     * Gets the queue corresponding to the session id given
     *
     * @param sessionId - the session id of the queue
     * @return the message queue
     */
    public MessageQueue getQueue(String sessionId) {
        return messageQueues.get(sessionId);
    }

    /**
     * Closes the queue with <tt>sessionContext</tt>
     *
     * @param sessionContext - the session context of the queue to close
     */
    public void closeQueue(String sessionContext) {
        MessageQueue q = getQueue(sessionContext);
        for (Set<MessageQueue> sq : remoteSubscriptions.values()) {
            sq.remove(q);
        }
        messageQueues.remove(sessionContext);
    }

    /**
     * Closes the message queue
     *
     * @param queue - the message queue to close
     */
    public void closeQueue(MessageQueue queue) {
        for (Set<MessageQueue> sq : remoteSubscriptions.values()) {
            sq.remove(queue);
        }

        messageQueues.values().remove(queue);
    }

    /**
     * Adds a rule for a specific subscription. The <tt>BooleanRoutingRule</tt> determines if a message should
     * be routed based on the already specified rules or not.
     *
     * @param subject - the subject of the subscription
     * @param rule    - the <tt>BooleanRoutingRule</tt> instance specifying the routing rules
     */
    public void addRule(String subject, BooleanRoutingRule rule) {
        List<MessageCallback> newCallbacks = new LinkedList<MessageCallback>();
        Iterator<MessageCallback> iter = subscriptions.get(subject).iterator();
        while (iter.hasNext()) {
            newCallbacks.add(new RuleDelegateMessageCallback(iter.next(), rule));
            iter.remove();
        }

        List<MessageCallback> slist = subscriptions.get(subject);
        for (MessageCallback mc : newCallbacks) {
            slist.add(mc);
        }
    }

    /**
     * Adds a subscription
     *
     * @param subject  - the subject to subscribe to
     * @param receiver - the callback function called when a message is dispatched
     */
    public void subscribe(String subject, MessageCallback receiver) {
        if (!subscriptions.containsKey(subject)) {
            subscriptions.put(subject, new ArrayList<MessageCallback>());
        }

        fireSubscribeListeners(new SubscriptionEvent(false, null, subject));

        subscriptions.get(subject).add(receiver);
    }

    /**
     * Adds a new remote subscription and fires subscription listeners
     *
     * @param sessionContext - session context of queue
     * @param queue          - the message queue
     * @param subject        - the subject to subscribe to
     */
    public void remoteSubscribe(String sessionContext, MessageQueue queue, String subject) {
        if (subscriptions.containsKey(subject) || subject == null) return;

        fireSubscribeListeners(new SubscriptionEvent(true, sessionContext, subject));

        if (!remoteSubscriptions.containsKey(subject)) {
            remoteSubscriptions.put(subject, new HashSet<MessageQueue>());
        }
        remoteSubscriptions.get(subject).add(queue);
    }

    /**
     * Unsubscribes a remote subsciption and fires the appropriate listeners
     *
     * @param sessionContext - session context of queue
     * @param queue          - the message queue
     * @param subject        - the subject to unsubscribe from
     */
    public void remoteUnsubscribe(Object sessionContext, MessageQueue queue, String subject) {
        if (!remoteSubscriptions.containsKey(subject)) {
            return;
        }

        try {
            fireUnsubscribeListeners(new SubscriptionEvent(true, sessionContext, subject));
        }
        catch (Exception e) {
            e.printStackTrace();
            System.out.println("Exception running listeners");
            return;
        }

        Set<MessageQueue> sessionsToSubject = remoteSubscriptions.get(subject);

        sessionsToSubject.remove(queue);

        if (sessionsToSubject.isEmpty()) {
            remoteSubscriptions.remove(subject);
        }

        /**
         * Any messages still in the queue for this subject, will now never be delivered.  So we must purge them,
         * like the unwanted and forsaken messages they are.
         */
        Iterator<MarshalledMessage> iter = queue.getQueue().iterator();
        while (iter.hasNext()) {
            if (subject.equals(iter.next().getSubject())) {
                iter.remove();
            }
        }
    }

    /**
     * Unsubscribe all subscriptions attached to <tt>subject</tt>
     *
     * @param subject - the subject to unsubscribe from
     */
    public void unsubscribeAll(String subject) {
        throw new RuntimeException("unsubscribeAll not yet implemented.");
    }

    /**
     * Starts a conversation using the specified message
     *
     * @param message  - the message to initiate the conversation
     * @param callback - the message's callback function
     */
    public void conversationWith(Message message, MessageCallback callback) {
        throw new RuntimeException("conversationWith not yet implemented.");
    }

    /**
     * Checks if a subscription exists for <tt>subject</tt>
     *
     * @param subject - the subject to search the subscriptions for
     * @return true if a subscription exists
     */
    public boolean isSubscribed(String subject) {
        return subscriptions.containsKey(subject);
    }

    private boolean isAnyoneListening(MessageQueue queue, String subject) {
        return subscriptions.containsKey(subject) ||
                (remoteSubscriptions.containsKey(subject) && remoteSubscriptions.get(subject).contains(queue));
    }

    private boolean fireGlobalMessageListeners(Message message) {
        boolean allowContinue = true;

        for (MessageListener listener : listeners) {
            if (!listener.handleMessage(message)) {
                allowContinue = false;
            }
        }

        return allowContinue;
    }

    private void fireSubscribeListeners(SubscriptionEvent event) {
        Iterator<SubscribeListener> iter = subscribeListeners.iterator();

        event.setDisposeListener(false);

        while (iter.hasNext()) {
            iter.next().onSubscribe(event);
            if (event.isDisposeListener()) {
                iter.remove();
                event.setDisposeListener(false);
            }
        }
    }

    private void fireUnsubscribeListeners(SubscriptionEvent event) {
        Iterator<UnsubscribeListener> iter = unsubscribeListeners.iterator();

        event.setDisposeListener(false);

        while (iter.hasNext()) {
            iter.next().onUnsubscribe(event);
            if (event.isDisposeListener()) {
                iter.remove();
                event.setDisposeListener(false);
            }
        }
    }

    /**
     * Adds a global listener
     *
     * @param listener - global listener to add
     */
    public void addGlobalListener(MessageListener listener) {
        listeners.add(listener);
    }

    /**
     * Adds subscription listener
     *
     * @param listener - subscription listener to add
     */
    public void addSubscribeListener(SubscribeListener listener) {
        subscribeListeners.add(listener);
    }

    /**
     * Adds unsubscription listener
     *
     * @param listener - adds an unsubscription listener
     */
    public void addUnsubscribeListener(UnsubscribeListener listener) {
        unsubscribeListeners.add(listener);
    }

    private static String getSessionId(Message message) {
        return message.getResource(QueueSession.class, "Session").getSessionId();
    }

    /**
     * Gets all the message queues
     *
     * @return a map of the message queues that exist
     */
    public Map<Object, MessageQueue> getMessageQueues() {
        return messageQueues;
    }

    /**
     * Gets the scheduler being used for the housekeeping
     *
     * @return the scheduler
     */
    public Scheduler getScheduler() {
        return houseKeeper;
    }

    private final MessageProvider provider = new MessageProvider() {
        {
            MessageBuilder.setProvider(this);
        }

        public Message get() {
            return JSONMessageServer.create();
        }
    };

    /**
     * Gets the current message provider
     *
     * @return the message provider
     */
    public MessageProvider getMessageProvider() {
        return provider;
    }
}