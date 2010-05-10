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
import org.jboss.errai.bus.client.api.*;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.api.base.RuleDelegateMessageCallback;
import org.jboss.errai.bus.client.framework.*;
import org.jboss.errai.bus.client.protocols.BusCommands;
import org.jboss.errai.bus.client.protocols.MessageParts;
import org.jboss.errai.bus.server.api.*;
import org.jboss.errai.bus.server.async.SchedulerService;
import org.jboss.errai.bus.server.async.SimpleSchedulerService;
import org.jboss.errai.bus.server.async.TimedTask;
import org.jboss.errai.bus.server.io.JSONMessageServer;
import org.jboss.errai.bus.server.service.ErraiServiceConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.jboss.errai.bus.client.api.base.MessageBuilder.createConversation;
import static org.jboss.errai.bus.client.protocols.MessageParts.ReplyTo;
import static org.jboss.errai.bus.client.protocols.SecurityCommands.MessageNotDelivered;
import static org.jboss.errai.bus.client.util.ErrorHelper.handleMessageDeliveryFailure;
import static org.jboss.errai.bus.server.util.ServerBusUtils.encodeJSON;

/**
 * The <tt>ServerMessageBusImpl</tt> implements the <tt>ServerMessageBus</tt>, making it possible for the server to
 * send and receive messages
 *
 * @author Mike Brock
 */
@Singleton
public class ServerMessageBusImpl implements ServerMessageBus {
    private static final String ERRAI_BUS_QUEUESIZE = "errai.bus.queuesize";

    private final static int DEFAULT_QUEUE_SIZE = 25;

    private int queueSize = DEFAULT_QUEUE_SIZE;

    private final List<MessageListener> listeners = new ArrayList<MessageListener>();

    private final Map<String, List<MessageCallback>> subscriptions = new ConcurrentHashMap<String, List<MessageCallback>>();
    private final Map<String, Set<MessageQueue>> remoteSubscriptions = new ConcurrentHashMap<String, Set<MessageQueue>>();

    private final Map<QueueSession, MessageQueue> messageQueues = new ConcurrentHashMap<QueueSession, MessageQueue>();
    private final Map<MessageQueue, List<MarshalledMessage>> deferredQueue = new ConcurrentHashMap<MessageQueue, List<MarshalledMessage>>();
    private final Map<String, QueueSession> sessionLookup = new ConcurrentHashMap<String, QueueSession>();

    private final List<SubscribeListener> subscribeListeners = new LinkedList<SubscribeListener>();
    private final List<UnsubscribeListener> unsubscribeListeners = new LinkedList<UnsubscribeListener>();
    private final List<QueueClosedListener> queueClosedListeners = new LinkedList<QueueClosedListener>();

    private final SchedulerService houseKeeper = new SimpleSchedulerService();

    private Logger log = LoggerFactory.getLogger(getClass());

    private BusMonitor busMonitor;

    private Set<String> lockDownServices = new HashSet<String>();

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
        subscribe("ServerBus", new MessageCallback() {
            public void callback(Message message) {
                QueueSession session = getSession(message);
                MessageQueueImpl queue;

                switch (BusCommands.valueOf(message.getCommandType())) {
                    case Heartbeat:
                        if (messageQueues.containsKey(session)) {
                            messageQueues.get(session).heartBeat();
                        }
                        break;

                    case RemoteSubscribe:
                        remoteSubscribe(session, messageQueues.get(session),
                                message.get(String.class, MessageParts.Subject));
                        break;

                    case RemoteUnsubscribe:
                        remoteUnsubscribe(session, messageQueues.get(session),
                                message.get(String.class, MessageParts.Subject));
                        break;

                    case FinishStateSync:
                        queue = (MessageQueueImpl) messageQueues.get(session);
                        queue.finishInit();
                        drainDeferredDeliveryQueue(queue);
                        break;

                    case ConnectToQueue:
                        if (messageQueues.containsKey(session)) {
                            messageQueues.get(session).stopQueue();
                        }

                        synchronized (messageQueues) {
                            addQueue(session, queue = new MessageQueueImpl(queueSize, ServerMessageBusImpl.this, session));
                            remoteSubscribe(session, queue, "ClientBus");
                        }

                        if (isMonitor()) {
                            busMonitor.notifyQueueAttached(session.getSessionId(), queue);
                        }

                        for (String service : subscriptions.keySet()) {
                            if (service.startsWith("local:")) {
                                continue;
                            }

                            createConversation(message)
                                    .toSubject("ClientBus")
                                    .command(BusCommands.RemoteSubscribe)
                                    .with(MessageParts.Subject, service)
                                    .noErrorHandling().sendNowWith(ServerMessageBusImpl.this, false);
                        }

                        createConversation(message)
                                .toSubject("ClientBus")
                                .command(BusCommands.FinishStateSync)
                                .noErrorHandling().sendNowWith(ServerMessageBusImpl.this, false);
                        /**
                         * Now the session is established, turn WindowPolling on.
                         */
                        getQueue(session).setWindowPolling(true);

                        break;
                }
            }
        });

