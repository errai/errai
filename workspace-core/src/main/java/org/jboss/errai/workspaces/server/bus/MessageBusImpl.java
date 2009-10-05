package org.jboss.errai.workspaces.server.bus;

import com.google.gwt.core.client.GWT;
import com.google.inject.Singleton;
import org.jboss.errai.workspaces.client.bus.CommandMessage;
import org.jboss.errai.workspaces.client.bus.ConversationMessage;
import org.jboss.errai.workspaces.client.bus.Message;
import org.jboss.errai.workspaces.client.framework.MessageCallback;
import org.jboss.errai.workspaces.bus.client.protocols.BusCommands;
import org.jboss.errai.workspaces.bus.client.protocols.MessageParts;
import org.jboss.errai.workspaces.bus.client.protocols.SecurityCommands;
import org.jboss.errai.workspaces.bus.client.protocols.SecurityParts;
import static org.jboss.errai.workspaces.server.bus.MessageBusServer.encodeMap;

import javax.servlet.http.HttpSession;
import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.Queue;

@Singleton
public class MessageBusImpl implements MessageBus {
    private final static int QUEUE_SIZE = 200;

    private final List<MessageListener> listeners = new ArrayList<MessageListener>();

    private final Map<String, List<MessageCallback>> subscriptions = new HashMap<String, List<MessageCallback>>();
    private final Map<String, Set<Object>> remoteSubscriptions = new HashMap<String, Set<Object>>();

    private final Map<Object, MessageQueue> messageQueues = new HashMap<Object, MessageQueue>();

    private final List<SubscribeListener> subscribeListeners = new LinkedList<SubscribeListener>();
    private final List<UnsubscribeListener> unsubscribeListeners = new LinkedList<UnsubscribeListener>();

    private final HouseKeeper houseKeeper = new HouseKeeper(this);

    public MessageBusImpl() {
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
                            Queue<Message> q = mq.getQueue();

                            builder.append("   Queue: ").append(queue).append(" (size:").append(q.size()).append("; active:")
                                    .append(mq.isActive()).append("; stale:").append(mq.isStale()).append(")")
                                    .append(q.size() == QUEUE_SIZE ? " ** QUEUE FULL (BLOCKING) **" : "").append("\n");
                            for (Message message : q) {
                                builder.append("     -> @").append(message.getSubject()).append(" = ").append(message.getMessage()).append("\n");
                            }

                        }

                        area.setText(builder.append("\n").toString());
                    }
                }
                catch (InterruptedException e) {
                    // do nothing.
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
        if (!GWT.isClient()) {
            thread.setPriority(Thread.MIN_PRIORITY);
            thread.start();
        }

        subscribe("ServerBus", new MessageCallback() {
            public void callback(CommandMessage message) {
                switch (BusCommands.valueOf(message.getCommandType())) {
                    case RemoteSubscribe:
                        remoteSubscribe(message.get(HttpSession.class, SecurityParts.SessionData).getAttribute(WS_SESSION_ID),
                                message.get(String.class, MessageParts.Subject));
                        break;

                    case RemoteUnsubscribe:
                        remoteUnsubscribe(message.get(HttpSession.class, SecurityParts.SessionData).getAttribute(WS_SESSION_ID),
                                message.get(String.class, MessageParts.Subject));
                        break;

                    case ConnectToQueue:
                        Object sessionContext = message.get(HttpSession.class, SecurityParts.SessionData).getAttribute(WS_SESSION_ID);

                        if (!messageQueues.containsKey(message.get(HttpSession.class, SecurityParts.SessionData)))
                            messageQueues.put(sessionContext,
                                    new MessageQueue(QUEUE_SIZE));

                        remoteSubscribe(sessionContext, "ClientBus");

                        for (String service : subscriptions.keySet()) {
                            if (service.startsWith("local:")) {
                                continue;
                            }

                            send(ConversationMessage.create(BusCommands.RemoteSubscribe, message)
                                    .set(MessageParts.Subject, service).setSubject("ClientBus"), false);
                        }

                        send(ConversationMessage.create(BusCommands.FinishStateSync, message).setSubject("ClientBus"), false);

                        break;
                }
            }
        });

        houseKeeper.start();
    }

    public void sendGlobal(CommandMessage message) {
        sendGlobal(message.getSubject(), message);
    }

    public void sendGlobal(String subject, CommandMessage message) {
        sendGlobal(subject, message, true);
    }

    public void sendGlobal(final String subject, final CommandMessage message, boolean fireListeners) {
        if (!subscriptions.containsKey(subject) && !remoteSubscriptions.containsKey(subject)) {
            throw new NoSubscribersToDeliverTo("for: " + subject);
        }

        if (fireListeners && !fireGlobalMessageListeners(message)) {
            if (message.hasPart(MessageParts.ReplyTo) && message.hasPart(SecurityParts.SessionData)) {
                /**
                 * Inform the sender that we did not deliver the message.
                 */

                store((String) message.get(HttpSession.class, SecurityParts.SessionData).getAttribute(WS_SESSION_ID),
                        message.get(String.class, MessageParts.ReplyTo),
                        encodeMap(CommandMessage.create(SecurityCommands.MessageNotDelivered).getParts()));
            }

            return;
        }

        final String jsonMessage = encodeMap(message.getParts());

        if (subscriptions.containsKey(subject)) {
            for (MessageCallback c : subscriptions.get(subject)) {
                c.callback(message);
            }
        }

        if (remoteSubscriptions.containsKey(subject)) {
            for (Map.Entry<Object, MessageQueue> entry : messageQueues.entrySet()) {
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

    public void send(CommandMessage message) {
        sendGlobal(message);
    }

    public void send(String sessionid, String subject, CommandMessage message) {
        send(sessionid, subject, message, true);
    }

    public void send(String sessionid, String subject, CommandMessage message, boolean fireListeners) {
        if (fireListeners && !fireGlobalMessageListeners(message)) {
            if (message.hasPart(MessageParts.ReplyTo)) {
                store(sessionid, message.get(String.class, MessageParts.ReplyTo),
                        encodeMap(CommandMessage.create(SecurityCommands.MessageNotDelivered).getParts()));
            }

            return;
        }

        store(sessionid, subject, encodeMap(message.getParts()));
    }

    public void send(String subject, CommandMessage message) {
        send(subject, message, true);
    }

    public void send(CommandMessage message, boolean fireListeners) {
        send(message.getSubject(), message, fireListeners);
    }

    public void send(String subject, CommandMessage message, boolean fireListeners) {
        if (!message.hasPart(SecurityParts.SessionData)) {
            throw new RuntimeException("cannot automatically route message. no session contained in message.");
        }

        HttpSession session = message.get(HttpSession.class, SecurityParts.SessionData);

        if (session == null) {
            throw new RuntimeException("cannot automatically route message. no session contained in message.");
        }

        send((String) session.getAttribute(WS_SESSION_ID), subject, message, fireListeners);
    }


    public Payload nextMessage(Object sessionContext) {
        return messageQueues.get(sessionContext).poll();
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
        Iterator<Message> iter = messageQueues.get(sessionContext).getQueue().iterator();
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

    private static class HouseKeeper extends Thread {
        private boolean running = true;
        private MessageBusImpl bus;

        public HouseKeeper(MessageBusImpl bus) {
            super();
            this.bus = bus;
            setPriority(Thread.MIN_PRIORITY);
        }

        @Override
        public void run() {
            try {
                while (running) {
                    Thread.sleep(1000 * 10);
                    houseKeep();
                }
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        private void houseKeep() {
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

    }


}