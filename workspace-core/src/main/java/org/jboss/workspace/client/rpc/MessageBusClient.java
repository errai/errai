package org.jboss.workspace.client.rpc;

import static com.google.gwt.core.client.GWT.create;
import static com.google.gwt.core.client.GWT.getModuleBaseURL;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.Timer;
import org.jboss.workspace.client.framework.AcceptsCallback;
import org.jboss.workspace.client.framework.MessageCallback;

import java.util.*;


public class MessageBusClient {
    private static List<AcceptsCallback> onSubscribeHooks = new ArrayList<AcceptsCallback>();
    private static final MessageBusServiceAsync messageBus = (MessageBusServiceAsync) create(MessageBusService.class);
    private static final ServiceDefTarget endpoint = (ServiceDefTarget) messageBus;

    private static final Queue<String[]> outgoingQueue = new LinkedList<String[]>();
    private static boolean transmitting = false;

    static {
        endpoint.setServiceEntryPoint(getModuleBaseURL() + "jbwMsgBus");
    }

    public static void subscribe(String subject, MessageCallback callback, Object subscriberData) {

        for (AcceptsCallback c : onSubscribeHooks) {
            c.callback(subject, subscriberData);
        }

        _subscribe(subject, callback, subscriberData);
    }

    public static void subscribe(String subject, MessageCallback callback) {

        for (AcceptsCallback c : onSubscribeHooks) {
            c.callback(subject, null);
        }

        _subscribe(subject, callback, null);
    }

    private native static void _subscribe(String subject, MessageCallback callback,
                                          Object subscriberData) /*-{

         $wnd.PageBus.subscribe(subject, null,
                 function (subject, message, subcriberData) {
                    callback.@org.jboss.workspace.client.framework.MessageCallback::callback(Lorg/jboss/workspace/client/rpc/CommandMessage;)(@org.jboss.workspace.client.rpc.MessageBusClient::decodeCommandMessage(Ljava/lang/Object;)(message))
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

    public static void enqueueForRemoteTransmit(String subject, CommandMessage message) {
        outgoingQueue.add(new String[]{subject, encodeMap(message.getParts())});
        sendAll();
    }

    private static void sendAll() {
        if (transmitting) {
            Timer retry = new Timer() {
                @Override
                public void run() {
                    if (!transmitting) {
                        cancel();
                        sendAll();
                    }
                }
            };
            retry.scheduleRepeating(50);
        }

        transmitting = true;
        while (!outgoingQueue.isEmpty()) {
            String[] msg = outgoingQueue.poll();
            transmitRemote(msg[0], msg[1]);
        }
        transmitting = false;
    }

    private static void transmitRemote(String subject, String message) {
        try {
            AsyncCallback<Void> cb = new AsyncCallback<Void>() {
                public void onFailure(Throwable throwable) {
                }

                public void onSuccess(Void o) {
                }
            };

            messageBus.store(subject, message, cb);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void addOnSubscribeHook(AcceptsCallback callback) {
        onSubscribeHooks.add(callback);
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

