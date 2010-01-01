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

import com.google.inject.Singleton;
import org.jboss.errai.bus.client.*;
import org.jboss.errai.bus.client.protocols.BusCommands;
import org.jboss.errai.bus.client.protocols.MessageParts;
import org.jboss.errai.bus.client.protocols.SecurityCommands;

import javax.servlet.http.HttpSession;
import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.Queue;

import static org.jboss.errai.bus.server.util.ServerBusUtils.encodeJSON;

@Singleton
public class ServerMessageBusImpl implements ServerMessageBus {
    private static final String ERRAI_BUS_SHOWMONITOR = "errai.bus.showmonitor";

    private final static int QUEUE_SIZE = 200;

    private final List<MessageListener> listeners = new ArrayList<MessageListener>();

    private final Map<String, List<MessageCallback>> subscriptions = new HashMap<String, List<MessageCallback>>();
    private final Map<String, Set<Object>> remoteSubscriptions = new HashMap<String, Set<Object>>();

    private final Map<Object, MessageQueue> messageQueues = new HashMap<Object, MessageQueue>();

    private final List<SubscribeListener> subscribeListeners = new LinkedList<SubscribeListener>();
    private final List<UnsubscribeListener> unsubscribeListeners = new LinkedList<UnsubscribeListener>();

    private final HouseKeeper houseKeeper = new HouseKeeper(this);

    private boolean busReady = false;

    public ServerMessageBusImpl() {
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
                                    .append(q.size() == QUEUE_SIZE ? " ** QUEUE FULL (BLOCKING) **" : "").append("\n");
                            for (MarshalledMessage message : q) {
                                builder.append("     -> @").append(message.getSubject()).append(" = ").append(message.getMessage()).append("\n");
                            }

                        }

