/*
 * Copyright 2011 JBoss, by Red Hat, Inc
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

package org.jboss.errai.bus.client.framework;

import static org.jboss.errai.bus.client.protocols.BusCommands.RemoteSubscribe;
import static org.jboss.errai.bus.client.protocols.BusCommands.RemoteUnsubscribe;
import static org.jboss.errai.common.client.protocols.MessageParts.PriorityProcessing;
import static org.jboss.errai.common.client.protocols.MessageParts.Subject;
import static org.jboss.errai.common.client.protocols.MessageParts.ToSubject;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import org.jboss.errai.bus.client.api.BusLifecycleEvent;
import org.jboss.errai.bus.client.api.BusLifecycleListener;
import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.api.MessageCallback;
import org.jboss.errai.bus.client.api.SubscribeListener;
import org.jboss.errai.bus.client.api.UnsubscribeListener;
import org.jboss.errai.bus.client.api.base.Capabilities;
import org.jboss.errai.bus.client.api.base.CommandMessage;
import org.jboss.errai.bus.client.api.base.DefaultErrorCallback;
import org.jboss.errai.bus.client.api.base.NoSubscribersToDeliverTo;
import org.jboss.errai.bus.client.framework.transports.BusTransportError;
import org.jboss.errai.bus.client.framework.transports.HttpPollingHandler;
import org.jboss.errai.bus.client.framework.transports.SSEHandler;
import org.jboss.errai.bus.client.framework.transports.TransportHandler;
import org.jboss.errai.bus.client.framework.transports.WebsocketHandler;
import org.jboss.errai.bus.client.protocols.BusCommands;
import org.jboss.errai.bus.client.util.BusToolsCli;
import org.jboss.errai.bus.client.util.ManagementConsole;
import org.jboss.errai.common.client.api.Assert;
import org.jboss.errai.common.client.api.extension.InitVotes;
import org.jboss.errai.common.client.protocols.MessageParts;
import org.jboss.errai.common.client.util.LogUtil;
import org.jboss.errai.marshalling.client.api.MarshallerFramework;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The default client <tt>MessageBus</tt> implementation.  This bus runs in the browser and automatically federates
 * with the server immediately upon initialization.
 *
 * @author Mike Brock
 */
public class ClientMessageBusImpl implements ClientMessageBus {
  static {
    MarshallerFramework.initializeDefaultSessionProvider();
  }

  String OUT_SERVICE_ENTRY_POINT;
  String IN_SERVICE_ENTRY_POINT;

  private final String clientId;
  private String sessionId;

  private final List<SubscribeListener> onSubscribeHooks = new ArrayList<SubscribeListener>();
  private final List<UnsubscribeListener> onUnsubscribeHooks = new ArrayList<UnsubscribeListener>();

  public final MessageCallback remoteCallback = new MessageCallback() {
    @Override
    public void callback(final Message message) {
      encodeAndTransmit(message);
    }
  };

  private final MessageCallback transportToBusCallback = new MessageCallback() {
    @Override
    public void callback(final Message message) {
      processMessageFromTransportLayer(message.getSubject(), message);
    }
  };

  private Map<String, TransportHandler> availableHandlers;

  private final TransportHandler BOOTSTRAP_HANDLER = HttpPollingHandler.newNoPollingInstance(transportToBusCallback, ClientMessageBusImpl.this);

  /**
   * The current transport handler that's in use. This field is never null; it bottoms out at the No-polling version of HttpPollingHandler.
   */
  private TransportHandler transportHandler = BOOTSTRAP_HANDLER;

  private final Map<String, List<MessageCallback>> subscriptions = new HashMap<String, List<MessageCallback>>();
  private final Map<String, List<MessageCallback>> localSubscriptions = new HashMap<String, List<MessageCallback>>();
  private final Map<String, MessageCallback> remotes = new HashMap<String, MessageCallback>();

  private final List<TransportErrorHandler> transportErrorHandlers = new ArrayList<TransportErrorHandler>();

  private final List<Runnable> deferredSubscriptions = new ArrayList<Runnable>();
  private final List<Message> deferredMessages = new ArrayList<Message>();

  private final List<BusLifecycleListener> lifecycleListeners = new ArrayList<BusLifecycleListener>();

  private BusState state = BusState.LOCAL_ONLY;

