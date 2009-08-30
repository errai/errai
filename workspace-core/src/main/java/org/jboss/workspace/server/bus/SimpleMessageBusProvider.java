package org.jboss.workspace.server.bus;

import org.jboss.workspace.client.framework.MessageCallback;
import org.jboss.workspace.client.rpc.CommandMessage;
import org.jboss.workspace.client.rpc.protocols.SecurityParts;
import org.jboss.workspace.server.MessageBusServiceImpl;
import static org.jboss.workspace.server.bus.MessageBusServer.encodeMap;

import javax.servlet.http.HttpSession;
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

        public void storeGlobal(String subject, CommandMessage message) {
            storeGlobal(subject, message, true);
        }

        public void storeGlobal(final String subject, final CommandMessage message, boolean fireListeners) {
            if (fireListeners && !fireAllListeners(message)) return;

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

            if (messageQueues.containsKey(sessionId)) {
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
        }

        public void store(String sessionid, String subject, CommandMessage message) {
            store(sessionid, subject, message, true);
        }

        public void store(String sessionid, String subject, CommandMessage message, boolean fireListeners) {
            if (fireListeners && !fireAllListeners(message)) return;

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
                try {
                    activeWaitingThreads.put(sessionContext, Thread.currentThread());
                    Thread.sleep(1000 * 45);
                }
                catch (InterruptedException e) {
                    // passthru
                }
                finally {
                    activeWaitingThreads.remove(sessionContext);
                }
                return q.poll();

            }
            else {
                return q.poll();
            }
        }


        private Queue<Message> getQueue(Object sessionContext) {
            if (!messageQueues.containsKey(sessionContext))
                messageQueues.put(sessionContext, new LinkedList<Message>());

            return messageQueues.get(sessionContext);
        }

        public void subscribe(String subject, MessageCallback receiver) {
            if (!subscriptions.containsKey(subject)) subscriptions.put(subject, new ArrayList<MessageCallback>());
            subscriptions.get(subject).add(receiver);
        }

        public void remoteSubscribe(Object sessionContext, String subject) {
            if (!remoteSubscriptions.containsKey(subject)) remoteSubscriptions.put(subject, new ArrayList<Object>());
            remoteSubscriptions.get(subject).add(sessionContext);
        }

        public Set<String> getSubjects() {
            return subscriptions.keySet();
        }

        private boolean fireAllListeners(CommandMessage message) {
            boolean allowContinue = true;

            for (MessageListener listener : listeners) {
                if (!listener.handleMessage(message)) {
                    allowContinue = false;
                }
            }

            return allowContinue;
        }

        public void addGlobalListener(MessageListener listener) {
            listeners.add(listener);
        }
    }
}
