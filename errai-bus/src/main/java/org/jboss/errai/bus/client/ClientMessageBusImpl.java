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

package org.jboss.errai.bus.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.*;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import org.jboss.errai.bus.client.ext.ExtensionsLoader;
import org.jboss.errai.bus.client.json.JSONUtilCli;
import org.jboss.errai.bus.client.protocols.BusCommands;
import org.jboss.errai.bus.client.protocols.MessageParts;

import java.util.*;

import static org.jboss.errai.bus.client.CommandMessage.create;
import static org.jboss.errai.bus.client.json.JSONUtilCli.decodePayload;
import static org.jboss.errai.bus.client.json.JSONUtilCli.encodeMap;
import static org.jboss.errai.bus.client.protocols.BusCommands.RemoteSubscribe;
import static org.jboss.errai.bus.client.protocols.MessageParts.*;

/**
 * The default client <tt>MessageBus</tt> implementation.  This bus runs in the browser and automatically federates
 * with the server immediately upon initialization.
 */
public class ClientMessageBusImpl implements ClientMessageBus {
    private static final String SERVICE_ENTRY_POINT = "in.erraiBus";

    private List<SubscribeListener> onSubscribeHooks = new ArrayList<SubscribeListener>();
    private List<UnsubscribeListener> onUnsubscribeHooks = new ArrayList<UnsubscribeListener>();

    private RequestBuilder sendBuilder;
    private final RequestBuilder recvBuilder;

    private final Map<String, List<Object>> subscriptions = new HashMap<String, List<Object>>();

    private final Queue<String> outgoingQueue = new LinkedList<String>();
    private boolean transmitting = false;

    private Map<String, Set<Object>> registeredInThisSession = new HashMap<String, Set<Object>>();

    private ArrayList<Runnable> postInitTasks = new ArrayList<Runnable>();

    private boolean initialized = false;

    public ClientMessageBusImpl() {
        (sendBuilder = new RequestBuilder(
                RequestBuilder.POST,
                URL.encode(SERVICE_ENTRY_POINT)
        )).setHeader("Connection", "Keep-Alive");

        (recvBuilder = new RequestBuilder(
                RequestBuilder.GET,
                URL.encode(SERVICE_ENTRY_POINT)
        )).setHeader("Connection", "Keep-Alive");

        init();
    }

    public void unsubscribeAll(String subject) {
        if (subscriptions.containsKey(subject)) {
            for (Object o : subscriptions.get(subject)) {
                _unsubscribe(o);
            }

            fireAllUnSubcribeListener(subject);
        }
    }

    public void subscribe(final String subject, final MessageCallback callback) {
        fireAllSubcribeListener(subject);

        MessageCallback dispatcher = new MessageCallback() {
            public void callback(CommandMessage message) {
                try {
                    callback.callback(message);
                }
                catch (Exception e) {
                    showError("Receiver '" + subject + "' threw an exception", decodeCommandMessage(message), e);
                }
            }
        };

        addSubscription(subject, _subscribe(subject, dispatcher, null));
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

    private static int conversationCounter = 0;

    /**
     * Have a single two-way conversation
     *
     * @param message  -
     * @param callback -
     */
    public void conversationWith(final CommandMessage message, final MessageCallback callback) {
        final String tempSubject = "temp:Conversation:" + (++conversationCounter);

        message.set(ReplyTo, tempSubject);

        subscribe(tempSubject, new MessageCallback() {
            public void callback(CommandMessage message) {
                unsubscribeAll(tempSubject);
                callback.callback(message);
            }
        });

        send(message);
    }

    public void sendGlobal(CommandMessage message) {
        send(message);
    }

    public void send(String subject, CommandMessage message) {
        try {
            send(subject, message.getParts());
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void send(final String subject, final Map<String, Object> message) {
        message.put("ToSubject", subject);
        if (!initialized) {
            postInitTasks.add(new Runnable() {
                public void run() {
                    store(subject, encodeMap(message));
                }
            });
        } else {
            store(subject, encodeMap(message));
        }
    }

    public void send(String subject, Enum commandType) {
        send(subject, create(commandType));
    }

    public void send(CommandMessage message) {
        if (message.hasPart(MessageParts.ToSubject)) {
            send(message.get(String.class, MessageParts.ToSubject), message);
        } else {
            throw new RuntimeException("Cannot send message using this method" +
                    " if the message does not contain a ToSubject field.");
        }
    }

    public void enqueueForRemoteTransmit(CommandMessage message) {
        outgoingQueue.add(encodeMap(message.getParts()));
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
        if (!initialized) {
            return;
        } else if (transmitting) {
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
                         * If this fails 20 times then we stop blocking
                         * progress and allow more messages to flow.
                         */
                        if (++timeout > 20) {
                            transmitting = false;
                        }
                    }
                };
                sendTimer.scheduleRepeating(75);
            }

            return;
        } else if (sendTimer != null) {
            sendTimer.cancel();
            sendTimer = null;
        }

        int transmissionSize = outgoingQueue.size();

        StringBuffer outgoing = new StringBuffer();
        for (int i = 0; i < transmissionSize; i++) {
            outgoing.append(outgoingQueue.poll());

            if ((i + 1) < transmissionSize) {
                outgoing.append(JSONUtilCli.MULTI_PAYLOAD_SEPER);
            }
        }

        if (transmissionSize != 0) transmitRemote(outgoing.toString());
    }