  private long lastSleepTick = System.currentTimeMillis();
  private boolean lastSleepCheckWasOnline = false;

  private final ManagementConsole managementConsole;

  public ClientMessageBusImpl() {
    setBusToInitializableState();

    managementConsole = new ManagementConsole(this);

    clientId = String.valueOf(com.google.gwt.user.client.Random.nextInt(99999)) + "-"
        + (System.currentTimeMillis() % (com.google.gwt.user.client.Random.nextInt(99999) + 1));


    IN_SERVICE_ENTRY_POINT = "in." + getClientId() + ".erraiBus";
    OUT_SERVICE_ENTRY_POINT = "out." + getClientId() + ".erraiBus";

    // when the window is closing, we want to stop the bus without causing any
    // errors (unless the server is unavailable of course) (see ERRAI-225)
    Window.addCloseHandler(new CloseHandler<Window>() {
      @Override
      public void onClose(final CloseEvent<Window> event) {
        if (state != BusState.LOCAL_ONLY) {
          stop(true);
        }
      }
    });
  }

  private void setBusToInitializableState() {
    this.remotes.clear();
    this.subscriptions.clear();

    this.onSubscribeHooks.clear();
    this.onUnsubscribeHooks.clear();
    this.deferredMessages.clear();
    this.transportHandler = BOOTSTRAP_HANDLER;

    setupDefaultHandlers();
  }

  private void setupDefaultHandlers() {
    availableHandlers = Collections.unmodifiableMap(new LinkedHashMap<String, TransportHandler>() {
      {
        put(Capabilities.WebSockets.name(), new WebsocketHandler(transportToBusCallback, ClientMessageBusImpl.this));
        put(Capabilities.SSE.name(), new SSEHandler(transportToBusCallback, ClientMessageBusImpl.this));
        put(Capabilities.LongPolling.name(), HttpPollingHandler.newLongPollingInstance(transportToBusCallback, ClientMessageBusImpl.this));
        put(Capabilities.ShortPolling.name(), HttpPollingHandler.newShortPollingInstance(transportToBusCallback, ClientMessageBusImpl.this));
      }
    });
  }

