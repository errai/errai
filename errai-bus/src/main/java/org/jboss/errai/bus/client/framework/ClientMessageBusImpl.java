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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import junit.framework.AssertionFailedError;

import org.jboss.errai.bus.client.ErraiBus;
import org.jboss.errai.bus.client.api.BusLifecycleEvent;
import org.jboss.errai.bus.client.api.BusLifecycleListener;
import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.api.MessageCallback;
import org.jboss.errai.bus.client.api.MessageListener;
import org.jboss.errai.bus.client.api.PreInitializationListener;
import org.jboss.errai.bus.client.api.SessionExpirationListener;
import org.jboss.errai.bus.client.api.SubscribeListener;
import org.jboss.errai.bus.client.api.UnsubscribeListener;
import org.jboss.errai.bus.client.api.base.Capabilities;
import org.jboss.errai.bus.client.api.base.CommandMessage;
import org.jboss.errai.bus.client.api.base.DefaultErrorCallback;
import org.jboss.errai.bus.client.api.base.NoSubscribersToDeliverTo;
import org.jboss.errai.bus.client.api.base.TransportIOException;
import org.jboss.errai.bus.client.json.JSONUtilCli;
import org.jboss.errai.bus.client.protocols.BusCommands;
import org.jboss.errai.bus.client.util.BusTools;
import org.jboss.errai.common.client.api.Assert;
import org.jboss.errai.common.client.api.ResourceProvider;
import org.jboss.errai.common.client.api.extension.InitVotes;
import org.jboss.errai.common.client.protocols.MessageParts;
import org.jboss.errai.common.client.util.LogUtil;
import org.jboss.errai.marshalling.client.api.MarshallerFramework;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.RequestTimeoutException;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.URL;
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
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * The default client <tt>MessageBus</tt> implementation.  This bus runs in the browser and automatically federates
 * with the server immediately upon initialization.
 *
 * @author Mike Brock
 */
public class ClientMessageBusImpl implements ClientMessageBus {

  private enum State { LOCAL_ONLY, CONNECTING, CONNECTED }

  private final String clientId;
  private String sessionId;

  /* The encoded URL to be used for the bus */
  String OUT_SERVICE_ENTRY_POINT;
  String IN_SERVICE_ENTRY_POINT;

  /* ArrayList of all subscription listeners */
  private final List<SubscribeListener> onSubscribeHooks
      = new ArrayList<SubscribeListener>();

  /* ArrayList of all un-subscription listeners */
  private final List<UnsubscribeListener> onUnsubscribeHooks
      = new ArrayList<UnsubscribeListener>();

  /* Used to build the HTTP POST request */
  private RequestBuilder sendBuilder;

  private volatile boolean cometChannelOpen = true;
  private volatile boolean webSocketUpgradeAvailable = false;
  private volatile boolean webSocketOpen = false;
  private String webSocketUrl;
  private String webSocketToken;
  private Object webSocketChannel;

  public final MessageCallback remoteCallback = new RemoteMessageCallback();

  private RequestCallback receiveCommCallback = new NoPollRequestCallback();

  private final Map<String, List<MessageCallback>> subscriptions =
      new HashMap<String, List<MessageCallback>>();

  private final Map<String, List<MessageCallback>> localSubscriptions =
      new HashMap<String, List<MessageCallback>>();

  private final Map<String, MessageCallback> remotes
      = new HashMap<String, MessageCallback>();

  private final List<SessionExpirationListener> sessionExpirationListeners
      = new ArrayList<SessionExpirationListener>();

  private final List<PreInitializationListener> preInitializationListeners
      = new ArrayList<PreInitializationListener>();

  private final List<TransportErrorHandler> transportErrorHandlers
      = new ArrayList<TransportErrorHandler>();

  /* A list of {@link Runnable} initialization tasks to be executed after the bus has successfully finished it's
* initialization and is now communicating with the remote bus. */
  private final List<Runnable> deferredSubscriptions = new ArrayList<Runnable>();
  private final List<Runnable> postInitTasks = new ArrayList<Runnable>();
  private final List<Message> deferredMessages = new ArrayList<Message>();
  private final Queue<Message> toSendBuffer = new LinkedList<Message>();

  /* True if the client's message bus has been initialized */
  private boolean initialized = false;
  private boolean reinit = false;
  private boolean postInit = false;
  private boolean stateSyncInProgress = false;
  private boolean sessionReEstablish = false;

  /**
   * The unique ID that will sent with the next request.
   * <p/>
   * IMPORTANT: only access this member via {@link #getNextRequestNumber()}}.
   */
  private int txNumber = 0;
  private int rxNumber = 0;
  private long lastTx = System.currentTimeMillis();
  boolean txActive = false;
  boolean rxActive = false;

  private boolean disconnected = false;

  private State state = State.LOCAL_ONLY;