        addSubscribeListener(new SubscribeListener() {
            public void onSubscribe(SubscriptionEvent event) {
                if (event.isRemote()) return;
                synchronized (messageQueues) {
                    if (messageQueues.isEmpty() || event.getSubject().startsWith("local:")) return;

                    MessageBuilder.createMessage()
                            .toSubject("ClientBus")
                            .command(BusCommands.RemoteSubscribe)
                            .with(MessageParts.Subject, event.getSubject())
                            .noErrorHandling().sendNowWith(ServerMessageBusImpl.this, false);
                }
            }
        });

        addUnsubscribeListener(new UnsubscribeListener() {
            public void onUnsubscribe(SubscriptionEvent event) {
                if (event.isRemote() || event.getSubject().startsWith("local:")) return;
                synchronized (messageQueues) {
                    if (messageQueues.isEmpty()) return;

                    MessageBuilder.createMessage()
                            .toSubject("ClientBus")
                            .command(BusCommands.RemoteUnsubscribe)
                            .with(MessageParts.Subject, event.getSubject())
                            .noErrorHandling().sendNowWith(ServerMessageBusImpl.this, false);
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
                        Iterator<MessageQueue> iter = ServerMessageBusImpl.this.messageQueues.values().iterator();
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
                    for (String subject : new HashSet<String>(ServerMessageBusImpl.this.remoteSubscriptions.keySet())) {
                        ServerMessageBusImpl.this.remoteUnsubscribe(ref.getSession(), ref, subject);
                    }

                    ServerMessageBusImpl.this.closeQueue(ref);
                    ref.getSession().endSession();
                    deferredQueue.remove(ref);
                }
            }

            @Override
            public String toString() {
                return "Bus Housekeeper";
            }
        });