  /**
   * Takes this message bus from the LOCAL_ONLY state into the CONNECTING state,
   * as long as remote communication is enabled.
   * <p/>
   * If this bus is not in the LOCAL_ONLY state when this method is called, this
   * method has no effect.
   *
   * @see org.jboss.errai.bus.client.util.BusToolsCli#isRemoteCommunicationEnabled()
   * @see BusLifecycleListener
   */
  @Override
  public void init() {
    if (getState() == BusState.CONNECTED) {

      /**
       * This is an optimization to improve unit testing speed. If a testcase does not tear down the bus after
       * each test, calling this will ensure that any services dependent on the bus will still be loaded.
       */
      InitVotes.voteFor(ClientMessageBus.class);
      return;
    }

    LogUtil.log("bus initialization started ...");
    setBusToInitializableState();

    directSubscribe(DefaultErrorCallback.CLIENT_ERROR_SUBJECT, new MessageCallback() {
      @Override
      public void callback(final Message message) {
        final String errorTo = message.get(String.class, MessageParts.ErrorTo);
        if (errorTo == null) {
          managementConsole.displayError(message.get(String.class, MessageParts.ErrorMessage),
              message.get(String.class, MessageParts.AdditionalDetails), null);
        }
        else {
          message.toSubject(errorTo);
          message.sendNowWith(ClientMessageBusImpl.this);
        }
      }
    }, false);

    registerInitVoteCallbacks();

    if (BusToolsCli.isRemoteCommunicationEnabled()) {
      remoteSubscribe(BuiltInServices.ServerEchoService.name());
    }

    directSubscribe(BuiltInServices.ClientBus.name(), new MessageCallback() {
      @Override
      @SuppressWarnings({"unchecked"})
      public void callback(final Message message) {
        BusCommands busCommands = BusCommands.valueOf(message.getCommandType());
        if (busCommands == null) {
          busCommands = BusCommands.Unknown;
        }
        switch (busCommands) {
          case RemoteSubscribe:
            if (message.hasPart(MessageParts.SubjectsList)) {
              LogUtil.log("remote services available: " + message.get(List.class, MessageParts.SubjectsList));

              for (final String subject : (List<String>) message.get(List.class, MessageParts.SubjectsList)) {
                remoteSubscribe(subject);
              }
            }
            else {
              remoteSubscribe(message.get(String.class, Subject));
            }
            break;

          case RemoteUnsubscribe:
            unsubscribeAll(message.get(String.class, Subject));
            break;

          case FinishAssociation:
            LogUtil.log("received FinishStateSync message. preparing to bring up the federation");

            loadRpcProxies();
            processCapabilities(message);

            for (final String svc : message.get(String.class, MessageParts.RemoteServices).split(",")) {
              remoteSubscribe(svc);
            }

            sessionId = message.get(String.class, MessageParts.ConnectionSessionKey);
            remoteSubscribe(BuiltInServices.ServerBus.name());

            if (!deferredSubscriptions.isEmpty()) {
              for (final Runnable deferredSubscription : deferredSubscriptions) {
                deferredSubscription.run();
              }
              deferredSubscriptions.clear();

              encodeAndTransmit(CommandMessage.createWithParts(new HashMap<String, Object>())
                  .toSubject(BuiltInServices.ServerBus.name()).command(BusCommands.RemoteSubscribe)
                  .set(PriorityProcessing, "1")
                  .set(MessageParts.RemoteServices, getAdvertisableSubjects()));
            }

            // We don't want to declare the subscription listeners until after we've sent our initial state
            // to the bus.
            declareSubscriptionListeners();

            setState(BusState.CONNECTED);
            startSleepDetector();
            sendAllDeferred();
            InitVotes.voteFor(ClientMessageBus.class);
            LogUtil.log("bus federated and running.");
            break;

          case SessionExpired:
            switch (getState()) {
              case CONNECTED:
                // try to reconnect
                stop(false);
                init();
                break;

              case CONNECTING:
              case LOCAL_ONLY:
                // do nothing
                break;
            }

            break;

          case Disconnect:
            stop(false);

            if (message.hasPart("Reason")) {
              managementConsole.displayError("The bus was disconnected by the server", "Reason: " + message.get(String.class, "Reason"), null);
            }
            break;

          case Heartbeat:
          case Resend:
            break;

          case Unknown:
          default:
            transportHandler.handleProtocolExtension(message);
            break;
        }
      }
    }, false);

    // The purpose of this timer is to let the bus yield and give other modules a chance to register
    // services before we send our state synchronization message. This is not strictly necessary
    // but significantly decreases network chattiness since more (if not all known services)
    // can then be listed in the initial handshake message.
    new Timer() {
      @Override
      public void run() {
        sendInitialMessage();
      }
    }.schedule(50);
  }

  /**
   * Sends the initial message to connect to the queue, to establish an HTTP
   * session. Otherwise, concurrent requests will result in multiple sessions
   * being created.
   *
   * @return true if initial message was sent successfully.
   */
  private void sendInitialMessage() {
    if (!BusToolsCli.isRemoteCommunicationEnabled()) {
      LogUtil.log("initializing client bus in offline mode (erraiBusRemoteCommunicationEnabled was set to false)");
      InitVotes.voteFor(ClientMessageBus.class);
      return;
    }

    if (getState() != BusState.LOCAL_ONLY) {
      LogUtil.log("aborting startup. bus is not in correct state.");
      return ;
    }


    setState(BusState.CONNECTING);

    try {
      LogUtil.log("sending initial handshake to remote bus");

      // XXX sending a custom HTTP header here is very fancy, but invites compatibility
      // problems (for example with CORS requests, locked down intermediate proxies, old
      // browsers, etc). It seems that we send this header if-and-only-if CommandType is
      // ConnectToQueue. We should look at making the server message bus not require it.
      final Map<String, String> connectHeader = Collections.singletonMap("phase", "connection");

      for (final Runnable deferredSubscription : deferredSubscriptions) {
        deferredSubscription.run();
      }
      deferredSubscriptions.clear();

      final Message initialMessage = CommandMessage.createWithParts(new HashMap<String, Object>())
          .command(BusCommands.Associate)
          .set(ToSubject, "ServerBus")
          .set(PriorityProcessing, "1")
          .set(MessageParts.RemoteServices, getAdvertisableSubjects());

      ((HttpPollingHandler) transportHandler).sendOutboundRequest(BusToolsCli.encodeMessage(initialMessage),
          connectHeader, new RequestCallback() {
        @Override
        public void onResponseReceived(final Request request, final Response response) {
          try {
            LogUtil.log("received response from initial handshake.");
            BusToolsCli.decodeToCallback(response.getText(), transportToBusCallback);
          }
          catch (Exception e) {
            e.printStackTrace();
            managementConsole.displayError("Error attaching to bus",
                e.getMessage() + "<br/>Message Contents:<br/>" + response.getText(), e);
          }
        }

        @Override
        public void onError(final Request request, final Throwable exception) {
          managementConsole.displayError("Could not connect to remote bus", "", exception);
        }
      });
    }
    catch (RequestException e) {
      e.printStackTrace();
    }
  }

