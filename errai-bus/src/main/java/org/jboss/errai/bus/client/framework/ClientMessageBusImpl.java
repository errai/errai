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

import static org.jboss.errai.bus.client.json.JSONUtilCli.decodePayload;
import static org.jboss.errai.bus.client.protocols.BusCommands.RemoteSubscribe;
import static org.jboss.errai.bus.client.protocols.BusCommands.RemoteUnsubscribe;
import static org.jboss.errai.common.client.protocols.MessageParts.PriorityProcessing;
import static org.jboss.errai.common.client.protocols.MessageParts.Subject;
import static org.jboss.errai.common.client.protocols.MessageParts.ToSubject;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.VerticalPanel;
import org.jboss.errai.bus.client.ErraiBus;
import org.jboss.errai.bus.client.api.BusLifecycleEvent;
import org.jboss.errai.bus.client.api.BusLifecycleListener;
import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.api.MessageCallback;
import org.jboss.errai.bus.client.api.MessageListener;
import org.jboss.errai.bus.client.api.PreInitializationListener;
import org.jboss.errai.bus.client.api.SubscribeListener;
import org.jboss.errai.bus.client.api.UnsubscribeListener;
import org.jboss.errai.bus.client.api.base.Capabilities;
import org.jboss.errai.bus.client.api.base.CommandMessage;
import org.jboss.errai.bus.client.api.base.DefaultErrorCallback;
import org.jboss.errai.bus.client.api.base.NoSubscribersToDeliverTo;
import org.jboss.errai.bus.client.framework.transports.BusTransportError;
import org.jboss.errai.bus.client.framework.transports.HttpPollingHandler;
import org.jboss.errai.bus.client.framework.transports.TransportHandler;
import org.jboss.errai.bus.client.framework.transports.WebsocketHandler;
import org.jboss.errai.bus.client.json.JSONUtilCli;
import org.jboss.errai.bus.client.protocols.BusCommands;
import org.jboss.errai.bus.client.util.BusTools;
import org.jboss.errai.common.client.api.Assert;
import org.jboss.errai.common.client.api.ResourceProvider;
import org.jboss.errai.common.client.api.extension.InitVotes;
import org.jboss.errai.common.client.protocols.MessageParts;
import org.jboss.errai.common.client.util.LogUtil;
import org.jboss.errai.marshalling.client.api.MarshallerFramework;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

/**
 * The default client <tt>MessageBus</tt> implementation.  This bus runs in the browser and automatically federates
 * with the server immediately upon initialization.
 *
 * @author Mike Brock
 */
public class ClientMessageBusImpl implements ClientMessageBus {

  private enum State {LOCAL_ONLY, CONNECTING, CONNECTED}
    /* The encoded URL to be used for the bus */
  String OUT_SERVICE_ENTRY_POINT;
  String IN_SERVICE_ENTRY_POINT;


  private final String clientId;
  private String sessionId;


  /* ArrayList of all subscription listeners */
  private final List<SubscribeListener> onSubscribeHooks
      = new ArrayList<SubscribeListener>();

  /* ArrayList of all un-subscription listeners */
  private final List<UnsubscribeListener> onUnsubscribeHooks
      = new ArrayList<UnsubscribeListener>();

  public final MessageCallback remoteCallback = new RemoteMessageCallback();

  private final MessageCallback transportToBusCallback = new MessageCallback() {
    @Override
    public void callback(Message message) {
      if (state != State.CONNECTED)
          setState(State.CONNECTED);

      processMessage(message.getSubject(), message);
    }
  };

  private final Map<String, TransportHandler> availableHandlers = Collections.unmodifiableMap(new LinkedHashMap<String, TransportHandler>() {
    {
      put(Capabilities.WebSockets.name(), new WebsocketHandler(transportToBusCallback, ClientMessageBusImpl.this));
      put(Capabilities.LongPolling.name(), HttpPollingHandler.newLongPollingInstance(transportToBusCallback, ClientMessageBusImpl.this));
      put(Capabilities.ShortPolling.name(), HttpPollingHandler.newShortPollingInstance(transportToBusCallback, ClientMessageBusImpl.this));
    }
  });

  private final TransportHandler BOOTSTRAP_HANDLER = HttpPollingHandler.newNoPollingInstance(transportToBusCallback, ClientMessageBusImpl.this);;
  /**
   * The current transport handler that's in use. This field is never null; it bottoms out at the No-polling version of HttpPollingHandler.
   */
  private TransportHandler transportHandler = BOOTSTRAP_HANDLER;

  private final Map<String, List<MessageCallback>> subscriptions = new HashMap<String, List<MessageCallback>>();
  private final Map<String, List<MessageCallback>> localSubscriptions = new HashMap<String, List<MessageCallback>>();
  private final Map<String, MessageCallback> remotes = new HashMap<String, MessageCallback>();