        houseKeeper.start();
    }


    private void addQueue(QueueSession session, MessageQueueImpl queue) {
        messageQueues.put(session, queue);
        sessionLookup.put(session.getSessionId(), session);
    }

    /**
     * Configures the server message bus with the specified <tt>ErraiServiceConfigurator</tt>. It only takes the queue
     * size specified by the configuration
     *
     * @param config -
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
                    enqueueForDelivery(getQueueByMessage(message),
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

        if (isMonitor()) {
            if (message.isFlagSet(RoutingFlags.FromRemote)) {
                busMonitor.notifyIncomingMessageFromRemote(
                        message.getResource(QueueSession.class, "Session").getSessionId(), message);
            } else {
                if (subscriptions.containsKey(subject)) {
                    busMonitor.notifyInBusMessage(message);
                }

                if (remoteSubscriptions.containsKey(subject)) {
                    for (Map.Entry<QueueSession, MessageQueue> entry : messageQueues.entrySet()) {
                        busMonitor.notifyOutgoingMessageToRemote(entry.getValue().getSession().getSessionId(), message);
                    }
                }
            }
        }

        if (subscriptions.containsKey(subject)) {
            for (MessageCallback c : subscriptions.get(subject)) {
                c.callback(message);
            }
        }

        if (remoteSubscriptions.containsKey(subject)) {
            for (MessageQueue queue : remoteSubscriptions.get(subject)) {
                queue.offer(new MarshalledMessage() {
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

    /**
     * Sends the <tt>message</tt>
     *
     * @param message - the message to send
     */
    public void send(Message message) {
        message.commit();
        if (message.hasResource("Session")) {
            send(getQueueByMessage(message), message, true);
        } else if (message.hasPart(MessageParts.SessionID)) {
            send(getQueueBySession(message.get(String.class, MessageParts.SessionID)), message, true);
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

        final MessageQueue queue = getQueue(getSession(message));

        if (queue == null) {
            handleMessageDeliveryFailure(this, message, "cannot automatically route message. no session contained in message.", null, false);

        }

        send(message.hasPart(MessageParts.SessionID) ? getQueueBySession(message.get(String.class, MessageParts.SessionID)) :
                getQueueByMessage(message), message, fireListeners);
    }

    private void send(MessageQueue queue, Message message, boolean fireListeners) {
        try {
            if (fireListeners && !fireGlobalMessageListeners(message)) {
                if (message.hasPart(ReplyTo)) {
                    Map<String, Object> rawMsg = new HashMap<String, Object>();
                    rawMsg.put(MessageParts.CommandType.name(), MessageNotDelivered.name());
                    enqueueForDelivery(queue, message.get(String.class, ReplyTo),
                            encodeJSON(rawMsg));
                }
                return;
            }

            if (isMonitor()) {
                busMonitor.notifyOutgoingMessageToRemote(queue.getSession().getSessionId(), message);
            }

            enqueueForDelivery(queue, message.getSubject(), message instanceof HasEncoded ?
                    ((HasEncoded) message).getEncoded() :
                    encodeJSON(message.getParts()));
        }
        catch (NoSubscribersToDeliverTo nstdt) {
            // catch this so we can get a full trace
            handleMessageDeliveryFailure(this, message, nstdt.getMessage(), nstdt, false);
        }
    }

    private void enqueueForDelivery(final MessageQueue queue, final String subject, final Object message) {
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
            if (queue != null && !queue.isInitialized()) {
                deferDelivery(queue, new MarshalledMessage() {
                    public String getSubject() {
                        return subject;
                    }

                    public Object getMessage() {
                        return message;
                    }
                });
            } else
                throw new NoSubscribersToDeliverTo("for: " + subject + ":" + isAnyoneListening(queue, subject) + ":" + queue.isInitialized());
        }

    }

    private void deferDelivery(MessageQueue queue, MarshalledMessage message) {
        synchronized (deferredQueue) {
            if (!deferredQueue.containsKey(queue)) deferredQueue.put(queue, new ArrayList<MarshalledMessage>());
            deferredQueue.get(queue).add(message);
        }
    }

    private void drainDeferredDeliveryQueue(MessageQueue queue) {
        synchronized (deferredQueue) {
            if (deferredQueue.containsKey(queue)) {
                for (MarshalledMessage message : deferredQueue.get(queue)) {
                    queue.offer(message);
                }

                deferredQueue.remove(queue);
            }
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
    public Payload nextMessage(QueueSession sessionContext, boolean wait) {
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
     * @param session - the session id of the queue
     * @return the message queue
     */
    public MessageQueue getQueue(QueueSession session) {
        return messageQueues.get(session);
    }

    /**
     * Closes the queue with <tt>sessionId</tt>
     *
     * @param sessionId - the session context of the queue to close
     */
    public void closeQueue(String sessionId) {
        closeQueue(getQueueBySession(sessionId));
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
        sessionLookup.values().remove(queue.getSession());

        fireQueueCloseListeners(new QueueCloseEvent(queue));
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

        if (lockDownServices.contains(subject))
            throw new IllegalArgumentException("Attempt to modify lockdown service: " + subject);

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
    public void remoteSubscribe(QueueSession sessionContext, MessageQueue queue, String subject) {
        if (subscriptions.containsKey(subject) || subject == null) return;

        fireSubscribeListeners(new SubscriptionEvent(true, sessionContext.getSessionId(), subject));

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
    public void remoteUnsubscribe(QueueSession sessionContext, MessageQueue queue, String subject) {
        if (!remoteSubscriptions.containsKey(subject)) {
            return;
        }

        try {
            fireUnsubscribeListeners(new SubscriptionEvent(true, sessionContext.getSessionId(), subject));
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
        if (lockDownServices.contains(subject))
            throw new IllegalArgumentException("Attempt to modify lockdown service: " + subject);

        subscriptions.remove(subject);

        fireUnsubscribeListeners(new SubscriptionEvent(false, null, subject));
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
        if (isMonitor()) {
            busMonitor.notifyNewSubscriptionEvent(event);
        }

        synchronized (subscribeListeners) {
            event.setDisposeListener(false);

            for (Iterator<SubscribeListener> iter = subscribeListeners.iterator(); iter.hasNext();) {
                iter.next().onSubscribe(event);
                if (event.isDisposeListener()) {
                    iter.remove();
                    event.setDisposeListener(false);
                }
            }
        }

    }

    private void fireUnsubscribeListeners(SubscriptionEvent event) {
        if (isMonitor()) {
            busMonitor.notifyUnSubcriptionEvent(event);
        }

        synchronized (unsubscribeListeners) {
            event.setDisposeListener(false);

            for (Iterator<UnsubscribeListener> iter = unsubscribeListeners.iterator(); iter.hasNext();) {
                iter.next().onUnsubscribe(event);
                if (event.isDisposeListener()) {
                    iter.remove();
                    event.setDisposeListener(false);
                }
            }
        }
    }

    private void fireQueueCloseListeners(QueueCloseEvent event) {
        if (isMonitor()) {
            busMonitor.notifyQueueDetached(event.getQueue().getSession().getSessionId(), event.getQueue());
        }

        synchronized (queueClosedListeners) {
            event.setDisposeListener(false);

            for (Iterator<QueueClosedListener> iter = queueClosedListeners.iterator(); iter.hasNext();) {
                iter.next().onQueueClosed(event);
                if (event.isDisposeListener()) {
                    iter.remove();
                    event.setDisposeListener(false);
                }
            }
        }
    }


    /**
     * Adds a global listener
     *
     * @param listener - global listener to add
     */
    public void addGlobalListener(MessageListener listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }

    /**
     * Adds subscription listener
     *
     * @param listener - subscription listener to add
     */
    public void addSubscribeListener(SubscribeListener listener) {
        synchronized (subscribeListeners) {
            subscribeListeners.add(listener);
        }
    }

    /**
     * Adds unsubscription listener
     *
     * @param listener - adds an unsubscription listener
     */
    public void addUnsubscribeListener(UnsubscribeListener listener) {
        synchronized (unsubscribeListeners) {
            unsubscribeListeners.add(listener);
        }
    }

    private static QueueSession getSession(Message message) {
        return message.getResource(QueueSession.class, "Session");
    }

    private MessageQueue getQueueByMessage(Message message) {
        MessageQueue queue = getQueue(getSession(message));
        if (queue == null)
            throw new QueueUnavailableException("no queue available to send. (queue or session may have expired)");
        return queue;
    }

    private MessageQueue getQueueBySession(String sessionId) {
        return getQueue(sessionLookup.get(sessionId));
    }

    private QueueSession getSessionById(String sessionId) {
        return sessionLookup.get(sessionId);
    }

    /**
     * Gets all the message queues
     *
     * @return a map of the message queues that exist
     */
    public Map<QueueSession, MessageQueue> getMessageQueues() {
        return messageQueues;
    }

    /**
     * Gets the scheduler being used for the housekeeping
     *
     * @return the scheduler
     */
    public SchedulerService getScheduler() {
        return houseKeeper;
    }

    public void addQueueClosedListener(QueueClosedListener listener) {
        synchronized (queueClosedListeners) {
            queueClosedListeners.add(listener);
        }
    }

    private final MessageProvider provider = new MessageProvider() {
        {
            MessageBuilder.setMessageProvider(this);
        }

        public Message get() {
            return JSONMessageServer.create();
        }
    };

    public List<MessageCallback> getReceivers(String subject) {
        return Collections.unmodifiableList(subscriptions.get(subject));
    }

    private boolean isMonitor() {
        return this.busMonitor != null;
    }

    public void attachMonitor(BusMonitor monitor) {
        if (this.busMonitor != null) {
            log.warn("new monitor attached, but a monitor was already attached: old monitor has been detached.");
        }
        this.busMonitor = monitor;

        for (Map.Entry<QueueSession, MessageQueue> entry : messageQueues.entrySet()) {
            busMonitor.notifyQueueAttached(entry.getKey().getSessionId(), entry.getValue());
        }

        for (String subject : subscriptions.keySet()) {
            busMonitor.notifyNewSubscriptionEvent(new SubscriptionEvent(false, "None", subject));
        }
        for (Map.Entry<String, Set<MessageQueue>> entry : remoteSubscriptions.entrySet()) {
            for (MessageQueue queue : entry.getValue()) {
                busMonitor.notifyNewSubscriptionEvent(new SubscriptionEvent(true, queue.getSession().getSessionId(), entry.getKey()));
            }
        }

        monitor.attach(this);
    }

    public void lockdown() {
        lockDownServices.addAll(subscriptions.keySet());
    }
}