  private void processCapabilities(final Message message) {
    for (final String capability : message.get(String.class, MessageParts.CapabilitiesFlags).split(",")) {
      final TransportHandler handler = availableHandlers.get(capability);
      if (handler == null) {
        LogUtil.log("warning: could not find handler for capability type: " + capability);
        continue;
      }

      handler.configure(message);
    }

    reconsiderTransport();
  }

  private void declareSubscriptionListeners() {
    addUnsubscribeListener(new UnsubscribeListener() {
      @Override
      public void onUnsubscribe(final SubscriptionEvent event) {
        final String subject = event.getSubject();

        if (subject.endsWith(":RespondTo:RPC") || subject.endsWith(":Errors:RPC")) {
          return;
        }

        encodeAndTransmit(CommandMessage.createWithParts(new HashMap<String, Object>())
            .toSubject(BuiltInServices.ServerBus.name()).command(RemoteUnsubscribe)
            .set(Subject, subject).set(PriorityProcessing, "1"));
      }
    });

    addSubscribeListener(new SubscribeListener() {
      @Override
      public void onSubscribe(final SubscriptionEvent event) {
        final String subject = event.getSubject();
        if (event.isLocalOnly() || subject.startsWith("local:")
            || remotes.containsKey(subject)) {
          return;
        }

        if (subject.endsWith(":RespondTo:RPC") || subject.endsWith(":Errors:RPC")) {
          return;
        }

        if (event.isNew()) {
          encodeAndTransmit(CommandMessage.createWithParts(new HashMap<String, Object>())
              .toSubject(BuiltInServices.ServerBus.name()).command(RemoteSubscribe)
              .set(Subject, subject).set(PriorityProcessing, "1"));
        }
      }
    });

  }

  @Override
  public void stop(final boolean sendDisconnect) {
    stop(sendDisconnect, null);
  }

  private void stop(final boolean sendDisconnect, final TransportError reason) {
    LogUtil.log("stopping bus ...");

    // Optionally tell the server we're going away (this causes two POST requests)
    try {
      if (sendDisconnect && BusToolsCli.isRemoteCommunicationEnabled()) {
        encodeAndTransmit(CommandMessage.createWithParts(new HashMap<String, Object>())
            .toSubject(BuiltInServices.ServerBus.name()).command(BusCommands.Disconnect)
            .set(MessageParts.PriorityProcessing, "1"));
      }

      subscriptions.clear();
      transportHandler.stop(true);
    }
    finally {
      if (state != BusState.LOCAL_ONLY)
        setState(BusState.LOCAL_ONLY, reason);
    }
  }

  private void startSleepDetector() {
    new Timer() {
      @Override
      public void run() {
        final long currentTime = System.currentTimeMillis();

        if (lastSleepTick - currentTime > 30000) {
          LogUtil.log("we might be asleep.");

          if (state == BusState.LOCAL_ONLY && lastSleepCheckWasOnline) {
            init();
          }
          else {
            sendInterrogationEcho(2000, new Runnable() {
              @Override
              public void run() {
                stop(false);
                init();
              }
            });
          }
        }

        lastSleepTick = currentTime;
        lastSleepCheckWasOnline = (state == BusState.CONNECTED);
      }
    }.scheduleRepeating(1000);
  }

  private String getAdvertisableSubjects() {
    String subjects = "";
    for (final String s : subscriptions.keySet()) {
      if (s.startsWith("local:"))
        continue;
      if (!remotes.containsKey(s)) {
        if (subjects.length() != 0) {
          subjects += ",";
        }
        subjects += s;
      }
    }
    return subjects;
  }

  public String getClientId() {
    return clientId;
  }

  public String getSessionId() {
    return sessionId;
  }

