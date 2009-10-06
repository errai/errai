package org.jboss.errai.bus.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.http.client.*;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import org.jboss.errai.bus.client.json.JSONUtilCli;
import static org.jboss.errai.bus.client.json.JSONUtilCli.decodePayload;
import org.jboss.errai.bus.client.protocols.BusCommands;
import org.jboss.errai.bus.client.protocols.MessageParts;

import java.util.*;

public class ClientBusImpl implements MessageBus {
    private static final String SERVICE_ENTRY_POINT = "erraiBus";

    private List<SubscribeListener> onSubscribeHooks = new ArrayList<SubscribeListener>();
    private List<UnsubscribeListener> onUnsubscribeHooks = new ArrayList<UnsubscribeListener>();


    private final RequestBuilder sendBuilder;
    private final RequestBuilder recvBuilder;

    private final Map<String, List<Object>> subscriptions = new HashMap<String, List<Object>>();

    private final Queue<String> outgoingQueue = new LinkedList<String>();
    private boolean transmitting = false;

    private Map<String, Set<Object>> registeredInThisSession = new HashMap<String, Set<Object>>();


    public ClientBusImpl() {
        sendBuilder = new RequestBuilder(
                RequestBuilder.POST,
                URL.encode(SERVICE_ENTRY_POINT)
        );

        recvBuilder = new RequestBuilder(
                RequestBuilder.GET,
                URL.encode(SERVICE_ENTRY_POINT)
        );

        init();
    }


    public Set<String> getAllLocalSubscriptions() {
        return subscriptions.keySet();
    }

    public void unsubscribeAll(String subject) {
        if (subscriptions.containsKey(subject)) {
            for (Object o : subscriptions.get(subject)) {
                _unsubscribe(o);
            }

           fireAllUnSubcribeListener(subject);
        }
    }

    public void subscribe(String subject, MessageCallback callback, Object subscriberData) {
        fireAllSubcribeListener(subject);
        addSubscription(subject, _subscribe(subject, callback, subscriberData));
    }

    public void subscribe(String subject, MessageCallback callback) {
        fireAllSubcribeListener(subject);
        addSubscription(subject, _subscribe(subject, callback, null));
    }

    private void fireAllSubcribeListener(String subject) {
        Iterator<SubscribeListener> iter = onSubscribeHooks.iterator();
        SubscriptionEvent evt = new SubscriptionEvent(false, "InBrowser", subject);

        while (iter.hasNext()) {
            iter.next().onSubscribe(evt);
            if (evt.isDisposeListener()) {
                iter.remove();
                evt.setDisposeListener(false);
            }
        }
    }

    private void fireAllUnSubcribeListener(String subject) {
        Iterator<UnsubscribeListener> iter = onUnsubscribeHooks.iterator();
        SubscriptionEvent evt = new SubscriptionEvent(false, "InBrowser", subject);

        while (iter.hasNext()) {
            iter.next().onUnsubscribe(evt);
            if (evt.isDisposeListener()) {
                iter.remove();
                evt.setDisposeListener(false);
            }
        }
    }

    public void subscribeOnce(String subject, MessageCallback callback, Object subscriberData) {
        if (subscriptions.containsKey(subject)) return;
        subscribe(subject, callback, subscriberData);
    }

    public void subscribeOnce(String subject, MessageCallback callback) {
        if (subscriptions.containsKey(subject)) return;
        subscribe(subject, callback);
    }


    private static int conversationCounter = 0;

    /**
     * Have a single two-way conversation
     *
     * @param message
     * @param callback
     */
    public void conversationWith(final CommandMessage message, final MessageCallback callback) {
        final String tempSubject = "temp:PsuedoConversation:" + (++conversationCounter);

        message.set(MessageParts.ReplyTo, tempSubject);

        subscribe(tempSubject, new MessageCallback() {
            public void callback(CommandMessage message) {
                unsubscribeAll(tempSubject);
                callback.callback(message);
            }
        });

        subscribe(tempSubject, callback);

        send(message);
    }


    public void sendGlobal(CommandMessage message) {
        send(message);
    }