  private BusErrorDialog errorDialog;

  static {
    MarshallerFramework.initializeDefaultSessionProvider();
  }

  private LogAdapter logAdapter = new LogAdapter() {
    @Override
    public void warn(final String message) {
      GWT.log("WARN: " + message, null);
    }

    @Override
    public void info(final String message) {
      GWT.log("INFO: " + message, null);
    }

    @Override
    public void debug(final String message) {
      GWT.log("DEBUG: " + message, null);
    }

    @Override
    public void error(final String message, final Throwable t) {
      showError(message, t);
    }
  };

  public ClientMessageBusImpl() {
    clientId = String.valueOf(com.google.gwt.user.client.Random.nextInt(99999))
        + "-" + (System.currentTimeMillis() % (com.google.gwt.user.client.Random.nextInt(99999) + 1));

    IN_SERVICE_ENTRY_POINT = "in." + clientId + ".erraiBus";
    OUT_SERVICE_ENTRY_POINT = "out." + clientId + ".erraiBus";

    init();
  }

  private RequestBuilder getSendBuilder() {
    final RequestBuilder builder = new RequestBuilder(
        RequestBuilder.POST,
        URL.encode(getApplicationRoot() + OUT_SERVICE_ENTRY_POINT) + "?z=" + getNextRequestNumber()
    );

    builder.setHeader("Content-Type", "application/json; charset=utf-8");
    builder.setHeader(ClientMessageBus.REMOTE_QUEUE_ID_HEADER, clientId);

    return builder;
  }

