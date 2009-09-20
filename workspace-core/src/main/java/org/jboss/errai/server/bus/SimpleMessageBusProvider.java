package org.jboss.errai.server.bus;

import org.jboss.errai.client.framework.MessageCallback;
import org.jboss.errai.client.rpc.CommandMessage;
import org.jboss.errai.client.rpc.protocols.SecurityParts;
import org.jboss.errai.server.MessageBusServiceImpl;
import static org.jboss.errai.server.bus.MessageBusServer.encodeMap;

import javax.servlet.http.HttpSession;
import static java.lang.Thread.currentThread;
import static java.lang.Thread.sleep;
import java.util.*;

public class SimpleMessageBusProvider implements MessageBusProvider {
    private static MessageBus bus;

    public MessageBus getBus() {
        if (bus == null) {
            bus = new SimpleMessageBus();
        }
        return bus;
    }

    public class SimpleMessageBus implements MessageBus {
        private final List<MessageListener> listeners = new ArrayList<MessageListener>();

        private final Map<String, List<MessageCallback>> subscriptions = new HashMap<String, List<MessageCallback>>();
        private final Map<String, List<Object>> remoteSubscriptions = new HashMap<String, List<Object>>();

        private final Map<Object, Queue<Message>> messageQueues = new HashMap<Object, Queue<Message>>();
        private final Map<Object, Thread> activeWaitingThreads = new HashMap<Object, Thread>();

        private final List<SubscribeListener> subscribeListeners = new LinkedList<SubscribeListener>();
        private final List<UnsubscribeListener> unsubscribeListeners = new LinkedList<UnsubscribeListener>();

        public SimpleMessageBus() {
            Thread thread = new Thread() {
                @Override
                public void run() {
                    try {
                        while (true) {
                            Thread.sleep(5000);

                            System.err.println();
                            System.err.println("[ MessageBus Status   ");
                            System.err.println("[ -----------------");
                            System.err.println("[ Remote Endpoints: " + remoteSubscriptions.size());
                            for (String endPointName : remoteSubscriptions.keySet()) {
                                System.err.println("[  __________________________");
                                System.err.println("[  Endpoint         : " + endPointName);
                                System.err.println("[  ClientsSubscribed: " + remoteSubscriptions.get(endPointName).size());
                            }

                            System.err.println("[");
                            System.err.println("[ Queues");
                            for (Object queue : messageQueues.keySet()) {
                                System.err.println("[  __________________________");
                                System.err.println("[  Queue: " + queue);
                                for (Message message : messageQueues.get(queue)) {
                                    System.err.println("[     -> @" + message.getSubject() + " = " + message.getMessage());
                                }
                            }

                            System.err.println();
                        }

                    }
                    catch (Exception e) {
                        e.printStackTrace();
                        System.out.println("Exit");
                        return;
                    }

                }
            };

            thread.setPriority(Thread.MIN_PRIORITY);
            thread.start();
        }

        public void storeGlobal(String subject, CommandMessage message) {
            storeGlobal(subject, message, true);
        }

        public void storeGlobal(final String subject, final CommandMessage message, boolean fireListeners) {
            if (!subscriptions.containsKey(subject) && !remoteSubscriptions.containsKey(subject)) {
                throw new NoSubscribersToDeliverTo("for: " + subject);
            }

            if (fireListeners && !fireGlobalMessageListeners(message)) return;

            final String jsonMessage = encodeMap(message.getParts());

            if (subscriptions.containsKey(subject)) {
                for (MessageCallback c : subscriptions.get(subject)) {
                    c.callback(message);
                }
            }

            if (remoteSubscriptions.containsKey(subject)) {
                for (Map.Entry<Object, Queue<Message>> entry : messageQueues.entrySet()) {
                    if (remoteSubscriptions.get(subject).contains(entry.getKey())) {
                        messageQueues.get(entry.getKey()).add(new Message() {
                            public String getSubject() {
                                return subject;
                            }

                            public Object getMessage() {
                                return jsonMessage;
                            }
                        });

                        Thread t = activeWaitingThreads.get(entry.getKey());
                        if (t != null) t.interrupt();
                    }
                }
            }
        }