  private void sendInterrogationEcho(final int timeoutMillis, final Runnable failure) {
    final String receivingSubject = "^ClientEchoReceiver";

    final Timer failureTimer = new Timer() {
      @Override
      public void run() {
        failure.run();
        unsubscribeAll(receivingSubject);
      }
    };

    failureTimer.schedule(timeoutMillis);

    subscribe(receivingSubject, new MessageCallback() {
      @Override
      public void callback(final Message message) {
        unsubscribeAll(receivingSubject);
        failureTimer.cancel();
      }
    });

    CommandMessage.createWithParts(new HashMap<String, Object>())
        .toSubject("ServerEchoService")
        .set(MessageParts.Value, "HelloServer");
  }

  /**
   * Removes all subscriptions attached to the specified subject
   *
   * @param subject
   *     - the subject to have all it's subscriptions removed
   */
  @Override
  public void unsubscribeAll(final String subject) {
    fireAllUnSubscribeListeners(subject);
    removeSubscriptionTopic(subject);
  }

  /**
   * Add a subscription for the specified subject
   *
   * @param subject
   *     - the subject to add a subscription for
   * @param callback
   *     - function called when the message is dispatched
   */
  @Override
  public Subscription subscribe(final String subject, final MessageCallback callback) {
    return _subscribe(subject, callback, false);
  }

  @Override
  public Subscription subscribeLocal(final String subject, final MessageCallback callback) {
    return _subscribe(subject, callback, true);
  }

  private Subscription _subscribe(final String subject, final MessageCallback callback, final boolean local) {
    if (getState() == BusState.CONNECTING) {
      return _subscribeDeferred(subject, callback, local);
    }
    else {
      return _subscribeNow(subject, callback, local);
    }
  }

  private Subscription _subscribeDeferred(final String subject, final MessageCallback callback, final boolean local) {
    final DeferredSubscription deferredSubscription = new DeferredSubscription();

    deferredSubscriptions.add(new Runnable() {
      @Override
      public void run() {
        deferredSubscription.attachSubscription(_subscribeNow(subject, callback, local));
      }

      @Override
      public String toString() {
        return "DeferredSubscribe:" + subject;
      }
    });

    return deferredSubscription;
  }

  private Subscription _subscribeNow(final String subject, final MessageCallback callback, final boolean local) {
    if (BuiltInServices.ServerBus.name().equals(subject) && subscriptions.containsKey(BuiltInServices.ServerBus.name()))
      return null;

    final WrappedCallbackHolder wrappedCallbackHolder = new WrappedCallbackHolder(callback);
    fireAllSubscribeListeners(subject, local, directSubscribe(subject, callback, local, wrappedCallbackHolder));

    return new Subscription() {
      @Override
      public void remove() {
        final List<MessageCallback> cbs = local ? localSubscriptions.get(subject) : subscriptions.get(subject);
        if (cbs != null) {
          cbs.remove(wrappedCallbackHolder.getWrappedCallback());
          if (cbs.isEmpty()) {
            unsubscribeAll(subject);
          }
        }
      }
    };
  }

  private boolean directSubscribe(final String subject,
                                  final MessageCallback callback,
                                  final boolean local) {

    return directSubscribe(subject, callback, local, new WrappedCallbackHolder(null));
  }

  private boolean directSubscribe(final String subject,
                                  final MessageCallback callback,
                                  final boolean local,
                                  final WrappedCallbackHolder callbackHolder) {
    final boolean isNew = !isSubscribed(subject);

    final MessageCallback cb = new MessageCallback() {
      @Override
      public void callback(final Message message) {
        try {
          callback.callback(message);
        }
        catch (Exception e) {
          managementConsole.displayError("receiver '" + subject + "' threw an exception", decodeCommandMessage(message), e);
        }
      }
    };

    callbackHolder.setWrappedCallback(cb);

    if (local) {
      addLocalSubscriptionEntry(subject, cb);
    }
    else {
      addSubscriptionEntry(subject, cb);
    }

    return isNew;
  }

