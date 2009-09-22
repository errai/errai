package org.jboss.errai.client.rpc;

import static com.google.gwt.core.client.GWT.create;
import static com.google.gwt.core.client.GWT.getModuleBaseURL;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import org.jboss.errai.client.framework.AcceptsCallback;
import org.jboss.errai.client.framework.MessageCallback;
import org.jboss.errai.client.rpc.protocols.MessageParts;
import org.jboss.errai.client.widgets.WSModalDialog;

import java.util.*;


public class MessageBusClient {
    private static List<AcceptsCallback> onSubscribeHooks = new ArrayList<AcceptsCallback>();
    private static List<AcceptsCallback> onUnsubscribeHooks = new ArrayList<AcceptsCallback>();

    private static final MessageBusServiceAsync messageBus = (MessageBusServiceAsync) create(MessageBusService.class);
    private static final ServiceDefTarget endpoint = (ServiceDefTarget) messageBus;
    private static final Map<String, List<Object>> subscriptions = new HashMap<String, List<Object>>();

    private static final Queue<String[]> outgoingQueue = new LinkedList<String[]>();
    private static boolean transmitting = false;

    private static Map<String, Set<Object>> registeredInThisSession = new HashMap<String, Set<Object>>();

    static {
        endpoint.setServiceEntryPoint(getModuleBaseURL() + "jbwMsgBus");
    }


    public static void unsubscribeAll(String subject) {
        if (subscriptions.containsKey(subject)) {
            for (Object o : subscriptions.get(subject)) {
                _unsubscribe(o);
            }

            for (AcceptsCallback c : onUnsubscribeHooks) {
                c.callback(subject, null);
            }
        }
    }

    public static void subscribe(String subject, MessageCallback callback, Object subscriberData) {
        for (AcceptsCallback c : onSubscribeHooks) {
            c.callback(subject, subscriberData);
        }

        addSubscription(subject, _subscribe(subject, callback, subscriberData));
    }

    public static void subscribe(String subject, MessageCallback callback) {
        for (AcceptsCallback c : onSubscribeHooks) {
            c.callback(subject, null);
        }

        addSubscription(subject, _subscribe(subject, callback, null));
    }

    public static void subscribeOnce(String subject, MessageCallback callback, Object subscriberData) {
        if (subscriptions.containsKey(subject)) return;
        subscribe(subject, callback, subscriberData);
    }

    public static void subscribeOnce(String subject, MessageCallback callback) {
        if (subscriptions.containsKey(subject)) return;
        subscribe(subject, callback);
    }

    private static int conversationCounter = 0;

    /**
     * Have a single two-way conversation
     *
     * @param subject
     * @param message
     * @param callback
     */
    public static void conversationWith(final String subject, CommandMessage message, MessageCallback callback) {
        final String tempSubject = conversationCounter++ + ":temp";

        final Timer t = new Timer() {
            @Override
            public void run() {
                WSModalDialog error = new WSModalDialog();
                error.ask("Service '" + subject + "' did not property respond", new AcceptsCallback() {
                    public void callback(Object message, Object data) {
                        unsubscribeAll(tempSubject);
                    }
                });

                error.showModal();
            }
        };

        message.set(MessageParts.ReplyTo, tempSubject);

        subscribe(tempSubject, callback);
        subscribe(tempSubject, new MessageCallback() {
            public void callback(CommandMessage message) {
                t.cancel();
                unsubscribeAll(tempSubject);
            }
        });


        store(subject, message);

        t.schedule(500);
    }

    private static void addSubscription(String subject, Object reference) {
        if (!subscriptions.containsKey(subject)) {
            subscriptions.put(subject, new ArrayList<Object>());
        }

        if (registeredInThisSession != null && !registeredInThisSession.containsKey(subject)) {
            registeredInThisSession.put(subject, new HashSet<Object>());
        }

        subscriptions.get(subject).add(reference);
        if (registeredInThisSession != null) registeredInThisSession.get(subject).add(reference);
    }

    public static Map<String, Set<Object>> getCapturedRegistrations() {
        return registeredInThisSession;
    }

    public static void beginCapture() {
        registeredInThisSession = new HashMap<String, Set<Object>>();
    }


    public static void endCapture() {
        registeredInThisSession = null;
    }

    public static void unregisterAll(Map<String, Set<Object>> all) {
        for (Map.Entry<String, Set<Object>> entry : all.entrySet()) {
            for (Object o : entry.getValue()) {
                subscriptions.get(entry.getKey()).remove(o);
                _unsubscribe(o);
            }

            if (subscriptions.get(entry.getKey()).isEmpty()) {
                for (AcceptsCallback c : onUnsubscribeHooks) {
                    c.callback(entry.getKey(), null);
                }
            }
        }

    }

