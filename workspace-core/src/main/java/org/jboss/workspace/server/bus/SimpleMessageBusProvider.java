package org.jboss.workspace.server.bus;

import org.jboss.workspace.client.framework.AcceptsCallback;
import static org.jboss.workspace.server.bus.MessageBusServer.encodeMap;

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
        private final Map<String, List<AcceptsCallback>> subscriptions = new HashMap<String, List<AcceptsCallback>>();
        private final Map<String, List<Object>> remoteSubscriptions = new HashMap<String, List<Object>>();

        private final Map<Object, Queue<Message>> messageQueues = new HashMap<Object, Queue<Message>>();
        private final Map<Object, Thread> activeWaitingThreads = new HashMap<Object, Thread>();

        public void store(final String subject, final Map<String, Object> message) {
            store(subject, encodeMap(message));
        }

        public void store(final String subject, final Object message) {
            if (subscriptions.containsKey(subject)) {
                for (AcceptsCallback c : subscriptions.get(subject)) {
                    c.callback(message, null);
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
                                return message;
                            }
                        });

                        Thread t = activeWaitingThreads.get(entry.getKey());
                        if (t != null) t.interrupt();
                    }
                }
            }

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

        public void subscribe(String subject, AcceptsCallback receiver) {
            if (!subscriptions.containsKey(subject)) subscriptions.put(subject, new ArrayList<AcceptsCallback>());
            subscriptions.get(subject).add(receiver);
        }

        public void remoteSubscribe(Object sessionContext, String subject) {
            if (!remoteSubscriptions.containsKey(subject)) remoteSubscriptions.put(subject, new ArrayList<Object>());
            remoteSubscriptions.get(subject).add(sessionContext);
        }

        public Set<String> getSubjects() {
            return subscriptions.keySet();
        }

        public void addGlobalListener(MessageListener listener) {

        }
    }
}