    private void transmitRemote(String message) {
        if (message == null) return;

        try {
            transmitting = true;

            sendBuilder.sendRequest(message, new RequestCallback() {
                public void onResponseReceived(Request request, Response response) {
                    transmitting = false;

                    /**
                     * If the server bus returned us some client-destined messages
                     * in response to our send, handle them now.
                     */
                    try {
                        procIncomingPayload(response);
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                        showError("Problem decoding incoming message:", response.getText(), e);
                    }

                    sendAll();
                }

                public void onError(Request request, Throwable exception) {
                    showError("Failed to communicate with remote bus", "", exception);
                    transmitting = false;
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
        final MessageBus self = this;

        subscribe("ClientBus", new MessageCallback() {
            public void callback(CommandMessage message) {
                switch (BusCommands.valueOf(message.getCommandType())) {
                    case RemoteSubscribe:
                        subscribe(message.get(String.class, Subject), new MessageCallback() {
                            public void callback(CommandMessage message) {
                                enqueueForRemoteTransmit(message);
                            }
                        });
                        break;

                    case RemoteUnsubscribe:
                        unsubscribeAll(message.get(String.class, Subject));
                        break;

                    case FinishStateSync:
                        for (String s : subscriptions.keySet()) {
                            if (s.startsWith("local:")) continue;

                            create(RemoteSubscribe)
                                    .toSubject("ServerBus")
                                    .set(Subject, s)
                                    .sendNowWith(self);
                        }

                        // Don't use an iterator here -- potential for concurrent
                        // modifications!
                        //noinspection ForLoopReplaceableByForEach
                        for (int i = 0; i < postInitTasks.size(); i++) {
                            postInitTasks.get(i).run();
                        }

                        initialized = true;

                        break;
                }
            }
        });

        subscribe("ClientBusErrors", new MessageCallback() {
            public void callback(CommandMessage message) {
                showError(message.get(String.class, "ErrorMessage"),
                        message.get(String.class, "AdditionalDetails"), null);
            }
        });

        addSubscribeListener(new SubscribeListener() {
            public void onSubscribe(SubscriptionEvent event) {
                if (event.getSubject().startsWith("local:")) {
                    return;
                }
                create(RemoteSubscribe)
                        .toSubject("ServerBus")
                        .set(Subject, event.getSubject())
                        .set(PriorityProcessing, "1")
                        .sendNowWith(self);
            }
        });

        /**
         * ... also send RemoteUnsubscribe signals.
         */

        addUnsubscribeListener(new UnsubscribeListener() {
            public void onUnsubscribe(SubscriptionEvent event) {
                create(BusCommands.RemoteUnsubscribe)
                        .toSubject("ServerBus")
                        .set(Subject, event.getSubject())
                        .set(PriorityProcessing, "1")
                        .sendNowWith(self);
            }
        });


        /**
         * Send initial message to connect to the queue, to establish an HTTP session. Otherwise, concurrent
         * requests will result in multiple sessions being created.  Which is bad.  Avoid this at all costs.
         * Please.
         */


        if (!sendInitialMessage(callback)) {
            showError("Could not connect to remote bus", "", null);
        }
    }

    private boolean sendInitialMessage(final HookCallback callback) {
        try {

            String initialMessage = "{\"CommandType\":\"ConnectToQueue\",\"ToSubject\":\"ServerBus\", \"PriorityProcessing\":\"1\"}";

            sendBuilder.sendRequest(initialMessage, new RequestCallback() {
                public void onResponseReceived(Request request, Response response) {
                    try {
                        procIncomingPayload(response);
                        initializeMessagingBus(callback);
                    }
                    catch (Exception e) {
                        showError("Error attaching to bus", e.getMessage() + "<br/>Message Contents:<br/>" + response.getText(), e);
                        return;
                    }
                }

                public void onError(Request request, Throwable exception) {
                    System.out.println("ERROR");
                }
            });
        }
        catch (RequestException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }


    public boolean isInitialized() {
        return initialized;
    }

    @SuppressWarnings({"UnusedDeclaration"})
    private void initializeMessagingBus(final HookCallback initCallback) {
        final com.google.gwt.user.client.Timer incoming = new com.google.gwt.user.client.Timer() {
            boolean block = false;
            @Override
            public void run() {
                if (block) {
                    scheduleRepeating(25);
                    return;
                }

                block = true;
                try {
                    recvBuilder.sendRequest(null,
                            new RequestCallback() {
                                public void onError(Request request, Throwable throwable) {
                                    block = false;
                                    showError("Communication Error", "None", throwable);
                                    cancel();
                                //    schedule(1);
                                }

                                public void onResponseReceived(Request request, Response response) {
                                    block = false;

                                    try {
                                        procIncomingPayload(response);
                                        schedule(1);
                                    }
                                    catch (Exception e) {
                                        showError("Errai MessageBus Disconnected Due to Fatal Error", response.getText(), e);
                                        cancel();
                                    }
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

        final MessageBus bus = this;

        final com.google.gwt.user.client.Timer outerTimer = new com.google.gwt.user.client.Timer() {
            @Override
            public void run() {
                incoming.scheduleRepeating(500);
                ExtensionsLoader loader = GWT.create(ExtensionsLoader.class);
                loader.initExtensions(bus);
            }
        };

        outerTimer.schedule(10);
    }

    public void addPostInitTask(Runnable run) {
        postInitTasks.add(run);
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

    public static void store(String subject, Object value) {
        try {
            _store(subject, value);
        }
        catch (Exception e) {
            showError("Error `sending data to client bus for '" + subject + "'", "Value:" + value, e);
        }
    }

    public native static void _store(String subject, Object value) /*-{
          $wnd.PageBus.store(subject, value);
     }-*/;

    private static String decodeCommandMessage(CommandMessage msg) {
        StringBuffer decode = new StringBuffer(
                "<table><thead style='font-weight:bold;'><tr><td>Field</td><td>Value</td></tr></thead><tbody>");

        for (Map.Entry<String, Object> entry : msg.getParts().entrySet()) {
            decode.append("<tr><td>").append(entry.getKey()).append("</td><td>").append(entry.getValue()).append("</td></tr>");
        }
        decode.append("</tbody></table>");

        return decode.toString();
    }

    private static void showError(String message, String additionalDetails, Throwable e) {
        final DialogBox errorDialog = new DialogBox();
        errorDialog.setText("Message Bus Error");

        StringBuffer buildTrace = new StringBuffer("<tt>");

        if (e != null) {
            buildTrace.append(e.getClass().getName()).append(": ").append(e.getMessage()).append("<br/>");
            for (StackTraceElement ste : e.getStackTrace()) {
                buildTrace.append(ste.toString()).append("<br/>");
            }
        }

        VerticalPanel panel = new VerticalPanel();
        Button closeButton = new Button("Dismiss");
        closeButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                errorDialog.hide();
            }
        });


        panel.add(closeButton);
        panel.setCellHorizontalAlignment(closeButton, HasHorizontalAlignment.ALIGN_RIGHT);

        Style s = panel.getElement().getStyle();

        s.setProperty("border", "1px");
        s.setProperty("borderStyle", "solid");
        s.setProperty("borderColor", "black");
        s.setProperty("backgroundColor", "lightgrey");

        panel.add(new HTML("<strong style='background:red;color:white;'>" + message + "</strong>"));

        ScrollPanel scrollPanel = new ScrollPanel();
        scrollPanel.add(new HTML(buildTrace.toString() + "<br/><strong>Additional Details:</strong>" + additionalDetails + "</tt>"));
        scrollPanel.setAlwaysShowScrollBars(true);
        scrollPanel.setHeight("500px");

        panel.add(scrollPanel);


        errorDialog.add(panel);

        errorDialog.center();
        errorDialog.show();
    }

    /**
     * Process the incoming payload and push all the incoming messages onto the bus.
     *
     * @param response
     * @throws Exception
     */
    private static void procIncomingPayload(Response response) throws Exception {
        for (Message m : decodePayload(response.getText())) {
            store(m.getSubject(), m.getMessage());
        }
    }
}