    public void send(String subject, Map<String, Object> message) {
        message.put("ToSubject", subject);
        store(subject, JSONUtilCli.encodeMap(message));
    }

    public void send(String subject, CommandMessage message) {
        try {
            send(subject, message.getParts());
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void send(String subject, Enum commandType) {
        send(subject, CommandMessage.create(commandType));
    }


    public void send(CommandMessage message) {
        if (message.hasPart(MessageParts.ToSubject)) {
            send(message.get(String.class, MessageParts.ToSubject), message);
        } else {
            throw new RuntimeException("Cannot send message using this method if the message does not contain a ToSubject field.");
        }
    }

    public void enqueueForRemoteTransmit(CommandMessage message) {
        outgoingQueue.add(JSONUtilCli.encodeMap(message.getParts()));
        sendAll();
    }


    private void addSubscription(String subject, Object reference) {
        if (!subscriptions.containsKey(subject)) {
            subscriptions.put(subject, new ArrayList<Object>());
        }

        if (registeredInThisSession != null && !registeredInThisSession.containsKey(subject)) {
            registeredInThisSession.put(subject, new HashSet<Object>());
        }

        subscriptions.get(subject).add(reference);
        if (registeredInThisSession != null) registeredInThisSession.get(subject).add(reference);
    }

    public boolean isSubscribed(String subject) {
        return subscriptions.containsKey(subject);
    }

    public Map<String, Set<Object>> getCapturedRegistrations() {
        return registeredInThisSession;
    }

    public void beginCapture() {
        registeredInThisSession = new HashMap<String, Set<Object>>();
    }

    public void endCapture() {
        registeredInThisSession = null;
    }

    public void unregisterAll(Map<String, Set<Object>> all) {
        for (Map.Entry<String, Set<Object>> entry : all.entrySet()) {
            for (Object o : entry.getValue()) {
                subscriptions.get(entry.getKey()).remove(o);
                _unsubscribe(o);
            }

            if (subscriptions.get(entry.getKey()).isEmpty()) {
                fireAllUnSubcribeListener(entry.getKey());
            }
        }
    }


    private com.google.gwt.user.client.Timer sendTimer;

    private void sendAll() {
        if (transmitting) {
            if (sendTimer == null) {
                sendTimer = new com.google.gwt.user.client.Timer() {
                    int timeout = 0;

                    @Override
                    public void run() {
                        if (outgoingQueue.isEmpty()) {
                            cancel();
                        }
                        sendAll();

                        /**
                         * If this fails 4 times (which is the equivalent of 200ms) then we stop blocking
                         * progress and allow more messages to flow.
                         */
                        if (++timeout > 4) {
                            transmitting = false;
                        }
                    }
                };
                sendTimer.scheduleRepeating(50);
            }

            return;
        } else if (sendTimer != null) {
            sendTimer.cancel();
            sendTimer = null;
        }

        transmitRemote(outgoingQueue.poll());
    }

    private void transmitRemote(String message) {
        if (message == null) return;

        try {
            transmitting = true;

            sendBuilder.sendRequest(message, new RequestCallback() {
                public void onResponseReceived(Request request, Response response) {
                    transmitting = false;
                    sendAll();
                }

                public void onError(Request request, Throwable exception) {
                    exception.printStackTrace();

                    transmitting = false;
                    sendAll();
                }
            });


        }
        catch (Exception e) {
            transmitting = false;
            e.printStackTrace();
        }
    }


    public void init() {
        init(null);
    }

    public void init(final HookCallback callback) {

        subscribe("ClientBus", new MessageCallback() {
            public void callback(CommandMessage message) {
                switch (BusCommands.valueOf(message.getCommandType())) {
                    case RemoteSubscribe:
                        subscribe(message.get(String.class, MessageParts.Subject), new MessageCallback() {
                            public void callback(CommandMessage message) {
                                enqueueForRemoteTransmit(message);
                            }
                        });
                        break;

                    case RemoteUnsubscribe:
                        unsubscribeAll(message.get(String.class, MessageParts.Subject));
                        break;

                    case FinishStateSync:
                        for (String s : subscriptions.keySet()) {
                            if (s.startsWith("local:")) continue;

                            send("ServerBus",
                                    CommandMessage.create(BusCommands.RemoteSubscribe)
                                            .set(MessageParts.Subject, s));
                        }


                        conversationWith(CommandMessage.create().toSubject("ServerEchoService"),
                                new MessageCallback() {
                                    public void callback(CommandMessage message) {
                                        GWT.log("Finishing initializing of Client Bus...", null);
                                        if (callback != null) callback.callback(null);
                                    }
                                });
                        break;

                }
            }
        });

        String initialMessage = "{\"CommandType\":\"ConnectToQueue\", \"ToSubject\":\"ServerBus\"}";

        /**
         * Send initial message to connect to the queue, to establish an HTTP session. Otherwise, concurrent
         * requests will result in multiple sessions being creatd.  Which is bad.  Avoid this at all costs.
         * Please.
         */
        try {
            sendBuilder.sendRequest(initialMessage, new RequestCallback() {
                public void onResponseReceived(Request request, Response response) {
                    initializeMessagingBus(callback);
                }

                public void onError(Request request, Throwable exception) {
                    exception.printStackTrace();
                }
            });
        }
        catch (RequestException e) {
            //todo: handle this.

        }
    }


    private void initializeMessagingBus(final HookCallback initCallback) {
        final SimplePanel heartBeat = new SimplePanel();
        final HTML hBtext = new HTML("*Heartbeat*");
        hBtext.getElement().getStyle().setProperty("color", "red");

        heartBeat.add(hBtext);

        Style s = heartBeat.getElement().getStyle();
        s.setProperty("position", "absolute");
        s.setProperty("left", "300");
        s.setProperty("top", "10");

        heartBeat.setVisible(false);

        RootPanel.get().add(heartBeat);

        final com.google.gwt.user.client.Timer incoming = new com.google.gwt.user.client.Timer() {
            boolean block = false;

            @Override
            public void run() {
                if (block) {
                    return;
                }

                block = true;
                try {
                    recvBuilder.sendRequest(null,
                            new RequestCallback() {
                                public void onError(Request request, Throwable throwable) {
                                    block = false;


                                    schedule(1);
                                }

                                public void onResponseReceived(Request request, Response response) {

                                    for (Message m : decodePayload(response.getText())) {
                                        store(m.getSubject(), m.getMessage());
                                    }

                                    block = false;
                                    schedule(1);
                                }
                            }

                    );
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
                block = false;
            }
        };

        final com.google.gwt.user.client.Timer outerTimer = new com.google.gwt.user.client.Timer() {
            @Override
            public void run() {
                incoming.run();
                incoming.scheduleRepeating((60 * 45) * 1000);
            }
        };


        outerTimer.schedule(10);
    }


    public Payload nextMessage(Object sessionContext) {
        return null;
    }


    public void addGlobalListener(MessageListener listener) {
    }

    public void send(CommandMessage message, boolean fireListeners) {
        send(message);
    }

    public void send(String subject, CommandMessage message, boolean fireListener) {
        send(subject, message);
    }

    public void addSubscribeListener(SubscribeListener listener) {
        this.onSubscribeHooks.add(listener);
    }

    public void addUnsubscribeListener(UnsubscribeListener listener) {
        this.onUnsubscribeHooks.add(listener);
    }

    private native static void _unsubscribe(Object registrationHandle) /*-{
         $wnd.PageBus.unsubscribe(registrationHandle);
     }-*/;

    private native static Object _subscribe(String subject, MessageCallback callback,
                                            Object subscriberData) /*-{
          return $wnd.PageBus.subscribe(subject, null,
                  function (subject, message, subcriberData) {
                     callback.@org.jboss.errai.bus.client.MessageCallback::callback(Lorg/jboss/errai/bus/client/CommandMessage;)(@org.jboss.errai.bus.client.json.JSONUtilCli::decodeCommandMessage(Ljava/lang/Object;)(message))
                  },
                  null);
     }-*/;

    public native static void store(String subject, Object value) /*-{
          $wnd.PageBus.store(subject, value);
     }-*/;


}