  /**
   * Fire listeners to notify that a new subscription has been registered on the
   * bus.
   *
   * @param subject
   *     - new subscription registered
   * @param local
   *     -
   * @param isNew
   *     -
   */
  private void fireAllSubscribeListeners(final String subject, final boolean local, final boolean isNew) {
    final Iterator<SubscribeListener> iterator = onSubscribeHooks.iterator();
    final SubscriptionEvent evt = new SubscriptionEvent(false, false, local, isNew, 1, "InBrowser", subject);

    while (iterator.hasNext()) {
      iterator.next().onSubscribe(evt);

      if (evt.isDisposeListener()) {
        iterator.remove();
        evt.setDisposeListener(false);
      }
    }
  }

  /**
   * Fire listeners to notify that a subscription has been unregistered from the
   * bus
   *
   * @param subject
   *     - subscription unregistered
   */
  private void fireAllUnSubscribeListeners(final String subject) {
    final Iterator<UnsubscribeListener> iterator = onUnsubscribeHooks.iterator();
    final SubscriptionEvent evt = new SubscriptionEvent(false, "InBrowser", 0, false, subject);

    while (iterator.hasNext()) {
      iterator.next().onUnsubscribe(evt);
      if (evt.isDisposeListener()) {
        iterator.remove();
        evt.setDisposeListener(false);
      }
    }
  }

  /**
   * Globally send message to all receivers.
   *
   * @param message
   *     - The message to be sent.
   */
  @Override
  public void sendGlobal(final Message message) {
    send(message);
  }

  /**
   * Sends the specified message, and notifies the listeners.
   *
   * @param message
   *     - the message to be sent
   * @param fireListeners
   *     - true if the appropriate listeners should be fired
   */
  @Override
  public void send(final Message message, final boolean fireListeners) {
    // TODO: fire listeners?
    send(message);
  }

  /**
   * Sends the message using it's encoded subject. If the bus has not been initialized, it will be added to
   * <tt>postInitTasks</tt>.
   *
   * @param message
   *     -
   *
   * @throws RuntimeException
   *     - if message does not contain a ToSubject field or if the
   *     message's callback throws an error.
   */
  @Override
  public void send(final Message message) {
    message.setResource(RequestDispatcher.class.getName(), BusToolsCli.getRequestDispatcherProvider())
        .setResource("Session", BusToolsCli.getClientSession()).commit();

    try {
      boolean deferred = false;
      if (message.hasPart(MessageParts.ToSubject)) {
        if (BusToolsCli.isRemoteCommunicationEnabled() && getState() != BusState.CONNECTED) {
          deferredMessages.add(message);
          deferred = true;
        }

        final String subject = message.getSubject();
        boolean routedToRemote = false;

        if (!message.isFlagSet(RoutingFlag.DeliverLocalOnly) && remotes.containsKey(subject)) {
          remotes.get(subject).callback(message);
          routedToRemote = true;
        }

        if (subscriptions.containsKey(subject)) {
          deliverToSubscriptions(subscriptions, subject, message);
        }
        else if (localSubscriptions.containsKey(subject)) {
          deliverToSubscriptions(localSubscriptions, subject, message);
        }
        else if (!deferred && !routedToRemote) {
          throw new NoSubscribersToDeliverTo(subject);
        }
      }
      else {
        throw new RuntimeException("Cannot send message using this method"
            + " if the message does not contain a ToSubject field.");
      }
    }
    catch (RuntimeException e) {
      callErrorHandler(message, e);
      throw e;
    }
  }

  private void processMessageFromTransportLayer(final String subject, final Message msg) {
    if (subscriptions.containsKey(subject)) {
      final ArrayList<MessageCallback> messageCallbacks = new ArrayList<MessageCallback>(subscriptions.get(subject));
      for (final MessageCallback cb : messageCallbacks) {
        cb.callback(msg);
      }
    }
  }

  public void callErrorHandler(final Message message, final Throwable t) {
    if (message.getErrorCallback() != null) {
      message.getErrorCallback().error(message, t);
    }
    managementConsole.displayError(t.getMessage(), "none", t);
  }

  public void encodeAndTransmit(final Message message) {
    if (getState() == BusState.LOCAL_ONLY) return;

    transportHandler.transmit(Collections.singletonList(message));
  }

  private void addSubscriptionEntry(final String subject, final MessageCallback reference) {
    _addCallbackEntry(subscriptions, subject, reference);
  }

  private void addLocalSubscriptionEntry(final String subject, final MessageCallback reference) {
    _addCallbackEntry(localSubscriptions, subject, reference);
  }

