/*
 * Copyright (C) 2011 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.bus.client.framework;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import org.jboss.errai.bus.client.api.*;
import org.jboss.errai.bus.client.api.base.Capabilities;
import org.jboss.errai.bus.client.api.base.CommandMessage;
import org.jboss.errai.bus.client.api.base.DefaultErrorCallback;
import org.jboss.errai.bus.client.api.base.NoSubscribersToDeliverTo;
import org.jboss.errai.bus.client.api.messaging.MessageInterceptor;
import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.bus.client.api.messaging.MessageCallback;
import org.jboss.errai.bus.client.api.messaging.RequestDispatcher;
import org.jboss.errai.bus.client.framework.transports.*;
import org.jboss.errai.bus.client.protocols.BusCommand;
import org.jboss.errai.bus.client.util.BusToolsCli;
import org.jboss.errai.bus.client.util.ManagementConsole;
import org.jboss.errai.common.client.api.Assert;
import org.jboss.errai.common.client.api.extension.InitVotes;
import org.jboss.errai.common.client.protocols.MessageParts;
import org.jboss.errai.marshalling.client.api.MarshallerFramework;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static org.jboss.errai.bus.client.protocols.BusCommand.RemoteSubscribe;
import static org.jboss.errai.bus.client.protocols.BusCommand.RemoteUnsubscribe;
import static org.jboss.errai.bus.client.util.BusToolsCli.isRemoteCommunicationEnabled;
import static org.jboss.errai.common.client.protocols.MessageParts.*;

import java.lang.Throwable;

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

  private final List<SubscribeListener> onSubscribeHooks = new ArrayList<>();
  private final List<UnsubscribeListener> onUnsubscribeHooks = new ArrayList<>();

  private final List<MessageInterceptor> interceptors = new ArrayList<>();

  /**
   * Forwards every message received across the communication link to the remote
   * server bus. This is the mechanism by which local messages are routed to the
   * server bus.
   * <p>
   * One instance of this callback can be subscribed to any number of subjects
   * simultaneously.
   */
  public final MessageCallback serverForwarder = new MessageCallback() {
    @Override
    public void callback(final Message message) {
      encodeAndTransmit(message);
    }
  };

  /**
   * This callback processes all messages sent to the
   * {@link DefaultErrorCallback#CLIENT_ERROR_SUBJECT} on this bus.
   */
  private final class ErrorProcessor implements MessageCallback {
    @Override
    public void callback(final Message message) {
      final String errorTo = message.get(String.class, MessageParts.ErrorTo);

      if (errorTo == null || DefaultErrorCallback.CLIENT_ERROR_SUBJECT.equals(errorTo)) {
        final Throwable t = message.get(Throwable.class, MessageParts.Throwable);
        if (GWT.getUncaughtExceptionHandler() != null) {
          GWT.getUncaughtExceptionHandler().onUncaughtException(t);
        }
        else {
          managementConsole.displayError(message.get(String.class, MessageParts.ErrorMessage),
                  message.get(String.class, MessageParts.AdditionalDetails), null);
        }
      }
      else {
        message.toSubject(errorTo);
        message.set(MessageParts.ErrorTo, null);
        message.sendNowWith(ClientMessageBusImpl.this);
      }
    }

  }
  private final ErrorProcessor clientBusErrorsCallback = new ErrorProcessor();

  /**
   * Processes bus protocol commands and passes protocol extensions to the
   * underlying transport handlers. Protocol commands include RemoteSubscribe,
   * SessionExpired, Disconnect, and so on. The complete set lives in the
   * {@link BusCommand} enum.
   */
  private final class ProtocolCommandProcessor implements MessageCallback {
    @Override
    @SuppressWarnings({"unchecked"})
    public void callback(final Message message) {
      final Logger logger = LoggerFactory.getLogger(getClass());
      BusCommand busCommand;
      if (message.getCommandType() == null) {
        busCommand = BusCommand.Unknown;
      }
      else {
        busCommand = BusCommand.valueOf(message.getCommandType());
      }
      if (busCommand == null) {
        busCommand = BusCommand.Unknown;
      }

      switch (busCommand) {
        case RemoteSubscribe:
          if (message.hasPart(MessageParts.SubjectsList)) {
            logger.info("remote services available: " + message.get(List.class, MessageParts.SubjectsList));

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
          sessionId = message.get(String.class, MessageParts.ConnectionSessionKey);
          logger.info("my queue session id: " + sessionId);

          processCapabilities(message);

          for (final String svc : message.get(String.class, MessageParts.RemoteServices).split(",")) {
            remoteSubscribe(svc);
          }

          remoteSubscribe(BuiltInServices.ServerBus.name());

          if (!deferredSubscriptions.isEmpty()) {
            for (final Runnable deferredSubscription : deferredSubscriptions) {
              deferredSubscription.run();
            }
            deferredSubscriptions.clear();

            encodeAndTransmit(CommandMessage.create()
                .toSubject(BuiltInServices.ServerBus.name()).command(BusCommand.RemoteSubscribe)
                .set(PriorityProcessing, "1")
                .set(MessageParts.RemoteServices, getAdvertisableSubjects()));
          }

          // We don't want to declare the subscription listeners until after we've sent our initial state
          // to the bus.
          declareSubscriptionListeners();

          setState(BusState.CONNECTED);
          sendAllDeferred();
          InitVotes.voteFor(ClientMessageBus.class);
          logger.info("bus federated and running.");
          break;

        case SessionExpired:
          logger.info("session expired while in state " + getState() + ": attempting to reset ...");

          // try to reconnect
          InitVotes.reset();
          stop(false, new BusTransportError(transportHandler, null,
              new SessionExpiredException(), -1, new RetryInfo(0, 0)));
          init();

          break;

        case Disconnect:
          stop(false);
          if (message.hasPart(MessageParts.Reason)) {
            managementConsole
                .displayError("The bus was disconnected by the server", "Reason: "
                    + message.get(String.class, "Reason"), null);
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
  }
  private final ProtocolCommandProcessor protocolCommandCallback = new ProtocolCommandProcessor();

  private Map<String, TransportHandler> availableHandlers;

  private final TransportHandler BOOTSTRAP_HANDLER
      = HttpPollingHandler.newNoPollingInstance(ClientMessageBusImpl.this);

  /**
   * The current transport handler that's in use. This field is never null; it bottoms out at the No-polling version
   * of HttpPollingHandler.
   */
  private TransportHandler transportHandler = BOOTSTRAP_HANDLER;

  private final Map<String, List<MessageCallback>> subscriptions = new HashMap<>();
  private final Map<String, List<MessageCallback>> localSubscriptions = new HashMap<>();
  private final Map<String, List<MessageCallback>> shadowSubscriptions = new HashMap<>();

  private final Map<String, MessageCallback> remotes = new HashMap<>();

  private final List<TransportErrorHandler> transportErrorHandlers = new ArrayList<>();

  private final List<Runnable> deferredSubscriptions = new ArrayList<>();
  private final List<Message> deferredMessages = new ArrayList<>();

  private final List<BusLifecycleListener> lifecycleListeners = new ArrayList<>();

  private BusState state = BusState.UNINITIALIZED;

  private final ManagementConsole managementConsole;

  private final Map<String, String> properties = new HashMap<>();

  private Timer initialConnectTimer;

  private static final Logger logger = LoggerFactory.getLogger(ClientMessageBus.class);

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
    this.onSubscribeHooks.clear();
    this.onUnsubscribeHooks.clear();
    this.transportHandler = BOOTSTRAP_HANDLER;

    removeRpcResponseSubscriptions();
    setupDefaultHandlers();
  }

  private void removeRpcResponseSubscriptions() {
    final Iterator<String> iter = subscriptions.keySet().iterator();
    while (iter.hasNext()) {
      final String topic = iter.next();
      if (topic.endsWith(":RespondTo:RPC") || topic.endsWith(":Errors:RPC")) {
        iter.remove();
      }
    }
  }

  private void setupDefaultHandlers() {
    if (availableHandlers != null) {
      for (final TransportHandler handler : availableHandlers.values()) {
        handler.close();
      }
    }

    final Map<String, TransportHandler> m = new LinkedHashMap<>();
    m.put(Capabilities.WebSockets.name(), new WebsocketHandler(ClientMessageBusImpl.this));
    m.put(Capabilities.SSE.name(), new SSEHandler(ClientMessageBusImpl.this));
    m.put(Capabilities.LongPolling.name(),
            HttpPollingHandler.newLongPollingInstance(ClientMessageBusImpl.this));
    m.put(Capabilities.ShortPolling.name(),
            HttpPollingHandler.newShortPollingInstance(ClientMessageBusImpl.this));
    availableHandlers = Collections.unmodifiableMap(m);
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
       * This is an optimization to improve unit testing speed. If a test case
       * does not tear down the bus after each test, calling this will ensure
       * that any services dependent on the bus will still be loaded.
       *
       * It's very important that we call waitFor first because InitVotes is
       * reset between most tests. Calling voteFor has not effect without a
       * prior waitFor.
       */
      InitVotes.waitFor(ClientMessageBus.class);
      InitVotes.voteFor(ClientMessageBus.class);
      return;
    }

    logger.info("bus initialization started ...");
    setBusToInitializableState();

    InitVotes.waitFor(ClientMessageBus.class);

    if (isRemoteCommunicationEnabled()) {
      remoteSubscribe(BuiltInServices.ServerEchoService.name());
    }

    if (!isSubscribed(DefaultErrorCallback.CLIENT_ERROR_SUBJECT)) {
      directSubscribe(DefaultErrorCallback.CLIENT_ERROR_SUBJECT, clientBusErrorsCallback, false);
    }

    if (!isSubscribed(BuiltInServices.ClientBus.name())) {
      directSubscribe(BuiltInServices.ClientBus.name(), protocolCommandCallback, false);
    }

    loadRpcProxies();
    // The purpose of this timer is to let the bus yield and give other modules a chance to register
    // services before we send our state synchronization message. This is not strictly necessary
    // but significantly decreases network chattiness since more (if not all known services)
    // can then be listed in the initial handshake message.
    initialConnectTimer = new Timer() {
      @Override
      public void run() {
        logger.debug("Bus initialization timer running...");
        sendInitialMessage();
      }
    };
    initialConnectTimer.schedule(50);
  }

  /**
   * Sends the initial message to connect to the queue, to establish an HTTP
   * session. Otherwise, concurrent requests will result in multiple sessions
   * being created.
   */
  private void sendInitialMessage() {
    if (!isRemoteCommunicationEnabled()) {
      logger.info("initializing client bus in offline mode (erraiBusRemoteCommunicationEnabled was set to false)");
      InitVotes.voteFor(ClientMessageBus.class);
      setState(BusState.LOCAL_ONLY);
      return;
    }

    if (!getState().isStartableState()) {
      logger.warn("aborting startup. bus is not in correct state. (current state: " + getState() + ")");
      return;
    }

    setState(BusState.CONNECTING);

    logger.info("sending handshake message to remote bus");

    for (final Runnable deferredSubscription : deferredSubscriptions) {
      deferredSubscription.run();
    }
    deferredSubscriptions.clear();

    if (!isProperty(ChaosMonkey.DONT_REALLY_CONNECT, "true")) {
      final Map<String, String> properties = new HashMap<>();
      properties.put("phase", "connection");
      properties.put("wait", "1");

      transportHandler.transmit(Collections.singletonList(CommandMessage.create()
          .command(BusCommand.Associate)
          .set(ToSubject, "ServerBus")
          .set(PriorityProcessing, "1")
          .set(MessageParts.RemoteServices, getAdvertisableSubjects())
          .setResource(TransportHandler.EXTRA_URI_PARMS_RESOURCE, properties)));

      transportHandler.start();
    }
    else {
      final String failOnConnectAfterMs = properties.get(ChaosMonkey.FAIL_ON_CONNECT_AFTER_MS);
      if (failOnConnectAfterMs != null) {
        final int ms = Integer.parseInt(failOnConnectAfterMs);

        new Timer() {
          @Override
          public void run() {
            setState(BusState.CONNECTION_INTERRUPTED);
          }
        }.schedule(ms);
      }
    }
  }

  private void processCapabilities(final Message message) {
    for (final String capability : message.get(String.class, MessageParts.CapabilitiesFlags).split(",")) {
      final TransportHandler handler = availableHandlers.get(capability);
      if (handler == null) {
        logger.warn("could not find handler for capability type: " + capability);
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

        encodeAndTransmit(CommandMessage.create()
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
          encodeAndTransmit(CommandMessage.create()
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
    logger.info("stopping bus ...");
    if (initialConnectTimer != null) {
      initialConnectTimer.cancel();
    }

    if (degradeToUnitialized()) {
      setState(BusState.UNINITIALIZED);

      deferredMessages.clear();
      remotes.clear();
      deferredSubscriptions.clear();
    }
    else if (state != BusState.LOCAL_ONLY) {
      setState(BusState.LOCAL_ONLY, reason);
    }

    // Optionally tell the server we're going away (this causes two POST requests)
    if (sendDisconnect && isRemoteCommunicationEnabled()) {
      encodeAndTransmit(CommandMessage.create()
          .toSubject(BuiltInServices.ServerBus.name()).command(BusCommand.Disconnect)
          .set(MessageParts.PriorityProcessing, "1"));
    }

    deferredMessages.addAll(transportHandler.stop(true));
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

  @Override
  public String getClientId() {
    return clientId;
  }

  @Override
  public String getSessionId() {
    return sessionId;
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

  @Override
  public Subscription subscribeShadow(final String subject, final MessageCallback callback) {
    List<MessageCallback> messageCallbacks = shadowSubscriptions.get(subject);
    if (messageCallbacks == null) {
      shadowSubscriptions.put(subject, messageCallbacks = new ArrayList<>());
    }
    messageCallbacks.add(callback);

    final List<MessageCallback> _messageCallbacks = messageCallbacks;
    return new Subscription() {
      @Override
      public void remove() {
        _messageCallbacks.remove(callback);
      }
    };
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
        catch (final Exception e) {
          handleCallbackError(message, e);
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
    logger.debug("send({})", message.getParts());

    try {
      boolean delivered = false;
      final boolean localOnly = message.isFlagSet(RoutingFlag.DeliverLocalOnly);
      final String subject = message.getSubject();

      if (message.hasPart(MessageParts.ToSubject)) {
        if(message.hasPart(MessageParts.ReplyTo)) {
          subscribe((String)message.getParts().get(MessageParts.ReplyTo.name()), this::signalInterceptorsAfter);
        }

        if (isRemoteCommunicationEnabled() && !localOnly) {
          if (getState().isShadowDeliverable() && shadowSubscriptions.containsKey(subject)) {
            deliverToSubscriptions(shadowSubscriptions, subject, message);
            delivered = true;
          }
          else if (getState() != BusState.CONNECTED) {
            logger.debug("deferred: {}", message);
            deferredMessages.add(message);
            delivered = true;
          }
          else if (remotes.containsKey(subject)) {
            logger.debug("sent to remote: {}", message);
            remotes.get(subject).callback(message);
            delivered = true;
          }
        }

        if (subscriptions.containsKey(subject)) {
          deliverToSubscriptions(subscriptions, subject, message);
        }
        else if (localSubscriptions.containsKey(subject)) {
          deliverToSubscriptions(localSubscriptions, subject, message);
        }
        else if (!delivered) {
          if (shadowSubscriptions.containsKey(subject)) {
            deliverToSubscriptions(shadowSubscriptions, subject, message);
          }
          else {
            throw new NoSubscribersToDeliverTo(subject);
          }
        }
      }
      else {
        throw new RuntimeException("Cannot send message using this method"
            + " if the message does not contain a ToSubject field.");
      }
    }
    catch (final RuntimeException e) {
      callErrorHandler(message, e);
    }
  }

  public void signalInterceptorsBefore(Message message) {
    for (MessageInterceptor interceptor : interceptors) {
      interceptor.beforeCall(message);
    }
  }

  public void signalInterceptorsAfter(Message message) {
    for (MessageInterceptor interceptor : interceptors) {
      interceptor.afterCall(message);
    }
  }

  @Override
  public void sendLocal(final Message msg) {
    final String subject = msg.getSubject();
    final List<MessageCallback> messageCallbacks = subscriptions.get(subject);
    if (messageCallbacks != null) {
      // iterating over a copy of the list in case a subscriber unsubscribes during callback
      for (final MessageCallback cb : new ArrayList<>(messageCallbacks)) {
        cb.callback(msg);
      }
    }
  }

  public boolean callErrorHandler(final Message message, final Throwable t) {
    boolean defaultErrorHandling = true;

    if (message.getErrorCallback() != null) {
      defaultErrorHandling = message.getErrorCallback().error(message, t);
    }

    if (defaultErrorHandling) {
      DefaultErrorCallback.INSTANCE.error(message, t);
    }

    return defaultErrorHandling;
  }

  public void encodeAndTransmit(final Message message) {
    logger.debug("encodeAndTransmit({})", message.getParts());
    if (getState() == BusState.LOCAL_ONLY) {
      logger.debug("encodeAndTransmit({}) NOT ROUTED - LOCAL ONLY", message.getParts());
      return;
    }

    transmit(Collections.singletonList(message));
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
   *          subject to look for
   *
   * @return true if the subject is already subscribed
   */
  @Override
  public boolean isSubscribed(final String subject) {
    return subscriptions.containsKey(subject);
  }

  /**
   * Arranges for messages to the given subject to be forwarded to the server.
   *
   * @param subject the bus subject for messages that should be forwarded to the server.
   */
  private void remoteSubscribe(final String subject) {
    remotes.put(subject, serverForwarder);
  }

  Set<String> getRemoteSubscriptions() {
    return remotes.keySet();
  }

  private void sendDeferredToShadow() {
    if (!deferredMessages.isEmpty() && !shadowSubscriptions.isEmpty()) {
      boolean deliveredMessages;
      do {
        deliveredMessages = false;
        for (final Message message : new ArrayList<>(deferredMessages)) {
          if (shadowSubscriptions.containsKey(message.getSubject())) {
            deferredMessages.remove(message);
            deliveredMessages = true;
            deliverToSubscriptions(shadowSubscriptions, message.getSubject(), message);
          }
        }
      }
      while (!deferredMessages.isEmpty() && deliveredMessages);
    }
  }

  private void sendAllDeferred() {
    if (!deferredMessages.isEmpty())
      logger.info("transmitting deferred messages now ...");

    final List<Message> highPriority = new ArrayList<>();
    for (final Message message : new ArrayList<>(deferredMessages)) {
      if (message.hasPart(MessageParts.PriorityProcessing)) {
        if (remotes.containsKey(message.getSubject())) {
          highPriority.add(message);
          deferredMessages.remove(message);
        }
      }
    }

    final List<Message> lowPriority = new ArrayList<>();
    for (final Message message : new ArrayList<>(deferredMessages)) {
      if (remotes.containsKey(message.getSubject())) {
        lowPriority.add(message);
        deferredMessages.remove(message);
      }
    }

    try {
      transmit(highPriority);
      transmit(lowPriority);

      for (final Message message : deferredMessages) {
        String subject = message.getSubject();
        if (!(localSubscriptions.containsKey(subject) ||
             subscriptions.containsKey(subject) ||
             shadowSubscriptions.containsKey(subject))) {
          try {
            throw new NoSubscribersToDeliverTo(subject);
          } catch (NoSubscribersToDeliverTo ex) {
            if (message.getErrorCallback() != null) {
              message.getErrorCallback().error(message, ex);
            }
          }
        }
      }
    } finally {
      deferredMessages.clear();
    }
  }

  private void transmit(List<Message> messages) {
    for (Message message : messages) {
      signalInterceptorsBefore(message);
    }
    transportHandler.transmit(messages);
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
        logger.error("got a transport error while in the " + state + " state");
      }
    }

    return transportError.isStopDefaultErrorHandler();
  }

  private void handleCallbackError(final Message message, final Throwable t) {
    boolean defaultErrorHandling = true;
    if (message.getErrorCallback() != null) {
      try {
        defaultErrorHandling = message.getErrorCallback().error(message, t);
      }
      catch (final Throwable secondaryError) {
        logger.error("Encountered an error while calling error callback for message to " + message.getSubject(), secondaryError);
      }
    }

    if (defaultErrorHandling) {
      DefaultErrorCallback.INSTANCE.error(message, t);
    }
  }

  private void loadRpcProxies() {
    final RpcProxyLoader proxyLoader = ((RpcProxyLoader) GWT.create(RpcProxyLoader.class));
    proxyLoader.loadProxies(ClientMessageBusImpl.this);
  }

  /**
   * Adds a subscription listener, so it is possible to add subscriptions to the
   * client.
   *
   * @param listener
   *          subscription listener
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

  @Override
  public void addInterceptor(MessageInterceptor interceptor) {
    this.interceptors.add(interceptor);
  }

  @Override
  public void removeInterceptor(MessageInterceptor interceptor) {
    this.interceptors.remove(interceptor);
  }

  /**
   * When called, the MessageBus assumes that the currently active transport is no longer capable of operating. The
   * MessageBus then find the best remaining handler and activates it.
   */
  public void reconsiderTransport() {
    TransportHandler newHandler = null;
    for (final TransportHandler handler : availableHandlers.values()) {
      if (handler.isUsable()) {
        newHandler = handler;
        break;
      }
    }

    if (newHandler == null) {
      logger.error("no available transports! stopping bus!");
      stop(false);
    }
    else if (newHandler != transportHandler) {
      logger.info("transitioning to new handler: " + newHandler);

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

  @Override
  public void removeTransportErrorHandler(final TransportErrorHandler errorHandler) {
    transportErrorHandlers.remove(errorHandler);
  }

  public BusState getState() {
    return state;
  }

  public Set<String> getRemoteServices() {
    return new HashSet<>(remotes.keySet());
  }

  public Set<String> getLocalServices() {
    return new HashSet<>(subscriptions.keySet());
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

  public TransportHandler getTransportHandler() {
    return transportHandler;
  }

  public Collection<TransportHandler> getAllAvailableHandlers() {
    return availableHandlers.values();
  }


  @Override
  public void setProperty(final String name, final String value) {
    properties.put(name, value);
  }

  @Override
  public void clearProperties() {
    properties.clear();
  }

  private boolean isProperty(final String name, final String value) {
    return properties.containsKey(name) && properties.get(name).equals(value);
  }

  private boolean degradeToUnitialized() {
    return isProperty(ChaosMonkey.DEGRADE_TO_UNINITIALIZED_ON_STOP, "true");
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
      GWT.log("bus tried to transition to " + state + ", but it already is");
      return;
    }

    final List<BusEventType> events = new ArrayList<>();

    switch (state) {
      case UNINITIALIZED:
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
        if (newState == BusState.CONNECTED) {
          logger.info("the connection has resumed.");
        }

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
      logger.warn("the connection to the server has been interrupted ...");
    }

    /*
     * If the new state is a state we deliver to shadow subscriptions, we send any deferred messages to
     * the shadow subscriptions now.
     */
    if (newState.isShadowDeliverable()) {
      sendDeferredToShadow();
    }

    for (final BusEventType et : events) {
      final BusLifecycleEvent e = new BusLifecycleEvent(this, reason);
      for (int i = lifecycleListeners.size() - 1; i >= 0; i--) {
        try {
          et.deliverTo(lifecycleListeners.get(i), e);
        }
        catch (final Throwable t) {
          logger.error("listener threw exception: " + t);
          t.printStackTrace();
        }
      }
    }
  }
}