                        area.setText(builder.append("\n").toString());
                    }
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
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

        subscribe("ServerBus", new MessageCallback() {
            public void callback(Message message) {
                String s = message.get(String.class, "Foo");

                switch (BusCommands.valueOf(message.getCommandType())) {
                    case RemoteSubscribe:
                        remoteSubscribe(getSession(message).getAttribute(WS_SESSION_ID),
                                message.get(String.class, MessageParts.Subject));
                        break;

                    case RemoteUnsubscribe:
                        remoteUnsubscribe(getSession(message).getAttribute(WS_SESSION_ID),
                                message.get(String.class, MessageParts.Subject));
                        break;

                    case ConnectToQueue:
                        System.out.println("[Bus] ConnectToQueue");

                        Object sessionContext = getSession(message).getAttribute(WS_SESSION_ID);

                        System.out.println("[Bus] Session = " + sessionContext);

                        if (!messageQueues.containsKey(getSession(message)))
                            messageQueues.put(sessionContext,
                                    new MessageQueue(QUEUE_SIZE));

                        System.out.println("[Bus] Queue started for = " + sessionContext);

                        remoteSubscribe(sessionContext, "ClientBus");

                        for (String service : subscriptions.keySet()) {
                            if (service.startsWith("local:")) {
                                continue;
                            }

                            send(ConversationMessage.create(message)
                                    .command(BusCommands.RemoteSubscribe)
                                    .set(MessageParts.Subject, service)
                                    .toSubject("ClientBus"), false);
                        }

                        send(ConversationMessage.create(message).command(BusCommands.FinishStateSync)
                                .toSubject("ClientBus"), false);

                        /**
                         * Now the session is established, turn WindowPolling on.
                         */
                        getQueue(sessionContext).setWindowPolling(true);

                        break;
                }
            }
        });

        houseKeeper.start();
        //    workerFactory.startPool();
    }

    public void sendGlobal(final Message message) {
        final String subject = message.getSubject();
        if (!subscriptions.containsKey(subject) && !remoteSubscriptions.containsKey(subject)) {
            throw new NoSubscribersToDeliverTo("for: " + subject + " [commandType:" + message.getCommandType() + "]");
        }

        if (!fireGlobalMessageListeners(message)) {
            if (message.hasPart(MessageParts.ReplyTo) && message.hasResource("Session")) {
                /**
                 * Inform the sender that we did not dispatchGlobal the message.
                 */

                enqueueForDelivery((String) getSession(message).getAttribute(WS_SESSION_ID),
                        message.get(String.class, MessageParts.ReplyTo),
                        encodeJSON(CommandMessage.create(SecurityCommands.MessageNotDelivered).getParts()));
            }

            return;
        }

        final String jsonMessage = encodeJSON(message.getParts());

        if (subscriptions.containsKey(subject)) {
            for (MessageCallback c : subscriptions.get(subject)) {
                c.callback(message);
            }
        }

        if (remoteSubscriptions.containsKey(subject)) {
            for (Map.Entry<Object, MessageQueue> entry : messageQueues.entrySet()) {
                if (remoteSubscriptions.get(subject).contains(entry.getKey())) {
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

    public void send(Message message) {
        if (message.hasResource("Session")) {
            send((String) getSession(message).getAttribute(WS_SESSION_ID), message, true);
        } else if (message.hasPart(MessageParts.SessionID)) {
            send(message.get(String.class, MessageParts.SessionID), message, true);
        } else {
            sendGlobal(message);
        }
    }

    public void send(Message message, boolean fireListeners) {
        if (!message.hasResource("Session")) {
            throw new RuntimeException("cannot automatically route message. no session contained in message.");
        }

        HttpSession session = getSession(message);

        if (session == null) {
            throw new RuntimeException("cannot automatically route message. no session contained in message.");
        }

        send(message.hasPart(MessageParts.SessionID) ? message.get(String.class, MessageParts.SessionID) :
                (String) session.getAttribute(WS_SESSION_ID), message, fireListeners);
    }

    private void send(String sessionid, Message message, boolean fireListeners) {
        if (fireListeners && !fireGlobalMessageListeners(message)) {
            if (message.hasPart(MessageParts.ReplyTo)) {
                enqueueForDelivery(sessionid, message.get(String.class, MessageParts.ReplyTo),
                        encodeJSON(CommandMessage.create(SecurityCommands.MessageNotDelivered).getParts()));
            }

            return;
        }

        enqueueForDelivery(sessionid, message.getSubject(), encodeJSON(message.getParts()));
    }

    private void enqueueForDelivery(final String sessionId, final String subject, final Object message) {
        if (messageQueues.containsKey(sessionId) && isAnyoneListening(sessionId, subject)) {
            messageQueues.get(sessionId).offer(new MarshalledMessage() {
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


    public Payload nextMessage(Object sessionContext, boolean wait) {
        return messageQueues.get(sessionContext).poll(wait);
    }

    public MessageQueue getQueue(Object sessionContext) {
        return messageQueues.get(sessionContext);
    }

    public void closeQueue(Object sessionContext) {
        messageQueues.remove(sessionContext);
        remoteSubscriptions.remove(sessionContext);

    }

    public void addRule(String subject, BooleanRoutingRule rule) {
        Iterator<MessageCallback> iter = subscriptions.get(subject).iterator();

        List<MessageCallback> newCallbacks = new LinkedList<MessageCallback>();

        while (iter.hasNext()) {
            final MessageCallback mc = iter.next();
            iter.remove();
            newCallbacks.add(new RuleDelegateMessageCallback(mc, rule));
        }

        List<MessageCallback> slist = subscriptions.get(subject);
        for (MessageCallback mc : newCallbacks) {
            slist.add(mc);
        }
    }

    public void subscribe(String subject, MessageCallback receiver) {
        if (!subscriptions.containsKey(subject)) {
            subscriptions.put(subject, new ArrayList<MessageCallback>());
        }

        fireSubscribeListeners(new SubscriptionEvent(false, null, subject));

        subscriptions.get(subject).add(receiver);
    }

    public void remoteSubscribe(Object sessionContext, String subject) {
        if (subscriptions.containsKey(subject) || subject == null) return;

        fireSubscribeListeners(new SubscriptionEvent(true, sessionContext, subject));

        if (!remoteSubscriptions.containsKey(subject)) {
            remoteSubscriptions.put(subject, new HashSet<Object>());
        }
        remoteSubscriptions.get(subject).add(sessionContext);
    }

    public void remoteUnsubscribe(Object sessionContext, String subject) {
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

        Set<Object> sessionsToSubject = remoteSubscriptions.get(subject);

        sessionsToSubject.remove(sessionContext);

        if (sessionsToSubject.isEmpty()) {
            remoteSubscriptions.remove(subject);
        }

        /**
         * Any messages still in the queue for this subject, will now never be delivered.  So we must purge them,
         * like the unwanted and forsaken messages they are.
         */
        Iterator<MarshalledMessage> iter = messageQueues.get(sessionContext).getQueue().iterator();
        while (iter.hasNext()) {
            if (subject.equals(iter.next().getSubject())) {
                iter.remove();
            }
        }
    }

    public void unsubscribeAll(String subject) {
        throw new RuntimeException("unsubscribeAll not yet implemented.");
    }

    public void conversationWith(Message message, MessageCallback callback) {
        throw new RuntimeException("conversationWith not yet implemented.");
    }

    public boolean isSubscribed(String subject) {
        return subscriptions.containsKey(subject);
    }

    private boolean isAnyoneListening(Object sessionContext, String subject) {
        return subscriptions.containsKey(subject) ||
                (remoteSubscriptions.containsKey(subject) && remoteSubscriptions.get(subject).contains(sessionContext));
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

    public void addGlobalListener(MessageListener listener) {
        listeners.add(listener);
    }

    public void addSubscribeListener(SubscribeListener listener) {
        subscribeListeners.add(listener);
    }

    public void addUnsubscribeListener(UnsubscribeListener listener) {
        unsubscribeListeners.add(listener);
    }

    private static HttpSession getSession(Message message) {
        return ((HttpSession) message.getResource("Session"));
    }

    public HouseKeeper getHouseKeeper() {
        return houseKeeper;
    }

    public static class HouseKeeper extends Thread {
        private boolean running = true;
        private ServerMessageBusImpl bus;
        private List<TimedTask> tasks = new LinkedList<TimedTask>();

        public HouseKeeper(final ServerMessageBusImpl bus) {
            super();
            this.bus = bus;
            setPriority(Thread.MIN_PRIORITY);
            setDaemon(true);

            tasks.add(new TimedTask() {
                {
                    this.period = (1000 * 10);
                }

                public void run() {
                    boolean houseKeepingPerformed = false;
                    List<Object> endSessions = new LinkedList<Object>();

                    while (!houseKeepingPerformed) {
                        try {

                            Iterator<Object> iter = bus.messageQueues.keySet().iterator();
                            Object ref;

                            while (iter.hasNext()) {
                                if (bus.messageQueues.get(ref = iter.next()).isStale()) {
                                    endSessions.add(ref);
                                }
                            }

                            houseKeepingPerformed = true;
                        }
                        catch (ConcurrentModificationException cme) {
                            // fall-through and try again.
                        }
                    }

                    for (Object ref : endSessions) {
                        for (String subject : new HashSet<String>(bus.remoteSubscriptions.keySet())) {
                            bus.remoteUnsubscribe(ref, subject);
                        }

                        bus.messageQueues.remove(ref);
                    }
                }
            });
        }

        @Override
        public void run() {
            try {
                while (running) {
                    Thread.sleep(1000 * 1);
                    runAllDue();
                }
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        private void runAllDue() {
            for (TimedTask task : tasks) {
                task.runIfDue(System.currentTimeMillis());
            }
        }

        public void addTask(TimedTask task) {
            tasks.add(task);
        }
    }
}