  private static void _addCallbackEntry(final Map<String, List<MessageCallback>> subscriptions,
                                        final String subject,
                                        final MessageCallback reference) {
    if (!subscriptions.containsKey(subject)) {
      subscriptions.put(subject, new ArrayList<MessageCallback>());
    }

    if (!subscriptions.get(subject).contains(reference)) {
      subscriptions.get(subject).add(reference);
    }
  }

  private void removeSubscriptionTopic(final String subject) {
    subscriptions.remove(subject);
  }

  private static void deliverToSubscriptions(final Map<String, List<MessageCallback>> subscriptions,
                                             final String subject,
                                             final Message message) {
    for (final MessageCallback cb : subscriptions.get(subject)) {
      cb.callback(message);
    }
  }


  /**
   * Checks if subject is already listed in the subscriptions map
   *
   * @param subject
   *     - subject to look for
   *
   * @return true if the subject is already subscribed
   */
  @Override
  public boolean isSubscribed(final String subject) {
    return subscriptions.containsKey(subject);
  }

  private void registerInitVoteCallbacks() {
    InitVotes.waitFor(ClientMessageBus.class);
  }

  private void remoteSubscribe(final String subject) {
    remotes.put(subject, remoteCallback);
  }

  Set<String> getRemoteSubscriptions() {
    return remotes.keySet();
  }

  private void sendAllDeferred() {
    if (!deferredMessages.isEmpty())
      LogUtil.log("transmitting deferred messages now ...");

    final List<Message> highPriority = new ArrayList<Message>();
    for (final Message message : new ArrayList<Message>(deferredMessages)) {
      if (message.hasPart(MessageParts.PriorityProcessing)) {
        highPriority.add(message);
        deferredMessages.remove(message);
      }
    }

    final List<Message> lowPriority = new ArrayList<Message>();
    do {
      for (final Message message : new ArrayList<Message>(deferredMessages)) {
        lowPriority.add(message);
        deferredMessages.remove(message);
      }
    }
    while (!deferredMessages.isEmpty());

    transportHandler.transmit(highPriority);
    transportHandler.transmit(lowPriority);
  }

  public boolean handleTransportError(final BusTransportError transportError) {
    for (final TransportErrorHandler handler : transportErrorHandlers) {
      handler.onError(transportError);
    }

    if (!transportError.isStopDefaultErrorHandler()) {
      if (state == BusState.CONNECTED) {
        setState(BusState.CONNECTION_INTERRUPTED, transportError);
      }
      else if (state != BusState.CONNECTING && state != BusState.CONNECTION_INTERRUPTED) {
        LogUtil.log("got a transport error while in the " + state + " state");
      }
    }

    return transportError.isStopDefaultErrorHandler();
  }

  /**
   * Initializes the message bus by setting up the <tt>recvBuilder</tt> to
   * accept responses. Also, initializes the incoming timer to ensure the
   * client's polling with the server is active.
   */
  private void loadRpcProxies() {
    ((RpcProxyLoader) GWT.create(RpcProxyLoader.class)).loadProxies(ClientMessageBusImpl.this);
  }

  /**
   * Adds a subscription listener, so it is possible to add subscriptions to the
   * client.
   *
   * @param listener
   *     - subscription listener
   */
  @Override
  public void addSubscribeListener(final SubscribeListener listener) {
    this.onSubscribeHooks.add(Assert.notNull(listener));
  }

  /**
   * Adds an unsubscribe listener, so it is possible for applications to remove
   * subscriptions from the client
   *
   * @param listener
   *     - unsubscribe listener
   */
  @Override
  public void addUnsubscribeListener(final UnsubscribeListener listener) {
    this.onUnsubscribeHooks.add(listener);
  }

  private static String decodeCommandMessage(final Message msg) {
    final StringBuilder decode = new StringBuilder(
        "<table><thead style='font-weight:bold;'><tr><td>Field</td><td>Value</td></tr></thead><tbody>");

    for (final Map.Entry<String, Object> entry : msg.getParts().entrySet()) {
      decode.append("<tr><td>").append(entry.getKey()).append("</td><td>").append(entry.getValue())
          .append("</td></tr>");
    }

    return decode.append("</tbody></table>").toString();
  }

