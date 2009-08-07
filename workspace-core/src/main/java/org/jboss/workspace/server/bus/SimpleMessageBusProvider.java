package org.jboss.workspace.server.bus;

import org.jboss.workspace.client.framework.AcceptsCallback;

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

        public void store(String subject, String message) {
            if (subscriptions.containsKey(subject)) {
                for (AcceptsCallback c : subscriptions.get(subject)) {
                    c.callback(message, null);
                }
            }
            if (remoteSubscriptions.containsKey(subject)) {
                for (Thread t : activeWaitingThreads.values()) {
                    t.interrupt();
                }
            }

        }

        public Message nextMessage(Object sessionContext) {
            Queue<Message> q = getQueue(sessionContext);

            assert !activeWaitingThreads.containsKey(Thread.currentThread());

            if (q.isEmpty()) {
                try {
                    activeWaitingThreads.put(sessionContext, Thread.currentThread());
                    Thread.sleep((1000 * 60) * 45);
                }
                catch (InterruptedException e) {
                    return q.poll();
                }
                finally {
                    activeWaitingThreads.remove(sessionContext);
                }
                return null;

            }
            else {
                return q.poll();
            }
        }


        private Queue<Message> getQueue(Object sessionContext) {
            if (!messageQueues.containsKey(sessionContext)) messageQueues.put(sessionContext, new LinkedList<Message>());
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
    }
}
