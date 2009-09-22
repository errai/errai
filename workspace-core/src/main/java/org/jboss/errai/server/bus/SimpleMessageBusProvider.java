package org.jboss.errai.server.bus;

import org.jboss.errai.client.framework.MessageCallback;
import org.jboss.errai.client.rpc.CommandMessage;
import org.jboss.errai.client.rpc.protocols.SecurityParts;
import org.jboss.errai.server.MessageBusServiceImpl;
import static org.jboss.errai.server.bus.MessageBusServer.encodeMap;

import javax.servlet.http.HttpSession;
import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;


public class SimpleMessageBusProvider implements MessageBusProvider {
    private static MessageBus bus;

    public MessageBus getBus() {
        if (bus == null) {
            bus = new SimpleMessageBus();
        }
        return bus;
    }

    private static final Message heartBeat = new Message() {
        public String getSubject() {
            return "HeartBeat";
        }

        public Object getMessage() {
            return null;
        }
    };


    public class SimpleMessageBus implements MessageBus {
        private final List<MessageListener> listeners = new ArrayList<MessageListener>();

        private final Map<String, List<MessageCallback>> subscriptions = new HashMap<String, List<MessageCallback>>();
        private final Map<String, Set<Object>> remoteSubscriptions = new HashMap<String, Set<Object>>();

        private final Map<Object, BlockingQueue<Message>> messageQueues = new HashMap<Object, BlockingQueue<Message>>();
        //   private final Map<Object, Thread> activeWaitingThreads = new HashMap<Object, Thread>();

        private final List<SubscribeListener> subscribeListeners = new LinkedList<SubscribeListener>();
        private final List<UnsubscribeListener> unsubscribeListeners = new LinkedList<UnsubscribeListener>();

        public SimpleMessageBus() {
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
                            Thread.sleep(100);

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
                                builder.append("   __________________________").append("\n");
                                Queue<Message> q = messageQueues.get(queue);

                                builder.append("   Queue: ").append(queue).append(" (size:").append(q.size()).append(")").append(q.size() == 25 ? " ** QUEUE FULL (BLOCKING) **" : "").append("\n");
                                for (Message message : q) {
                                    builder.append("     -> @").append(message.getSubject()).append(" = ").append(message.getMessage()).append("\n");
                                }

                            }

                            area.setText(builder.append("\n").toString());
                        }
                    }
                    catch (InterruptedException e) {
                    }
                    catch (ConcurrentModificationException e) {
                        run();
                    }
                    catch (Exception e) {
                        e.printStackTrace();
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
                for (Map.Entry<Object, BlockingQueue<Message>> entry : messageQueues.entrySet()) {
                    if (remoteSubscriptions.get(subject).contains(entry.getKey())) {
                        messageQueues.get(entry.getKey()).offer(new Message() {
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

        private void store(final String sessionId, final String subject, final Object message) {
            if (messageQueues.containsKey(sessionId) && isAnyoneListening(sessionId, subject)) {
                messageQueues.get(sessionId).offer(new Message() {
                    public String getSubject() {
                        return subject;
                    }

                    public Object getMessage() {
                        return message;
                    }
                });
            }
            else {
                throw new NoSubscribersToDeliverTo("for: " + subject);
            }
        }

        public void store(String sessionid, String subject, CommandMessage message) {
            store(sessionid, subject, message, true);
        }

        public void store(String sessionid, String subject, CommandMessage message, boolean fireListeners) {
            if (fireListeners && !fireGlobalMessageListeners(message)) {
                System.out.println("ListenerBlockedDelivery (@" + subject + ")");
                return;
            }

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
            try {
                /**
                 * Long-poll for 45 seconds.
                 */
                Message m = getQueue(sessionContext).poll(45, TimeUnit.SECONDS);
                return m == null ? heartBeat : m;
            }
            catch (InterruptedException e) {
                return heartBeat;
            }
        }

        private BlockingQueue<Message> getQueue(Object sessionContext) {
            if (!messageQueues.containsKey(sessionContext))
                messageQueues.put(sessionContext, new ArrayBlockingQueue<Message>(25));

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
            if (subscriptions.containsKey(subject) || subject == null) return;

            fireSubscribeListeners(new SubscriptionEvent(true, sessionContext, subject));

            if (!remoteSubscriptions.containsKey(subject)) {
                remoteSubscriptions.put(subject, new HashSet<Object>());
            }
            remoteSubscriptions.get(subject).add(sessionContext);
        }

        public void remoteUnsubscribe(Object sessionContext, String subject) {

            if (!remoteSubscriptions.containsKey(subject)) {
                System.out.println("CannotUnsubscribe (NO KNOWN SUBJECT: " + subject + ")");
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
            Iterator<Message> iter = messageQueues.get(sessionContext).iterator();
            while (iter.hasNext()) {
                if (subject.equals(iter.next().getSubject())) {
                    iter.remove();
                }
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
    }
}