  /**
   * When called, the MessageBus assumes that the currently active transport is no longer capable of operating. The
   * MessageBus then find the best remaining handler and activates it.
   */
  @Override
  public void reconsiderTransport() {
    TransportHandler newHandler = null;
    for (final TransportHandler handler : availableHandlers.values()) {
      if (handler.isUsable()) {
        newHandler = handler;
        break;
      }
    }

    if (newHandler == null) {
      LogUtil.log("no available transports! stopping bus!");
      stop(false);
    }
    else if (newHandler != transportHandler) {
      LogUtil.log("transitioning to new handler: " + newHandler);

      transportHandler.stop(false);
      transportHandler = newHandler;
      transportHandler.start();
    }
    // 3rd case: we're already using the best available handler. Do nothing.
  }

  @Override
  public void attachMonitor(final BusMonitor monitor) {
    // only supported server-side right now.
  }

  @Override
  public Set<String> getAllRegisteredSubjects() {
    return Collections.unmodifiableSet(subscriptions.keySet());
  }

  @Override
  public void addTransportErrorHandler(final TransportErrorHandler errorHandler) {
    transportErrorHandlers.add(errorHandler);
  }

  public BusState getState() {
    return state;
  }

  public Set<String> getRemoteServices() {
    return new HashSet<String>(remotes.keySet());
  }

  public Set<String> getLocalServices() {
    return new HashSet<String>(subscriptions.keySet());
  }

  public String getApplicationLocation(final String serviceEntryPoint) {
    final Configuration configuration = GWT.create(Configuration.class);
    if (configuration instanceof Configuration.NotSpecified) {
      return BusToolsCli.getApplicationRoot() + serviceEntryPoint;
    }
    return configuration.getRemoteLocation() + serviceEntryPoint;
  }

  public String getOutServiceEntryPoint() {
    return OUT_SERVICE_ENTRY_POINT;
  }

  public String getInServiceEntryPoint() {
    return IN_SERVICE_ENTRY_POINT;
  }

  @Override
  public void addLifecycleListener(final BusLifecycleListener l) {
    lifecycleListeners.add(Assert.notNull(l));
  }

  @Override
  public void removeLifecycleListener(final BusLifecycleListener l) {
    lifecycleListeners.remove(l);
  }

  /**
   * Puts the bus in the given state, firing all necessary transition events with no <tt>reason</tt> field.
   */
  public void setState(final BusState newState) {
    setState(newState, null);
  }

  /**
   * Puts the bus in the given state, firing all necessary transition events with the given reason.
   *
   * @param reason
   *     The error that led to this state transition, if any. Null is permitted.
   */
  private void setState(final BusState newState, final TransportError reason) {
    if (state == newState) {
      GWT.log("bus tried to transition from " + state + " ");
      return;
    }

    final List<BusEventType> events = new ArrayList<BusEventType>();

    switch (state) {
      case LOCAL_ONLY:
        if (newState == BusState.CONNECTING) {
          events.add(BusEventType.ASSOCIATING);
        }
        else if (newState == BusState.CONNECTED) {
          events.add(BusEventType.ASSOCIATING);
          events.add(BusEventType.ONLINE);
        }
        break;

      case CONNECTION_INTERRUPTED:
      case CONNECTING:
        if (newState == BusState.LOCAL_ONLY) {
          events.add(BusEventType.DISASSOCIATING);
        }
        else if (newState == BusState.CONNECTED) {
          events.add(BusEventType.ONLINE);
        }
        break;

      case CONNECTED:
        if (newState == BusState.CONNECTING || newState == BusState.CONNECTION_INTERRUPTED) {
         events.add(BusEventType.OFFLINE);
        }
        else if (newState == BusState.LOCAL_ONLY) {
          events.add(BusEventType.OFFLINE);
          events.add(BusEventType.DISASSOCIATING);
        }
        break;

      default:
        throw new IllegalStateException("Bus is in unknown state: " + state);
    }

    state = newState;

    if (newState == BusState.CONNECTION_INTERRUPTED) {
      LogUtil.log("the connection to the server has been interrupted ...");
    }

    for (final BusEventType et : events) {
      final BusLifecycleEvent e = new BusLifecycleEvent(this, reason);
      for (int i = lifecycleListeners.size() - 1; i >= 0; i--) {
        try {
          et.deliverTo(lifecycleListeners.get(i), e);
        }
        catch (Throwable t) {
          LogUtil.log("listener threw exception: " + t);
          t.printStackTrace();
        }
      }
    }
  }
}