  private final List<PreInitializationListener> preInitializationListeners = new ArrayList<PreInitializationListener>();
  private final List<TransportErrorHandler> transportErrorHandlers = new ArrayList<TransportErrorHandler>();

  /*
   * A list of {@link Runnable} initialization tasks to be executed after the
   * bus has successfully finished it's initialization and is now communicating
   * with the remote bus.
   */
  private final List<Runnable> deferredSubscriptions = new ArrayList<Runnable>();
  private final List<Runnable> postInitTasks = new ArrayList<Runnable>();
  private final List<Message> deferredMessages = new ArrayList<Message>();
  private final Queue<Message> toSendBuffer = new LinkedList<Message>();


  /* True if the client's message bus has been initialized */
  private boolean initialized = false;
  private boolean disconnected = false;

  private State state = State.LOCAL_ONLY;

  private BusErrorDialog errorDialog;


  static {
    MarshallerFramework.initializeDefaultSessionProvider();
  }

  public ClientMessageBusImpl() {
    clientId = String.valueOf(com.google.gwt.user.client.Random.nextInt(99999)) + "-"
        + (System.currentTimeMillis() % (com.google.gwt.user.client.Random.nextInt(99999) + 1));


    IN_SERVICE_ENTRY_POINT = "in." + getClientId() + ".erraiBus";
    OUT_SERVICE_ENTRY_POINT = "out." + getClientId() + ".erraiBus";

    // when the window is closing, we want to stop the bus without causing any
    // errors (unless the server is unavailable of course) (see ERRAI-225)
    Window.addCloseHandler(new CloseHandler<Window>() {
      @Override
      public void onClose(final CloseEvent<Window> event) {
        if (state != State.LOCAL_ONLY) {
          stop(true);
        }
      }
    });

    init();
  }

  private long lastSleepTick = System.currentTimeMillis();
  private boolean lastSleepCheckWasOnline = false;