  private RequestBuilder getReceiveBuilder() {
    final RequestBuilder builder = new RequestBuilder(
        RequestBuilder.GET,
        URL.encode(getApplicationRoot() + IN_SERVICE_ENTRY_POINT) + "?z=" + getNextRequestNumber()
    );

    builder.setHeader("Content-Type", "application/json; charset=utf-8");
    builder.setHeader(ClientMessageBus.REMOTE_QUEUE_ID_HEADER, clientId);
    return builder;
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
    if (BuiltInServices.ServerBus.name().equals(subject) && subscriptions.containsKey(BuiltInServices.ServerBus.name()))
      return null;

    if (!postInit && !stateSyncInProgress) {
      final DeferredSubscription deferredSubscription = new DeferredSubscription();

      deferredSubscriptions.add(new Runnable() {
        @Override
        public void run() {
          deferredSubscription.attachSubscription(_subscribe(subject, callback, local));
        }

        @Override
        public String toString() {
          return "DeferredSubscribe:" + subject;
        }
      });

      return deferredSubscription;
    }

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
          logError("receiver '" + subject + "' threw an exception", decodeCommandMessage(message), e);
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
   * Fire listeners to notify that a new subscription has been registered on the bus.
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
   * Fire listeners to notify that a subscription has been unregistered from the bus
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

  private static final ResourceProvider<RequestDispatcher> dispatcherProvider
      = new ResourceProvider<RequestDispatcher>() {
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
   *     - if message does not contain a ToSubject field or if the message's callback throws
   *     an error.
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
        throw new RuntimeException("Cannot send message using this method" +
            " if the message does not contain a ToSubject field.");
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

  private void callErrorHandler(final Message message, final Throwable t) {
    if (message.getErrorCallback() != null) {
      message.getErrorCallback().error(message, t);
    }
    logError(t.getMessage(), "none", t);
  }

  private void directStore(final Message message) {
    final String subject = message.getSubject();

    if (remotes.containsKey(subject)) {
      remotes.get(subject).callback(message);
    }
    else if (subscriptions.containsKey(subject)) {
      deliverToSubscriptions(subscriptions, subject, message);
    }
    else if (localSubscriptions.containsKey(subject)) {
      deliverToSubscriptions(localSubscriptions, subject, message);
    }
    else {
      throw new NoSubscribersToDeliverTo(subject);
    }
  }

  private boolean throttleOutgoing() {
    return (System.currentTimeMillis() - lastTx) < 150;
  }

  /**
   * Add message to the queue that remotely transmits messages to the server.
   * All messages in the queue are then sent.
   *
   * @param message
   *     -
   */
  private void encodeAndTransmit(final Message message) {
    if (initialized && !message.hasPart(MessageParts.PriorityProcessing)
        && (!toSendBuffer.isEmpty() || throttleOutgoing())) {
      toSendBuffer.offer(message);
      if (!txActive && toSendBuffer.size() == 1) {
        new Timer() {
          @Override
          public void run() {
            if (!initialized) return;
            transmitRemote(BusTools.encodeMessages(toSendBuffer), new ArrayList<Message>(toSendBuffer));
          }
        }.schedule(150);
      }
    }
    else {
      transmitRemote(BusTools.encodeMessage(message), Collections.singletonList(message));
    }
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

  /**
   * Transmits JSON string containing message, using the <tt>sendBuilder</tt>
   *
   * @param message
   *     - JSON string representation of message
   * @param txMessages
   *     - Messages reference.
   */
  private void transmitRemote(final String message, final List<Message> txMessages) {
    if (message == null) return;

    try {
      txActive = true;

      if (webSocketOpen) {
        if (ClientWebSocketChannel.transmitToSocket(webSocketChannel, message)) {
          return;
        }
        else {
          LogUtil.log("websocket channel is closed. falling back to comet");

          //disconnected.
          webSocketOpen = false;
          webSocketChannel = null;
          cometChannelOpen = true;

          if (receiveCommCallback instanceof LongPollRequestCallback) {
            ((LongPollRequestCallback) receiveCommCallback).schedule();
          }
        }
      }

      try {
        getSendBuilder().sendRequest(message, new RequestCallback() {
          int statusCode = 0;

          @Override
          public void onResponseReceived(final Request request, final Response response) {
            switch (statusCode = response.getStatusCode()) {
              case 1:
              case 404:
              case 408:
              case 502:
              case 503:
              case 504: {
                // Sending the message failed.
                // Although the response may still be valid
                // Handle it gracefully
                //noinspection ThrowableInstanceNeverThrown

                final TransportIOException tioe
                    = new TransportIOException(response.getText(), response.getStatusCode(),
                    "Failure communicating with server");

                if (handleHTTPTransportError(request, tioe, statusCode)) {
                  return;
                }

                LogUtil.log("connection problem. server returned status code: " + response.getStatusCode()
                    + " (" + response.getStatusText() + ")");

                for (final Message txM : txMessages) {
                  callErrorHandler(txM, tioe);
                }
                return;
              }
            }

            /**
             * If the server bus returned us some client-destined messages
             * in response to our send, handle them now.
             */
            try {
              processIncomingPayload(response);
            }
            catch (AssertionFailedError e) {
              throw e;
            }
            catch (Throwable e) {
              for (final Message txM : txMessages) {
                callErrorHandler(txM, e);
              }
            }
            finally {
              lastTx = System.currentTimeMillis();
            }
          }

          @Override
          public void onError(final Request request, final Throwable exception) {
            handleHTTPTransportError(request, exception, statusCode);

            for (final Message txM : txMessages) {
              if (txM.getErrorCallback() == null || txM.getErrorCallback().error(txM, exception)) {
                logError("Failed to communicate with remote bus", "", exception);
              }
            }
          }
        });
      }
      catch (Exception e) {
        for (final Message txM : txMessages) {
          callErrorHandler(txM, e);
        }
      }
    }
    finally {
      txActive = false;
    }
  }

  private void performPoll() {
    try {
      if (rxActive || !cometChannelOpen) return;
      rxActive = true;
      getReceiveBuilder().sendRequest(null, receiveCommCallback);
    }
    catch (RequestTimeoutException e) {
      statusCode = 1;

      if (handleHTTPTransportError(null, e, statusCode)) {
        return;
      }

      receiveCommCallback.onError(null, e);
    }
    catch (Throwable t) {
      if (handleHTTPTransportError(null, t, statusCode)) {
        return;
      }

      DefaultErrorCallback.INSTANCE.error(null, t);
    }
    finally {
      rxActive = false;
    }
  }

  @Override
  public void stop(final boolean sendDisconnect) {
    if (state != State.LOCAL_ONLY)
      setState(State.LOCAL_ONLY);

    try {
      if (sendDisconnect && isRemoteCommunicationEnabled()) {
        sendBuilder.setHeader("phase", "disconnect");

        encodeAndTransmit(CommandMessage.createWithParts(new HashMap<String, Object>())
            .toSubject(BuiltInServices.ServerBus.name())
            .command(BusCommands.Disconnect)
            .set(MessageParts.PriorityProcessing, "1"));
      }

      unsubscribeAll(BuiltInServices.ClientBus.name());
      subscriptions.clear();
    }
    finally {
      this.lastTx = 0;
      this.toSendBuffer.clear();
      this.txActive = false;
      this.rxActive = false;

      this.remotes.clear();
      this.disconnected = true;
      this.initialized = false;
      this.postInit = false;
      this.stateSyncInProgress = false;
      this.sendBuilder = null;
      this.deferredSubscriptions.clear();
      this.postInitTasks.clear();

      InitVotes.reset();
    }
  }

  public class RemoteMessageCallback implements MessageCallback {
    @Override
    public void callback(final Message message) {
      encodeAndTransmit(message);
    }
  }

  public void setInitialized(final boolean initialized) {
    this.initialized = initialized;
  }

  public boolean isReinit() {
    return this.reinit;
  }

  private void setReinit(final boolean reinit) {
    this.reinit = reinit;
  }

  private void registerInitVoteCallbacks() {
    InitVotes.waitFor(ClientMessageBus.class);
    InitVotes.waitFor(RpcProxyLoader.class);
    InitVotes.registerOneTimeInitCallback(new Runnable() {
      @Override
      public void run() {
        completeInit();
      }
    });
  }

  /**
   * Initializes the message bus, by subscribing to the ClientBus (to receive subscription messages) and the
   * ClientErrorBus to dispatch errors when called.
   */
  @Override
  public void init() {
    declareDebugFunction();
    if (!reinit) {
      registerInitVoteCallbacks();
    }

    cometChannelOpen = true;

    if (sendBuilder == null) {

      initialized = false;
      disconnected = false;
      state = State.LOCAL_ONLY;

      remotes.clear();
      onSubscribeHooks.clear();
      onUnsubscribeHooks.clear();

      sendBuilder = getSendBuilder();
      if (!GWT.isScript()) {   // Hosted Mode
        if (isReinit()) {
          setReinit(true);
          init();
          setReinit(false);
          return;
        }
        else {
          init();
          return;
        }
      }
    }

    if (sendBuilder == null) {
      return;
    }

    if (reinit) {
      resubscribeShadowSubscriptions();
    }

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

          case CapabilitiesNotice:
            LogUtil.log("received capabilities notice from server. supported capabilities of remote: "
                + message.get(String.class, MessageParts.CapabilitiesFlags));

            for (final String capability : message.get(String.class, MessageParts.CapabilitiesFlags).split(",")) {
              switch (Capabilities.valueOf(capability)) {
                case WebSockets:
                  webSocketUrl = message.get(String.class, MessageParts.WebSocketURL);
                  webSocketToken = message.get(String.class, MessageParts.WebSocketToken);
                  webSocketUpgradeAvailable = true;
                  break;
                case LongPollAvailable:

                  LogUtil.log("initializing long poll subsystem");
                  receiveCommCallback = new LongPollRequestCallback();
                  break;
                case NoLongPollAvailable:
                  receiveCommCallback = new ShortPollRequestCallback();
                  if (message.hasPart(MessageParts.PollFrequency)) {
                    POLL_FREQUENCY = message.get(Integer.class, MessageParts.PollFrequency);
                  }
                  else {
                    POLL_FREQUENCY = 500;
                  }
                  break;
                case Proxy:
                  break;
              }
            }
            break;

          case RemoteMonitorAttach:
            break;

          case FinishStateSync:
            if (isInitialized()) {
              return;
            }

            new Timer() {
              @Override
              public void run() {
                LogUtil.log("received FinishStateSync message. preparing to bring up the federation");

                stateSyncInProgress = true;

                for (final Runnable deferredSubscription : deferredSubscriptions) {
                  deferredSubscription.run();
                }

                final List<String> subjects = new ArrayList<String>();
                for (final String s : subscriptions.keySet()) {
                  if (s.startsWith("local:")) continue;
                  if (!remotes.containsKey(s)) subjects.add(s);
                }

                sessionId = message.get(String.class, MessageParts.ConnectionSessionKey);

                remoteSubscribe(BuiltInServices.ServerBus.name());

                encodeAndTransmit(CommandMessage.createWithParts(new HashMap<String, Object>())
                    .toSubject(BuiltInServices.ServerBus.name())
                    .command(RemoteSubscribe)
                    .set(MessageParts.SubjectsList, subjects)
                    .set(PriorityProcessing, "1"));

                encodeAndTransmit(CommandMessage.createWithParts(new HashMap<String, Object>())
                    .toSubject(BuiltInServices.ServerBus.name())
                    .command(BusCommands.FinishStateSync)
                    .set(PriorityProcessing, "1"));


                /**
                 * ... also send RemoteUnsubscribe signals.
                 */
                addSubscribeListener(new SubscribeListener() {
                  @Override
                  public void onSubscribe(final SubscriptionEvent event) {
                    if (event.isLocalOnly() || event.getSubject().startsWith("local:")
                        || remotes.containsKey(event.getSubject())) {
                      return;
                    }

                    if (event.isNew()) {
                      encodeAndTransmit(CommandMessage.createWithParts(new HashMap<String, Object>())
                          .toSubject(BuiltInServices.ServerBus.name())
                          .command(RemoteSubscribe)
                          .set(Subject, event.getSubject())
                          .set(PriorityProcessing, "1"));
                    }
                  }
                });

                addUnsubscribeListener(new UnsubscribeListener() {
                  @Override
                  public void onUnsubscribe(final SubscriptionEvent event) {
                    encodeAndTransmit(CommandMessage.createWithParts(new HashMap<String, Object>())
                        .toSubject(BuiltInServices.ServerBus.name())
                        .command(RemoteUnsubscribe)
                        .set(Subject, event.getSubject())
                        .set(PriorityProcessing, "1"));
                  }
                });

                subscribe(DefaultErrorCallback.CLIENT_ERROR_SUBJECT, new MessageCallback() {
                  @Override
                  public void callback(final Message message) {
                    final String errorTo = message.get(String.class, MessageParts.ErrorTo);
                    if (errorTo == null) {
                      logError(message.get(String.class, MessageParts.ErrorMessage),
                          message.get(String.class, MessageParts.AdditionalDetails), null);
                    }
                    else {
                      message.toSubject(errorTo);
                      message.sendNowWith(ClientMessageBusImpl.this);
                    }
                  }
                });

                stateSyncInProgress = true;

                if (webSocketUpgradeAvailable) {
                  websocketUpgrade();
                }
                else {
                  InitVotes.voteFor(ClientMessageBus.class);
                }

                setState(State.CONNECTED);

                // end of FinishStateSync Timer
              }
            }.schedule(5);
            break;

          case SessionExpired:
            if (!sessionReEstablish) {
              sessionReEstablish = true;

              if (isReinit()) {
                showError("session was terminated and could not be re-established", null);
                return;
              }

              if (!isInitialized()) return;

              LogUtil.log("http session has expired. resetting bus and attempting reconnection.");

              for (final SessionExpirationListener listener : sessionExpirationListeners) {
                listener.onSessionExpire();
              }

              stop(false);
              init();
            }

            break;

          case WebsocketChannelVerify:
            LogUtil.log("received verification token for websocket connection");

            encodeAndTransmit(CommandMessage.createWithParts(new HashMap<String, Object>())
                .toSubject(BuiltInServices.ServerBus.name())
                .command(BusCommands.WebsocketChannelVerify)
                .copy(MessageParts.WebSocketToken, message));
            break;

          case WebsocketChannelOpen:

            cometChannelOpen = false;
            webSocketOpen = true;

            // send final message to open the channel
            ClientWebSocketChannel.transmitToSocket(webSocketChannel, getWebSocketNegotiationString());

            LogUtil.log("web socket channel successfully negotiated. comet channel deactivated.");

            new Timer() {
              @Override
              public void run() {
                InitVotes.voteFor(ClientMessageBus.class);
              }
            }.schedule(50);
            break;

          case WebsocketNegotiationFailed:
            webSocketChannel = null;
            logError("failed to connect to websocket: server rejected request",
                message.get(String.class, MessageParts.ErrorMessage), null);
            break;

          case Disconnect:
            stop(false);

            if (message.hasPart("Reason")) {
              logError("The bus was disconnected by the server", "Reason: "
                  + message.get(String.class, "Reason"), null);
            }
            break;
          case ConnectToQueue:
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
     * Send initial message to connect to the queue, to establish an HTTP session. Otherwise, concurrent
     * requests will result in multiple sessions being created.  Which is bad.  Avoid this at all costs.
     * Please.
     */
    if (!sendInitialMessage()) {
      logError("Could not connect to remote bus", "", null);
    }
  }

  private void websocketUpgrade() {
    LogUtil.log("attempting web sockets connection at URL: " + webSocketUrl);

    final Object o = ClientWebSocketChannel.attemptWebSocketConnect(ClientMessageBusImpl.this, webSocketUrl);

    if (o instanceof String) {
      LogUtil.log("could not use web sockets. reason: " + o);
      InitVotes.voteFor(ClientMessageBus.class);
    }
  }

  @SuppressWarnings("ConstantConditions")
  private void completeInit() {
    if (!postInit && !initialized) {
      postInit = true;

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

      sendAllDeferred();
      postInitTasks.clear();

      setInitialized(true);

      LogUtil.log("bus federation complete. now operating normally.");

      stateSyncInProgress = false;
      sessionReEstablish = false;
    }
  }

  private void remoteSubscribe(final String subject) {
    remotes.put(subject, remoteCallback);
  }

  Set<String> getRemoteSubscriptions() {
    if (remotes == null)
      return null;

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
      // defensively copy and don't use a fail-fast iterator -- these tasks may send messages
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

  private boolean handleHTTPTransportError(final Request request, final Throwable throwable, final int statusCode) {
    setState(State.CONNECTING);

    class ErrorHandling {
      boolean stopDefaultErrorHandler = false;
    }

    final ErrorHandling errorHandling = new ErrorHandling();

    final TransportError transportError = new TransportError() {

      @Override
      public Request getRequest() {
        return request;
      }

      @Override
      public String getErrorMessage() {
        return throwable != null ? throwable.getMessage() : "";
      }

      @Override
      public boolean isHTTP() {
        return true;
      }

      @Override
      public boolean isWebSocket() {
        return false;
      }

      @Override
      public int getStatusCode() {
        return statusCode;
      }

      @Override
      public Throwable getException() {
        return throwable;
      }

      @Override
      public void stopDefaultErrorHandling() {
        errorHandling.stopDefaultErrorHandler = true;
      }

      @Override
      public BusControl getBusControl() {
        return new BusControl() {
          @Override
          public void disconnect() {
            ClientMessageBusImpl.this.stop(true);
          }

          @Override
          public void reconnect() {
            ClientMessageBusImpl.this.setReinit(true);
            ClientMessageBusImpl.this.init();
          }
        };
      }
    };

    for (final TransportErrorHandler handler : transportErrorHandlers) {
      handler.onError(transportError);
    }

    return errorHandling.stopDefaultErrorHandler;
  }

  /**
   * Sends the initial message to connect to the queue, to establish an HTTP session. Otherwise, concurrent
   * requests will result in multiple sessions being created.
   *
   * @return true if initial message was sent successfully.
   */

  private boolean sendInitialMessage() {
    if (!isRemoteCommunicationEnabled()) {
      LogUtil.log("initializing client bus in offline mode (erraiBusRemoteCommunicationEnabled was set to false)");
      InitVotes.voteFor(ClientMessageBus.class);
      InitVotes.voteFor(RpcProxyLoader.class);
      return true;
    }

    setState(State.CONNECTING);

    try {
      LogUtil.log("sending initial handshake to remote bus");

      final RequestBuilder initialRequest = getSendBuilder();

      initialRequest.setHeader("phase", "connection");
      initialRequest.sendRequest("{\"CommandType\":\"ConnectToQueue\",\"ToSubject\":\"ServerBus\"," +
          " \"PriorityProcessing\":\"1\"}", new RequestCallback() {
        @Override
        public void onResponseReceived(final Request request, final Response response) {
          try {
            LogUtil.log("received response from initial handshake.");
            processIncomingPayload(response);
            initializeMessagingBus();
          }
          catch (Exception e) {
            e.printStackTrace();
            logError("Error attaching to bus", e.getMessage() + "<br/>Message Contents:<br/>"
                + response.getText(), e);
          }
        }

        @Override
        public void onError(final Request request, final Throwable exception) {
          logError("Could not connect to remote bus", "", exception);
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

  int maxRetries = 5;
  int retries = 0;
  int timeout = 2000;
  int statusCode = 0;
  DialogBox timeoutDB;
  Label timeoutMessage;

  private void createConnectAttemptGUI() {
    timeoutDB = new DialogBox();
    timeoutMessage = new Label();
    timeoutDB.add(timeoutMessage);
    RootPanel.get().add(timeoutDB);
    timeoutDB.show();
    timeoutDB.center();
  }

  private void clearConnectAttemptGUI() {
    timeoutDB.hide();
    RootPanel.get().remove(timeoutDB);
    timeoutDB = null;
    timeoutMessage = null;
    retries = 0;
  }

  private class LongPollRequestCallback implements RequestCallback {
    @Override
    public void onError(final Request request, final Throwable throwable) {
      if (handleHTTPTransportError(request, throwable, statusCode)) {
        return;
      }

      switch (statusCode) {
        case 0:
          return;
        case 1:
        case 404:
        case 408:
        case 502:
        case 504:
          if (retries <= maxRetries) {
            if (timeoutDB == null) {
              createConnectAttemptGUI();
            }

            final String message = "Attempting reconnection -- Retries: " + (maxRetries - retries);
            logAdapter.warn(message);
            timeoutMessage.setText(message);
            retries++;

            new Timer() {
              @Override
              public void run() {
                cometChannelOpen = true;
                performPoll();
              }
            }.schedule(timeout);

            statusCode = 0;
            return;
          }
          else {
            timeoutMessage.setText("Connection re-attempt failed!");
            DefaultErrorCallback.INSTANCE.error(null, throwable);
            stop(false);
          }
          break;

        default:
          // polling error is probably unrecoverable; go to local-only mode
          DefaultErrorCallback.INSTANCE.error(null, throwable);
          stop(false);
      }
    }

    @Override
    public void onResponseReceived(final Request request, final Response response) {
      if (response.getStatusCode() != 200) {
        switch (statusCode = response.getStatusCode()) {
          case 200:
          case 300:
          case 301:
          case 302:
          case 303:
          case 304:
          case 305:
          case 307:
            break;
          default:
            cometChannelOpen = false;
            onError(request, new TransportIOException("unexpected response code: " + statusCode, statusCode,
                response.getStatusText()));
            return;
        }
      }

      if (retries != 0) {
        clearConnectAttemptGUI();
      }

      try {
        if (state != State.CONNECTED) {
          setState(State.CONNECTED);
        }
        processIncomingPayload(response);
        schedule();
      }
      catch (Throwable e) {
        logError("bus disconnected due to fatal error", response.getText(), e);
      }
    }

    public void schedule() {
      if (!cometChannelOpen) return;
      new Timer() {
        @Override
        public void run() {
          performPoll();
        }
      }.schedule(25);
    }
  }

  private class NoPollRequestCallback extends LongPollRequestCallback {
    @Override
    public void schedule() {
      performPoll();
    }
  }

  private class ShortPollRequestCallback extends LongPollRequestCallback {
    public ShortPollRequestCallback() {
    }

    @Override
    public void schedule() {
      new Timer() {
        @Override
        public void run() {
          performPoll();
        }
      }.schedule(POLL_FREQUENCY);
    }
  }

  public static int POLL_FREQUENCY = 250;

  /**
   * Initializes the message bus by setting up the <tt>recvBuilder</tt> to accept responses. Also, initializes the
   * incoming timer to ensure the client's polling with the server is active.
   */
  private void initializeMessagingBus() {
    if (disconnected) {
      return;
    }

    ((RpcProxyLoader) GWT.create(RpcProxyLoader.class)).loadProxies(ClientMessageBusImpl.this);

    InitVotes.voteFor(RpcProxyLoader.class);

    new Timer() {
      @Override
      public void run() {
        performPoll();
      }
    }.schedule(10);
  }

  /**
   * Add runnable tasks to be run after the message bus is initialized.
   *
   * @param run
   *     a {@link Runnable} task.
   */
  @Override
  public void addPostInitTask(final Runnable run) {
    if (isInitialized() || postInit) {
      run.run();
      return;
    }
    postInitTasks.add(run);
  }

  @Override
  public void addSessionExpirationListener(final SessionExpirationListener listener) {
    sessionExpirationListeners.add(Assert.notNull(listener));
  }

  @Override
  public void addPreInitializationListener(final PreInitializationListener listener) {
    preInitializationListeners.add(Assert.notNull(listener));
  }

  /**
   * Do-nothing function, should eventually be able to add a global listener to receive all messages. Though global
   * message dispatches the message to all listeners attached.
   *
   * @param listener
   *     - listener to accept all messages dispatched
   */
  @Override
  public void addGlobalListener(final MessageListener listener) {
  }

  /**
   * Adds a subscription listener, so it is possible to add subscriptions to the client.
   *
   * @param listener
   *     - subscription listener
   */
  @Override
  public void addSubscribeListener(final SubscribeListener listener) {
    this.onSubscribeHooks.add(Assert.notNull(listener));
  }

  /**
   * Adds an unsubscribe listener, so it is possible for applications to remove subscriptions from the client
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
      decode.append("<tr><td>").append(entry.getKey()).append("</td><td>").append(entry.getValue()).append("</td></tr>");
    }

    return decode.append("</tbody></table>").toString();
  }

  private void logError(final String message, final String additionalDetails, final Throwable e) {
    logAdapter.error(message + " -- Additional Details: " + additionalDetails, e);
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
   * Process the incoming payload and push all the incoming messages onto the bus.
   *
   * @param response
   *     -
   *
   * @throws Exception
   *     -
   */
  private void processIncomingPayload(final Response response) throws Exception {
    procPayload(response.getText());
  }

  public void procPayload(final String text) {
    // LogUtil.log("RX:" + text);
    try {
      for (final MarshalledMessage m : decodePayload(text)) {
        rxNumber++;
        _store(m.getSubject(), JSONUtilCli.decodeCommandMessage(m.getMessage()));
      }
    }
    catch (RuntimeException e) {
      e.printStackTrace();
      logError("Error delivering message into bus", text, e);
    }
  }

  @Override
  public void attachMonitor(final BusMonitor monitor) {
  }

  @Override
  public void setLogAdapter(final LogAdapter logAdapter) {
    this.logAdapter = logAdapter;
  }

  @Override
  public LogAdapter getLogAdapter() {
    return logAdapter;
  }

  @Override
  public Set<String> getAllRegisteredSubjects() {
    return Collections.unmodifiableSet(subscriptions.keySet());
  }

  @Override
  public void addTransportErrorHandler(final TransportErrorHandler errorHandler) {
    transportErrorHandlers.add(errorHandler);
  }

  public void _store(final String subject, final Message msg) {
    if (subscriptions.containsKey(subject)) {
      final ArrayList<MessageCallback> messageCallbacks = new ArrayList<MessageCallback>(subscriptions.get(subject));
      for (final MessageCallback cb : messageCallbacks) {
        cb.callback(msg);
      }
    }
  }

  public int getNextRequestNumber() {
    if (txNumber == Integer.MAX_VALUE) {
      txNumber = 0;
    }
    return txNumber++;
  }

  private String getWebSocketNegotiationString() {
    return "{\"" + MessageParts.CommandType.name() + "\":\"" + BusCommands.ConnectToQueue.name()
        + "\", \"" + MessageParts.ConnectionSessionKey + "\":\"" + sessionId + "\"" +
        ",\"" + MessageParts.WebSocketToken + "\":\"" + webSocketToken + "\"}";
  }

  public void attachWebSocketChannel(final Object o) {
    LogUtil.log("web socket opened. sending negotiation message.");
    ClientWebSocketChannel.transmitToSocket(o, getWebSocketNegotiationString());
    webSocketChannel = o;
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
          if (Window.confirm("Are you sure you want to disconnect and de-federate the local bus from the server bus? "
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
      if (!showErrors) return;

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

      contentPanel.add(new HTML(buildTrace.toString() + "<br/><strong>Additional Details:</strong>" + additionalDetails + "</tt>"));

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

    LogUtil.nativeLog("Bus State              : " + (((ClientMessageBusImpl) ErraiBus.get()).initialized ? "Online/Federated" : "Disconnected"));
    LogUtil.nativeLog("");
    LogUtil.nativeLog("Comet Channel          : " + (((ClientMessageBusImpl) ErraiBus.get()).cometChannelOpen ? "Active" : "Offline"));
    LogUtil.nativeLog("  Endpoint (RX)        : " + (((ClientMessageBusImpl) ErraiBus.get()).getReceiveBuilder().getUrl()));
    LogUtil.nativeLog("  Endpoint (TX)        : " + (((ClientMessageBusImpl) ErraiBus.get()).getSendBuilder().getUrl()));
    LogUtil.nativeLog("");
    LogUtil.nativeLog("WebSocket Channel      : " + (((ClientMessageBusImpl) ErraiBus.get()).webSocketOpen ? "Active" : "Offline"));
    LogUtil.nativeLog("  Endpoint (RX/TX)     : " + (((ClientMessageBusImpl) ErraiBus.get()).webSocketUrl));
    LogUtil.nativeLog("");
    LogUtil.nativeLog("Total TXs              : " + (((ClientMessageBusImpl) ErraiBus.get()).txNumber));
    LogUtil.nativeLog("Total RXs              : " + (((ClientMessageBusImpl) ErraiBus.get()).rxNumber));
    LogUtil.nativeLog("");
    LogUtil.nativeLog("Endpoints");
    LogUtil.nativeLog("  Remote (total)       : " + (((ClientMessageBusImpl) ErraiBus.get()).remotes.size()));
    LogUtil.nativeLog("  Local (total)        : " + (((ClientMessageBusImpl) ErraiBus.get()).subscriptions.size()));

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
      LogUtil.nativeLog(localName + " (" + ((ClientMessageBusImpl) ErraiBus.get()).subscriptions.get(localName).size() + ")");
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
   * The JavaScript variable <code>erraiBusRemoteCommunicationEnabled</code> can be used
   * to control this value. If the variable is not present in the window object, the default
   * value <code>true</code> is returned.
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

  /**
   * Returns the application root for the remote message bus endpoints.
   *
   * @return path with trailing slash, or empty string if undefined or explicitly set to empty
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

  private final List<BusLifecycleListener> lifecycleListeners = new ArrayList<BusLifecycleListener>();

  @Override
  public void addLifecycleListener(BusLifecycleListener l) {
    lifecycleListeners.add(Assert.notNull(l));
  }

  @Override
  public void removeLifecycleListener(BusLifecycleListener l) {
    lifecycleListeners.remove(l);
  }

  private enum EventType {
    ASSOCIATING {
      @Override
      public void deliverTo(BusLifecycleListener l, BusLifecycleEvent e) {
        l.busAssociating(e);
      }
    },
    DISASSOCIATING {
      @Override
      public void deliverTo(BusLifecycleListener l, BusLifecycleEvent e) {
        l.busDisassociating(e);
      }
    },
    ONLINE {
      @Override
      public void deliverTo(BusLifecycleListener l, BusLifecycleEvent e) {
        l.busOnline(e);
      }
    },
    OFFLINE {
      @Override
      public void deliverTo(BusLifecycleListener l, BusLifecycleEvent e) {
        l.busOffline(e);
      }
    };

    public abstract void deliverTo(BusLifecycleListener l, BusLifecycleEvent e);
  }

  /**
   * Puts the bus in the given state, firing all necessary transition events.
   */
  private void setState(State newState) {
    GWT.log("Bus State: " + state + " -> " + newState + " (" + lifecycleListeners.size() + " listeners)");
    if (state == newState) {
      logAdapter.warn("Bus tried to transition from " + state + " to " + newState);
      return;
    }

    List<EventType> events = new ArrayList<EventType>();

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

    for (EventType et : events) {
      final BusLifecycleEvent e = new BusLifecycleEvent(this);
      for (int i = lifecycleListeners.size() - 1; i >= 0; i--) {
        try {
          et.deliverTo(lifecycleListeners.get(i), e);
        }
        catch (Throwable t) {
          logAdapter.warn("Listener threw exception: " + t);
          t.printStackTrace();
        }
      }
    }
  }

}