        private void store(final String sessionId, final String subject, final Object message) {
            if (messageQueues.containsKey(sessionId) && isAnyoneListening(sessionId, subject)) {
                messageQueues.get(sessionId).add(new Message() {
                    public String getSubject() {
                        return subject;
                    }

                    public Object getMessage() {
                        return message;
                    }
                });

                Thread t = activeWaitingThreads.get(sessionId);
                if (t != null) t.interrupt();
            }
            else {
                throw new NoSubscribersToDeliverTo("for: " + subject);
            }
        }

        public void store(String sessionid, String subject, CommandMessage message) {
            store(sessionid, subject, message, true);
        }

        public void store(String sessionid, String subject, CommandMessage message, boolean fireListeners) {
            if (fireListeners && !fireGlobalMessageListeners(message)) return;

            store(sessionid, subject, encodeMap(message.getParts()));
        }

        public void store(String subject, CommandMessage message) {
            store(subject, message, true);
        }

        public void store(String subject, CommandMessage message, boolean fireListeners) {
            if (!message.hasPart(SecurityParts.SessionData)) {
                throw new RuntimeException("cannot automatically route message. no session contained in message.");
            }

            HttpSession session = message.get(HttpSession.class, SecurityParts.SessionData);

            if (session == null) {
                throw new RuntimeException("cannot automatically route message. no session contained in message.");
            }

            store((String) session.getAttribute(MessageBusServiceImpl.WS_SESSION_ID), subject, message, fireListeners);
        }

        public Message nextMessage(Object sessionContext) {
            Queue<Message> q = getQueue(sessionContext);

            if (q.isEmpty()) {
                holdThread(sessionContext);
                return q.poll();
            }
            else {
                return q.poll();
            }
        }

        private void holdThread(Object sessionContext) {
            try {
                activeWaitingThreads.put(sessionContext, currentThread());
                sleep(1000 * 45);
            }
            catch (InterruptedException e) {
                // passthru
            }
            finally {
                activeWaitingThreads.remove(sessionContext);
            }
        }

        private Queue<Message> getQueue(Object sessionContext) {
            if (!messageQueues.containsKey(sessionContext))
                messageQueues.put(sessionContext, new LinkedList<Message>());

            return messageQueues.get(sessionContext);
        }

        public void subscribe(String subject, MessageCallback receiver) {
            if (!subscriptions.containsKey(subject)) {
                subscriptions.put(subject, new ArrayList<MessageCallback>());
            }

            fireSubscribeListeners(new SubscriptionEvent(false, null, subject));

            subscriptions.get(subject).add(receiver);
        }

        public void remoteSubscribe(Object sessionContext, String subject) {
            //  System.out.println("RemoteSubscriptionRequest:" + subject);
            if (subscriptions.containsKey(subject) || subject == null) return;

            fireSubscribeListeners(new SubscriptionEvent(true, sessionContext, subject));

            if (!remoteSubscriptions.containsKey(subject)) {
                remoteSubscriptions.put(subject, new ArrayList<Object>());
            }
            remoteSubscriptions.get(subject).add(sessionContext);
        }

        public void remoteUnsubscribe(Object sessionContext, String subject) {
            if (!remoteSubscriptions.containsKey(subject)) return;

            fireUnsubscribeListeners(new SubscriptionEvent(true, sessionContext, subject));

            List<Object> sessionsToSubject = remoteSubscriptions.get(subject);
            sessionsToSubject.remove(sessionContext);

            if (sessionsToSubject.isEmpty()) {
                remoteSubscriptions.remove(subject);
            }
        }

        private boolean isAnyoneListening(Object sessionContext, String subject) {
            return subscriptions.containsKey(subject) ||
                    (remoteSubscriptions.containsKey(subject) && remoteSubscriptions.get(subject).contains(sessionContext));
        }

        public Set<String> getSubjects() {
            return subscriptions.keySet();
        }

        private boolean fireGlobalMessageListeners(CommandMessage message) {
            boolean allowContinue = true;

            for (MessageListener listener : listeners) {
                if (!listener.handleMessage(message)) {
                    allowContinue = false;
                }
            }

            return allowContinue;
        }

        private void fireSubscribeListeners(SubscriptionEvent event) {
            for (SubscribeListener listener : subscribeListeners) {
                listener.onSubscribe(event);
            }
        }

        private void fireUnsubscribeListeners(SubscriptionEvent event) {
            for (UnsubscribeListener listener : unsubscribeListeners) {
                listener.onUnsubscribe(event);
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
    }
}