  private void startSleepDetector() {
    new Timer() {
      @Override
      public void run() {
        final long currentTime = System.currentTimeMillis();

        if (lastSleepTick - currentTime > 30000) {
          LogUtil.log("we might be asleep.");

          if (state == State.LOCAL_ONLY && lastSleepCheckWasOnline) {
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
        lastSleepCheckWasOnline = (state == State.CONNECTED);
      }
    }.scheduleRepeating(1000);
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
      public void callback(Message message) {
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
    if (!isInitialized()) {
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
          cbs.remove(wrappedCallbackHolder.wrappedCallback);
          if (cbs.isEmpty()) {
            unsubscribeAll(subject);
          }
        }
      }
    };
  }

  final static class WrappedCallbackHolder {
    private MessageCallback wrappedCallback;

    WrappedCallbackHolder(final MessageCallback wrappedCallback) {
      this.wrappedCallback = wrappedCallback;
    }
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
          displayError("receiver '" + subject + "' threw an exception", decodeCommandMessage(message), e);
        }
      }
    };

    callbackHolder.wrappedCallback = cb;

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

  private static final ResourceProvider<RequestDispatcher> dispatcherProvider = new ResourceProvider<RequestDispatcher>() {
    @Override
    public RequestDispatcher get() {
      return ErraiBus.getDispatcher();
    }
  };

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
    message.setResource(RequestDispatcher.class.getName(), dispatcherProvider)
        .setResource("Session", JSONUtilCli.getClientSession()).commit();

    try {
      if (message.hasPart(MessageParts.ToSubject)) {
        if (!initialized) {
          deferredMessages.add(message);
        }
        else {
          if (!hasListeners(message.getSubject())) {
            throw new NoSubscribersToDeliverTo(message.getSubject());
          }

          directStore(message);
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

  private boolean hasListeners(final String subject) {
    return subscriptions.containsKey(subject)
        || remotes.containsKey(subject)
        || localSubscriptions.containsKey(subject);
  }

  public void callErrorHandler(final Message message, final Throwable t) {
    if (message.getErrorCallback() != null) {
      message.getErrorCallback().error(message, t);
    }
    displayError(t.getMessage(), "none", t);
  }

  /**
   * Delivers the message to either the remote, "subscriptions", or local message callback for the message's subject.
   *
   * @param message
   *     the message to deliver. Not null.
   */
  private void directStore(final Message message) {
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
    else if (!routedToRemote) {
      throw new NoSubscribersToDeliverTo(subject);
    }
  }


  /**
   * Add message to the queue that remotely transmits messages to the server.
   * All messages in the queue are then sent.
   *
   * @param message
   *     -
   */
  public void encodeAndTransmit(final Message message) {
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

  // TODO delete this method
  private void resubscribeShadowSubscriptions() {
    for (final Map.Entry<String, List<MessageCallback>> entry : subscriptions.entrySet()) {
      for (final MessageCallback callback : entry.getValue()) {
        _subscribe(entry.getKey(), callback, false);
      }
    }
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

  @Override
  public void stop(final boolean sendDisconnect) {
    stop(sendDisconnect, null);
  }

  private void stop(final boolean sendDisconnect, final TransportError reason) {
    LogUtil.log("stopping bus ...");
    // Ensure the polling callback does not reawaken the bus.
    // It could be sleeping now and about to start another poll request.

    // Optionally tell the server we're going away (this causes two POST requests)
    try {
      if (sendDisconnect && isRemoteCommunicationEnabled()) {
        encodeAndTransmit(CommandMessage.createWithParts(new HashMap<String, Object>())
            .toSubject(BuiltInServices.ServerBus.name()).command(BusCommands.Disconnect)
            .set(MessageParts.PriorityProcessing, "1"));
        unsubscribeAll(BuiltInServices.ClientBus.name());
      }

      subscriptions.clear();
    }
    finally {
      this.toSendBuffer.clear();
      this.remotes.clear();
      this.disconnected = true;
      this.initialized = false;
      this.deferredSubscriptions.clear();
      this.postInitTasks.clear();
      this.transportHandler = BOOTSTRAP_HANDLER;

      InitVotes.reset();

      if (state != State.LOCAL_ONLY)
        setState(State.LOCAL_ONLY, reason);
    }
  }

  public class RemoteMessageCallback implements MessageCallback {
    @Override
    public void callback(final Message message) {
      encodeAndTransmit(message);
    }
  }

  /**
   * Evil backdoor for enabling local communication when the bus is offline.
   * This method will be replaced with a better-behaved API in Errai 3.0.
   * <p/>
   * <h2>Instructions for using this method</h2>
   * <p/>
   * When the bus has gone into LOCAL_ONLY state (after a disassociating event),
   * by default it defers all message delivery <i>including local messages</i>.
   * By calling <tt>setInitialized(true)</tt> you tell the bus to attempt
   * immedaite delivery of all messages. This will allow local messages to get
   * through, but it will cause messages that would normally be enqueued for
   * remote delivery when the bus comes back online to throw a
   * {@link NoSubscribersToDeliverTo} exception when sent.
   * <p/>
   * If you want to take the bus back online after calling
   * <tt>setInitialized(true)</tt>, you must do so by calling
   * <code>bus.stop(false)</code> followed by <code>bus.init()</code>.
   */
  public void setInitialized(final boolean initialized) {
    this.initialized = initialized;
  }

  private void registerInitVoteCallbacks() {
    InitVotes.waitFor(ClientMessageBus.class);
    InitVotes.registerOneTimeInitCallback(new Runnable() {
      @Override
      public void run() {
        completeInit();
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
   * @see #isRemoteCommunicationEnabled()
   * @see BusLifecycleListener
   */
  @Override
  public void init() {
    directSubscribe(DefaultErrorCallback.CLIENT_ERROR_SUBJECT, new MessageCallback() {
      @Override
      public void callback(final Message message) {
        final String errorTo = message.get(String.class, MessageParts.ErrorTo);
        if (errorTo == null) {
          displayError(message.get(String.class, MessageParts.ErrorMessage),
              message.get(String.class, MessageParts.AdditionalDetails), null);
        }
        else {
          message.toSubject(errorTo);
          message.sendNowWith(ClientMessageBusImpl.this);
        }
      }
    }, false);

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

    /**
     * ... also send RemoteUnsubscribe signals.
     */
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


    // if the bus is already initialized or is currently initializing
    if (state != State.LOCAL_ONLY) return;

    declareDebugFunction();
    registerInitVoteCallbacks();

    resubscribeShadowSubscriptions();

    /**
     * Fire initialization listeners now.
     */
    for (final PreInitializationListener listener : preInitializationListeners) {
      listener.beforeInitialization();
    }

    if (isRemoteCommunicationEnabled()) {
      remoteSubscribe(BuiltInServices.ServerEchoService.name());
    }

    directSubscribe(BuiltInServices.ClientBus.name(), new MessageCallback() {
      @Override
      @SuppressWarnings({"unchecked"})
      public void callback(final Message message) {
        switch (BusCommands.valueOf(message.getCommandType())) {
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

          case RemoteMonitorAttach:
            break;

          case FinishAssociation:
            if (isInitialized()) {
              return;
            }

            initializeMessagingBus();

            new Timer() {
              @Override
              public void run() {
                LogUtil.log("received FinishStateSync message. preparing to bring up the federation");


                sessionId = message.get(String.class, MessageParts.ConnectionSessionKey);

                remoteSubscribe(BuiltInServices.ServerBus.name());

                final boolean hasDeferred = !deferredSubscriptions.isEmpty();
                for (final Runnable deferredSubscription : deferredSubscriptions) {
                  deferredSubscription.run();
                }
                deferredSubscriptions.clear();

                processCapabilities(message);

                for (final String svc : message.get(String.class, MessageParts.RemoteServices).split(",")) {
                  remoteSubscribe(svc);
                }

                if (hasDeferred) {
                  encodeAndTransmit(CommandMessage.createWithParts(new HashMap<String, Object>())
                      .toSubject(BuiltInServices.ServerBus.name()).command(BusCommands.RemoteSubscribe)
                      .set(PriorityProcessing, "1")
                      .set(MessageParts.RemoteServices, getAdvertisableSubjects()));
                }

                setState(State.CONNECTED);
                startSleepDetector();
                // end of FinishStateSync Timer

                InitVotes.voteFor(ClientMessageBus.class);

                reconsiderTransport();
              }
            }.schedule(5);
            break;

          case SessionExpired:
              if (!isInitialized())
                return;

              LogUtil.log("http session has expired. resetting bus and attempting reconnection.");

              stop(false);
              init();
            break;


          case Disconnect:
            stop(false);

            if (message.hasPart("Reason")) {
              displayError("The bus was disconnected by the server", "Reason: " + message.get(String.class, "Reason"), null);
            }
            break;
          case Heartbeat:
            break;
          case Resend:
            break;
          case RemoteMonitorDetach:
            break;
        }
      }
    }, false);

    /**
     * Send initial message to connect to the queue, to establish an HTTP
     * session. Otherwise, concurrent requests will result in multiple sessions
     * being created. Which is bad. Avoid this at all costs. Please.
     */
    if (!sendInitialMessage()) {
      displayError("Could not connect to remote bus", "", null);
    }
  }

  private void processCapabilities(final Message message) {
    for (final String capability : message.get(String.class, MessageParts.CapabilitiesFlags).split(",")) {
      final TransportHandler handler = availableHandlers.get(capability);
      if (handler == null) {
        LogUtil.log("warning: could not find handler for capability type: " + capability);
        continue;
      }

//      switch (Capabilities.valueOf(capability)) {
//        case SSE:
//          sseAvailable = true;
//          break;
//        case LongPolling:
//          LogUtil.log("initializing long poll subsystem");
//          receiveCommCallback = new LongPollRequestCallback();
//          break;
//        case ShortPolling:
//          receiveCommCallback = new ShortPollRequestCallback();
//          if (message.hasPart(MessageParts.PollFrequency)) {
//            POLL_FREQUENCY = message.get(Integer.class, MessageParts.PollFrequency);
//          }
//          else {
//            POLL_FREQUENCY = 500;
//          }
//          break;
//
//      }
    }
  }

//  private void activateSSE() {
//    try {
//      LogUtil.log("opening SSE channel ...");
//
//      final Object o = ClientSSEChannel.attemptSSEChannel(ClientMessageBusImpl.this,
//          URL.encode(getApplicationLocation(IN_SERVICE_ENTRY_POINT))
//              + "?z=" + getNextRequestNumber() + "&sse=1&clientId=" + clientId);
//
//      if (o instanceof String) {
//        LogUtil.log("sse could not be negotiated. reason: " + o);
//        new Timer() {
//          @Override
//          public void run() {
//            performPoll();
//          }
//        }.schedule(10);
//      }
//      else {
//        LogUtil.log("sse channel active.");
//        setState(State.CONNECTED);
//        pendingRequests.clear();
//      }
//    }
//    catch (Throwable t) {
//      t.printStackTrace();
//    }
//  }
//
//  protected void reconnectSSE() {
//    LogUtil.log("reconnectSSE() called");
//    if (state == State.CONNECTING) {
//      return;
//    }
//
//    setState(State.CONNECTING);
//    try {
//      new Timer() {
//        @Override
//        public void run() {
//          retries++;
//          final int retryDelay = retries * 1000;
//          final RetryInfo retryInfo = new RetryInfo(retryDelay, retries);
//          final BusTransportError transportError = new BusTransportError(null, null, -1, retryInfo);
//
//          if (handleTransportError(transportError)) {
//            return;
//          }
//
//          new Timer() {
//            @Override
//            public void run() {
//              activateSSE();
//            }
//          }.schedule(retryDelay);
//        }
//      }.schedule(1);
//
//    }
//    catch (Throwable t) {
//      t.printStackTrace();
//    }
//  }


  private void websocketUpgrade() {

  }

  @SuppressWarnings("ConstantConditions")
  private void completeInit() {
    if (!isInitialized()) {
      LogUtil.log("received final vote for initialization ...");

      if (!postInitTasks.isEmpty())
        LogUtil.log("executing " + postInitTasks.size() + " post init task(s)");

      do {
        for (final Runnable runnable : new ArrayList<Runnable>(postInitTasks)) {
          postInitTasks.remove(runnable);
          try {
            runnable.run();
          }
          catch (Throwable t) {
            LogUtil.log("[error] running post init task: " + t);
            LogUtil.log("     -> " + t.getMessage());
          }
        }
      }
      while (!postInitTasks.isEmpty());

      setInitialized(true);

      sendAllDeferred();
      postInitTasks.clear();

      LogUtil.log("bus federation complete. now operating normally.");
    }
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

    for (final Message message : new ArrayList<Message>(deferredMessages)) {
      if (message.hasPart(MessageParts.PriorityProcessing)) {
        transmitDeferred(message);
        deferredMessages.remove(message);
      }
    }

    do {
      // defensively copy and don't use a fail-fast iterator -- these tasks may
      // send messages
      for (final Message message : new ArrayList<Message>(deferredMessages)) {
        transmitDeferred(message);
        deferredMessages.remove(message);
      }
    }
    while (!deferredMessages.isEmpty());
  }

  private void transmitDeferred(final Message message) {
    try {
      directStore(message);
    }
    catch (Throwable t) {
      LogUtil.log("[error] failed to transmit deferred message: " + message);
      LogUtil.log("    -> " + t.getMessage());
    }
  }

  public boolean handleTransportError(final BusTransportError transportError) {
    for (final TransportErrorHandler handler : transportErrorHandlers) {
      handler.onError(transportError);
    }

    if (!transportError.isStopDefaultErrorHandler()) {
      if (state == State.CONNECTED) {
        setState(State.CONNECTING, transportError);
      }
      else if (state != State.CONNECTING) {
        LogUtil.log("got a transport error while in the " + state + " state");
      }
    }

    return transportError.isStopDefaultErrorHandler();
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

  /**
   * Sends the initial message to connect to the queue, to establish an HTTP
   * session. Otherwise, concurrent requests will result in multiple sessions
   * being created.
   *
   * @return true if initial message was sent successfully.
   */
  private boolean sendInitialMessage() {
    if (!isRemoteCommunicationEnabled()) {
      LogUtil.log("initializing client bus in offline mode (erraiBusRemoteCommunicationEnabled was set to false)");
      InitVotes.voteFor(ClientMessageBus.class);
      return true;
    }

    setState(State.CONNECTING);

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

      ((HttpPollingHandler) transportHandler).sendOutboundRequest(BusTools.encodeMessage(initialMessage),
          connectHeader, new RequestCallback() {
        @Override
        public void onResponseReceived(final Request request, final Response response) {
          try {
            LogUtil.log("received response from initial handshake.");
            handleJsonMessage(response.getText());
          }
          catch (Exception e) {
            e.printStackTrace();
            displayError("Error attaching to bus",
                e.getMessage() + "<br/>Message Contents:<br/>" + response.getText(), e);
          }
        }

        @Override
        public void onError(final Request request, final Throwable exception) {
          displayError("Could not connect to remote bus", "", exception);
        }
      });
    }
    catch (RequestException e) {
      e.printStackTrace();
      return false;
    }
    return true;
  }

  /**
   * Returns true if client message bus is initialized.
   *
   * @return true if client message bus is initialized.
   */
  @Override
  public boolean isInitialized() {
    return this.initialized;
  }

  public String getClientId() {
    return clientId;
  }

  public String getSessionId() {
    return sessionId;
  }


  /**
   * Initializes the message bus by setting up the <tt>recvBuilder</tt> to
   * accept responses. Also, initializes the incoming timer to ensure the
   * client's polling with the server is active.
   */
  private void initializeMessagingBus() {
    if (disconnected) {
      return;
    }

    try {
      ((RpcProxyLoader) GWT.create(RpcProxyLoader.class)).loadProxies(ClientMessageBusImpl.this);

      InitVotes.voteFor(RpcProxyLoader.class);
    }
    catch (Throwable t) {
      t.printStackTrace();
    }
  }

  /**
   * Add runnable tasks to be run after the message bus is initialized.
   *
   * @param run
   *     a {@link Runnable} task.
   */
  @Override
  public void addPostInitTask(final Runnable run) {
    if (isInitialized()) {
      run.run();
      return;
    }
    postInitTasks.add(run);
  }

  @Override
  public void addPreInitializationListener(final PreInitializationListener listener) {
    preInitializationListeners.add(Assert.notNull(listener));
  }

  /**
   * Do-nothing function, should eventually be able to add a global listener to
   * receive all messages. Though global message dispatches the message to all
   * listeners attached.
   *
   * @param listener
   *     - listener to accept all messages dispatched
   */
  @Override
  public void addGlobalListener(final MessageListener listener) {
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

  public void displayError(final String message, final String additionalDetails, final Throwable e) {
    showError(message + " -- Additional Details: " + additionalDetails, e);
    errorDialog.show();
  }

  private void ensureInitErrorDialog() {
    if (errorDialog == null) {
      errorDialog = new BusErrorDialog();
    }
  }

  private void showError(final String message, final Throwable e) {
    ensureInitErrorDialog();
    errorDialog.addError(message, "", e);

    if (LogUtil.isNativeJavaScriptLoggerSupported()) {
      LogUtil.nativeLog(message);
    }
  }

  /**
   * Parses the given JSON message and reacts to its contents.
   *
   * @param text
   *     the JSON message payload.
   */
  @Override
  public void handleJsonMessage(final String text) {
    try {
      for (final MarshalledMessage m : decodePayload(text)) {
        processMessage(m.getSubject(), JSONUtilCli.decodeCommandMessage(m.getMessage()));
      }
    }
    catch (Throwable t) {
      t.printStackTrace();
      displayError("Error delivering message into bus", text, t);
      System.out.println("Rejected message <<" + text + ">>");
    }
  }

  /**
   * When called, the MessageBus assumes that the currently active transport is no longer capable of operating. The
   * MessageBus then find the best remaining handler and activates it.
   */
  @Override
  public void reconsiderTransport() {
    TransportHandler newHandler = null;
    for (TransportHandler handler : availableHandlers.values()) {
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

      LogUtil.log("transitioning to new handler: " + newHandler.getClass().getName());

      transportHandler.stop();
      transportHandler = newHandler;
      transportHandler.start();
    }
    // 3rd case: we're already using the best available handler. Do nothing.
  }

  @Override
  public void attachMonitor(final BusMonitor monitor) {
  }

  @Override
  public Set<String> getAllRegisteredSubjects() {
    return Collections.unmodifiableSet(subscriptions.keySet());
  }

  @Override
  public void addTransportErrorHandler(final TransportErrorHandler errorHandler) {
    transportErrorHandlers.add(errorHandler);
  }

  public State getState() {
    return state;
  }

  public void processMessage(final String subject, final Message msg) {
    if (subscriptions.containsKey(subject)) {
      final ArrayList<MessageCallback> messageCallbacks = new ArrayList<MessageCallback>(subscriptions.get(subject));
      for (final MessageCallback cb : messageCallbacks) {
        cb.callback(msg);
      }
    }
  }



  /**
   * The built-in, default error dialog.
   */
  class BusErrorDialog extends DialogBox {
    boolean showErrors = !GWT.isProdMode();
    Panel contentPanel = new AbsolutePanel();

    public BusErrorDialog() {
      setModal(false);

      final VerticalPanel panel = new VerticalPanel();

      final HorizontalPanel titleBar = new HorizontalPanel();
      titleBar.getElement().getStyle().setProperty("backgroundColor", "#A9A9A9");
      titleBar.getElement().getStyle().setWidth(100, Style.Unit.PCT);
      titleBar.getElement().getStyle().setProperty("borderBottom", "1px solid black");
      titleBar.getElement().getStyle().setProperty("marginBottom", "5px");

      final Label titleBarLabel = new Label("An Error Occurred in the Bus");
      titleBarLabel.getElement().getStyle().setFontSize(10, Style.Unit.PT);
      titleBarLabel.getElement().getStyle().setFontWeight(Style.FontWeight.BOLDER);
      titleBarLabel.getElement().getStyle().setColor("white");

      titleBar.add(titleBarLabel);
      titleBar.setCellVerticalAlignment(titleBarLabel, HasVerticalAlignment.ALIGN_MIDDLE);

      final HorizontalPanel buttonPanel = new HorizontalPanel();

      final CheckBox showFurtherErrors = new CheckBox();
      showFurtherErrors.setValue(showErrors);
      showFurtherErrors.setText("Show further errors");
      showFurtherErrors.getElement().getStyle().setFontSize(10, Style.Unit.PT);
      showFurtherErrors.getElement().getStyle().setColor("white");

      showFurtherErrors.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
        @Override
        public void onValueChange(final ValueChangeEvent<Boolean> booleanValueChangeEvent) {
          showErrors = booleanValueChangeEvent.getValue();
        }
      });

      final Button disconnectFromServer = new Button("Disconnect Bus");
      disconnectFromServer.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(final ClickEvent event) {
          if (Window
              .confirm("Are you sure you want to disconnect and de-federate the local bus from the server bus? "
                  + "This will permanently kill your session. You will need to refresh to reconnect. OK will proceed. Click "
                  + "Cancel to abort this operation")) {
            stop(true);
          }
        }
      });

      final Button clearErrors = new Button("Clear Log");
      clearErrors.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(final ClickEvent event) {
          contentPanel.clear();
        }
      });

      final Button closeButton = new Button("Dismiss Error");
      closeButton.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(final ClickEvent event) {
          errorDialog.hide();
        }
      });

      buttonPanel.add(showFurtherErrors);
      buttonPanel.add(disconnectFromServer);
      buttonPanel.add(clearErrors);
      buttonPanel.add(closeButton);

      buttonPanel.setCellVerticalAlignment(showFurtherErrors, HasVerticalAlignment.ALIGN_MIDDLE);

      titleBar.add(buttonPanel);
      titleBar.setCellHorizontalAlignment(buttonPanel, HasHorizontalAlignment.ALIGN_RIGHT);

      panel.add(titleBar);

      final Style s = panel.getElement().getStyle();

      s.setProperty("border", "1px");
      s.setProperty("borderStyle", "solid");
      s.setProperty("borderColor", "black");
      s.setProperty("backgroundColor", "#ede0c3");

      resize();

      panel.add(contentPanel);
      add(panel);

      getElement().getStyle().setZIndex(16777271);
    }

    public void addError(final String message, final String additionalDetails, final Throwable e) {
      if (!showErrors)
        return;

      contentPanel.add(new HTML("<strong style='background:red;color:white;'>" + message + "</strong>"));

      final StringBuilder buildTrace = new StringBuilder("<tt style=\"font-size:11px;\"><pre>");
      if (e != null) {
        e.printStackTrace();
        buildTrace.append(e.getClass().getName()).append(": ").append(e.getMessage()).append("<br/>");
        for (final StackTraceElement ste : e.getStackTrace()) {
          buildTrace.append("  ").append(ste.toString()).append("<br/>");
        }
      }
      buildTrace.append("</pre>");

      contentPanel.add(new HTML(buildTrace.toString() + "<br/><strong>Additional Details:</strong>" + additionalDetails
          + "</tt>"));

      if (!isShowing()) {
        resize();
        show();
        center();
        setModal(true);
      }
    }

    private void resize() {
      contentPanel.setWidth(Window.getClientWidth() * 0.90 + "px");
      contentPanel.setHeight(Window.getClientHeight() * 0.90 + "px");
      contentPanel.getElement().getStyle().setProperty("overflow", "auto");
    }
  }

  private static native void declareDebugFunction() /*-{
      $wnd.errai_status = function () {
          @org.jboss.errai.bus.client.framework.ClientMessageBusImpl::_displayStatusToLog()();
      };

      $wnd.errai_list_services = function () {
          @org.jboss.errai.bus.client.framework.ClientMessageBusImpl::_listAvailableServicesToLog()();
      };

      $wnd.errai_show_error_console = function () {
          @org.jboss.errai.bus.client.framework.ClientMessageBusImpl::_showErrorConsole()();
      }
  }-*/;

  /**
   * Debugging functions.
   */
  private static void _displayStatusToLog() {

    LogUtil.displayDebuggerUtilityTitle("ErraiBus Status");

    final ClientMessageBusImpl bus = (ClientMessageBusImpl) ErraiBus.get();
    LogUtil.nativeLog("Bus State              : " + (bus.initialized ? "Online/Federated" : "Disconnected"));
    LogUtil.nativeLog("");
//    LogUtil.nativeLog("Comet Channel          : " + (bus.cometChannelOpen ? "Active" : "Offline"));
//    LogUtil.nativeLog("  Endpoint (RX)        : " + getApplicationRoot() + bus.IN_SERVICE_ENTRY_POINT);
//    LogUtil.nativeLog("  Endpoint (TX)        : " + getApplicationRoot() + bus.OUT_SERVICE_ENTRY_POINT);
//    LogUtil.nativeLog("  Pending Requests     : " + bus.pendingRequests.size());
//    LogUtil.nativeLog("");
//    LogUtil.nativeLog("WebSocket Channel      : " + (bus.webSocketOpen ? "Active" : "Offline"));
//    LogUtil.nativeLog("  Endpoint (RX/TX)     : " + bus.webSocketUrl);
//    LogUtil.nativeLog("");
//    LogUtil.nativeLog("Total TXs              : " + bus.txNumber);
//    LogUtil.nativeLog("Total RXs              : " + bus.rxNumber);
    LogUtil.nativeLog("");
    LogUtil.nativeLog("Endpoints");
    LogUtil.nativeLog("  Remote (total)       : " + bus.remotes.size());
    LogUtil.nativeLog("  Local (total)        : " + bus.subscriptions.size());

    LogUtil.displaySeparator();
  }

  private static void _listAvailableServicesToLog() {

    LogUtil.displayDebuggerUtilityTitle("Service and Routing Table");
    LogUtil.nativeLog("[REMOTES]");

    for (final String remoteName : ((ClientMessageBusImpl) ErraiBus.get()).remotes.keySet()) {
      LogUtil.nativeLog(remoteName);
    }

    LogUtil.nativeLog("[LOCALS]");

    for (final String localName : ((ClientMessageBusImpl) ErraiBus.get()).subscriptions.keySet()) {
      LogUtil.nativeLog(localName + " (" + ((ClientMessageBusImpl) ErraiBus.get()).subscriptions.get(localName).size()
          + ")");
    }

    LogUtil.displaySeparator();
  }

  private static void _showErrorConsole() {
    ((ClientMessageBusImpl) ErraiBus.get()).ensureInitErrorDialog();
    ((ClientMessageBusImpl) ErraiBus.get()).errorDialog.show();
  }

  /**
   * Checks whether remote bus communication is enabled.
   * <p/>
   * The JavaScript variable <code>erraiBusRemoteCommunicationEnabled</code> can
   * be used to control this value. If the variable is not present in the window
   * object, the default value <code>true</code> is returned.
   *
   * @return true if remote communication enabled, otherwise false.
   */
  public native boolean isRemoteCommunicationEnabled() /*-{
      //noinspection JSUnresolvedVariable
      if ($wnd.erraiBusRemoteCommunicationEnabled === undefined || $wnd.erraiBusRemoteCommunicationEnabled.length === 0) {
          return true;
      }
      else {
          //noinspection JSUnresolvedVariable
          return $wnd.erraiBusRemoteCommunicationEnabled;
      }
  }-*/;

  /**
   * Sets the application root for the remote message bus endpoints.
   *
   * @param path
   *     path to use when sending requests to the JAX-RS endpoint
   */
  @SuppressWarnings("UnusedDeclaration")
  public static native void setApplicationRoot(final String path) /*-{
      if (path == null) {
          $wnd.erraiBusApplicationRoot = undefined;
      }
      else {
          $wnd.erraiBusApplicationRoot = path;
      }
  }-*/;

  public static String getApplicationLocation(final String serviceEntryPoint) {
    final Configuration configuration = GWT.create(Configuration.class);
    if (configuration instanceof Configuration.NotSpecified) {
      return getApplicationRoot() + serviceEntryPoint;
    }
    return configuration.getRemoteLocation() + serviceEntryPoint;
  }

  /**
   * Returns the application root for the remote message bus endpoints.
   *
   * @return path with trailing slash, or empty string if undefined or
   *         explicitly set to empty
   */
  public static native String getApplicationRoot() /*-{
      //noinspection JSUnresolvedVariable
      if ($wnd.erraiBusApplicationRoot === undefined || $wnd.erraiBusApplicationRoot.length === 0) {
          return "";
      }
      else {
          //noinspection JSUnresolvedVariable
          if ($wnd.erraiBusApplicationRoot.substr(-1) !== "/") {
              //noinspection JSUnresolvedVariable
              return $wnd.erraiBusApplicationRoot + "/";
          }
          //noinspection JSUnresolvedVariable
          return $wnd.erraiBusApplicationRoot;
      }
  }-*/;

  public String getOutServiceEntryPoint() {
    return OUT_SERVICE_ENTRY_POINT;
  }

  public String getInServiceEntryPoint() {
    return IN_SERVICE_ENTRY_POINT;
  }

  private final List<BusLifecycleListener> lifecycleListeners = new ArrayList<BusLifecycleListener>();

  @Override
  public void addLifecycleListener(final BusLifecycleListener l) {
    lifecycleListeners.add(Assert.notNull(l));
  }

  @Override
  public void removeLifecycleListener(final BusLifecycleListener l) {
    lifecycleListeners.remove(l);
  }

  private enum EventType {
    ASSOCIATING {
      @Override
      public void deliverTo(final BusLifecycleListener l, final BusLifecycleEvent e) {
        l.busAssociating(e);
      }
    },
    DISASSOCIATING {
      @Override
      public void deliverTo(final BusLifecycleListener l, final BusLifecycleEvent e) {
        l.busDisassociating(e);
      }
    },
    ONLINE {
      @Override
      public void deliverTo(final BusLifecycleListener l, final BusLifecycleEvent e) {
        l.busOnline(e);
      }
    },
    OFFLINE {
      @Override
      public void deliverTo(final BusLifecycleListener l, final BusLifecycleEvent e) {
        l.busOffline(e);
      }
    };

    public abstract void deliverTo(BusLifecycleListener l, BusLifecycleEvent e);
  }

  /**
   * Puts the bus in the given state, firing all necessary transition events with no <tt>reason</tt> field.
   */
  private void setState(final State newState) {
    setState(newState, null);
  }

  /**
   * Puts the bus in the given state, firing all necessary transition events with the given reason.
   *
   * @param reason
   *     The error that led to this state transition, if any. Null is permitted.
   */
  private void setState(final State newState, final TransportError reason) {
    if (state == newState) {
      GWT.log("bus tried to transition from " + state + " ");
      return;
    }

    final List<EventType> events = new ArrayList<EventType>();

    switch (state) {
      case LOCAL_ONLY:
        if (newState == State.CONNECTING) {
          events.add(EventType.ASSOCIATING);
        }
        else if (newState == State.CONNECTED) {
          events.add(EventType.ASSOCIATING);
          events.add(EventType.ONLINE);
        }
        break;

      case CONNECTING:
        if (newState == State.LOCAL_ONLY) {
          events.add(EventType.DISASSOCIATING);
        }
        else if (newState == State.CONNECTED) {
          events.add(EventType.ONLINE);
        }
        break;

      case CONNECTED:
        if (newState == State.CONNECTING) {
          events.add(EventType.OFFLINE);
        }
        else if (newState == State.LOCAL_ONLY) {
          events.add(EventType.OFFLINE);
          events.add(EventType.DISASSOCIATING);
        }
        break;

      default:
        throw new IllegalStateException("Bus is in unknown state: " + state);
    }

    state = newState;

    for (final EventType et : events) {
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