    private native static void _unsubscribe(Object registrationHandle) /*-{
        $wnd.PageBus.unsubscribe(registrationHandle);
    }-*/;

    private native static Object _subscribe(String subject, MessageCallback callback,
                                            Object subscriberData) /*-{
         return $wnd.PageBus.subscribe(subject, null,
                 function (subject, message, subcriberData) {
                    callback.@org.jboss.errai.client.framework.MessageCallback::callback(Lorg/jboss/errai/client/rpc/CommandMessage;)(@org.jboss.errai.client.rpc.MessageBusClient::decodeCommandMessage(Ljava/lang/Object;)(message))
                 },
                 null);
    }-*/;

    public native static void store(String subject, Object value) /*-{
         $wnd.PageBus.store(subject, value);
    }-*/;

    public static void store(String subject, Map<String, Object> message) {
        store(subject, encodeMap(message));
    }

    public static void store(String subject, CommandMessage message) {
        try {
            store(subject, message.getParts());
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void store(CommandMessage message) {
        if (message.hasPart(MessageParts.ToSubject)) {
            store(message.get(String.class, MessageParts.ToSubject), message);
        }
        else {
            throw new RuntimeException("Cannot send message using this method if the message does not contain a ToSubject field.");
        }
    }

    public static void enqueueForRemoteTransmit(String subject, CommandMessage message) {
        outgoingQueue.add(new String[]{subject, encodeMap(message.getParts())});
        sendAll();
    }


    private static Timer sendTimer;

    private static void sendAll() {
        if (transmitting) {
            if (sendTimer == null) {
                sendTimer = new Timer() {
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
        }
        else if (sendTimer != null) {
            sendTimer.cancel();
            sendTimer = null;
        }

        String[] msg = outgoingQueue.poll();
        if (msg != null) transmitRemote(msg[0], msg[1]);
    }


    private static void transmitRemote(String subject, String message) {
        try {
            transmitting = true;
            messageBus.store(subject, message, new AsyncCallback<Void>() {
                public void onFailure(Throwable throwable) {
                    MessageBusClient.transmitting = false;
                    sendAll();
                }

                public void onSuccess(Void o) {
                    MessageBusClient.transmitting = false;
                    sendAll();
                }
            });

        }
        catch (Exception e) {
            transmitting = false;
            e.printStackTrace();
        }
    }

    public static void addOnSubscribeHook(AcceptsCallback callback) {
        onSubscribeHooks.add(callback);
    }

    public static void addOnUnsubscribeHook(AcceptsCallback callback) {
        onUnsubscribeHooks.add(callback);
    }

    public static Set<String> getAllLocalSubscriptions() {
        return subscriptions.keySet();
    }

    public static Map<String, Object> decodeMap(Object value) {
        JSONValue a = JSONParser.parse(String.valueOf(value));

        Map<String, Object> m = new HashMap<String, Object>();

        if (a instanceof JSONObject) {
            JSONObject eMap = (JSONObject) a;

            for (String key : eMap.keySet()) {
                JSONValue v = eMap.get(key);

                if (v.isString() != null) {
                    m.put(key, v.isString().stringValue());
                }
                else if (v.isNumber() != null) {
                    m.put(key, v.isNumber().doubleValue());
                }
                else if (v.isBoolean() != null) {
                    m.put(key, v.isBoolean().booleanValue());
                }
                else if (v.isNull() != null) {
                    m.put(key, null);
                }
            }
        }
        else {
            throw new RuntimeException("bad encoding");
        }

        return m;
    }


    public static CommandMessage decodeCommandMessage(Object value) {
        return new CommandMessage(decodeMap(value), String.valueOf(value));
    }

    public static String encodeMap(Map<String, Object> map) {
        if (map.size() == 0) return "{}";

        StringBuffer buf = new StringBuffer("{");
        Object v;

        int i = 0;
        for (Map.Entry<String, Object> entry : map.entrySet()) {

            buf.append("\"").append(entry.getKey()).append("\"").append(":");

            v = entry.getValue();
            if (v == null) {
                buf.append("null");
            }
            else if (v instanceof String) {
                buf.append("\"").append(v).append("\"");
            }
            else if (v instanceof Number) {
                buf.append(v);
            }
            else if (v instanceof Boolean) {
                buf.append(v);
            }
            else {
                throw new RuntimeException("cannot encode element type: " + v);
            }

            if (++i < map.size()) buf.append(", ");
        }


        return buf.append("}").toString();
    }

}

