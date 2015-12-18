/*
 * Copyright (C) 2012 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.bus.server;

import static org.jboss.errai.bus.client.api.base.MessageBuilder.createConversation;
import static org.jboss.errai.bus.client.util.ErrorHelper.handleMessageDeliveryFailure;
import static org.jboss.errai.bus.server.io.websockets.WebSocketTokenManager.getNewOneTimeToken;
import static org.jboss.errai.bus.server.io.websockets.WebSocketTokenManager.verifyOneTimeToken;
import static org.jboss.errai.common.client.protocols.MessageParts.ConnectionSessionKey;
import static org.jboss.errai.common.client.protocols.MessageParts.RemoteServices;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.http.HttpServletRequest;

import org.jboss.errai.bus.client.api.BusMonitor;
import org.jboss.errai.bus.client.api.QueueSession;
import org.jboss.errai.bus.client.api.RoutingFlag;
import org.jboss.errai.bus.client.api.SubscribeListener;
import org.jboss.errai.bus.client.api.Subscription;
import org.jboss.errai.bus.client.api.UnsubscribeListener;
import org.jboss.errai.bus.client.api.base.Capabilities;
import org.jboss.errai.bus.client.api.base.ConversationMessage;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.api.base.NoSubscribersToDeliverTo;
import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.bus.client.api.messaging.MessageCallback;
import org.jboss.errai.bus.client.framework.BuiltInServices;
import org.jboss.errai.bus.client.framework.SubscriptionEvent;
import org.jboss.errai.bus.client.protocols.BusCommand;
import org.jboss.errai.bus.client.util.BusTools;
import org.jboss.errai.bus.server.api.MessageQueue;
import org.jboss.errai.bus.server.api.QueueCloseEvent;
import org.jboss.errai.bus.server.api.QueueClosedListener;
import org.jboss.errai.bus.server.api.ServerMessageBus;
import org.jboss.errai.bus.server.cluster.ClusteringProvider;
import org.jboss.errai.bus.server.io.BufferHelper;
import org.jboss.errai.bus.server.io.PageUtil;
import org.jboss.errai.bus.server.io.buffers.BufferColor;
import org.jboss.errai.bus.server.io.buffers.TransmissionBuffer;
import org.jboss.errai.bus.server.io.websockets.WebSocketServerHandler;
import org.jboss.errai.bus.server.io.websockets.WebSocketTokenManager;
import org.jboss.errai.bus.server.service.ErraiConfigAttribs;
import org.jboss.errai.bus.server.service.ErraiService;
import org.jboss.errai.bus.server.service.ErraiServiceConfigurator;
import org.jboss.errai.bus.server.util.LocalContext;
import org.jboss.errai.bus.server.util.SecureHashUtil;
import org.jboss.errai.common.client.protocols.MessageParts;
import org.jboss.errai.common.client.protocols.Resources;
import org.jboss.errai.common.server.api.ErraiBootstrapFailure;
import org.slf4j.Logger;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * The <tt>ServerMessageBusImpl</tt> implements the <tt>ServerMessageBus</tt>, making it possible for the server to
 * send and receive messages
 *
 * @author Mike Brock
 */
@Singleton
public class ServerMessageBusImpl implements ServerMessageBus {
  private final TransmissionBuffer transmissionbuffer;

  private final Map<String, DeliveryPlan> subscriptions = new ConcurrentHashMap<String, DeliveryPlan>();
  private final Set<String> globalSubscriptions = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
  private final Map<String, RemoteMessageCallback> remoteSubscriptions = new ConcurrentHashMap<String, RemoteMessageCallback>();

  private final Map<QueueSession, MessageQueue> messageQueues = new ConcurrentHashMap<QueueSession, MessageQueue>();

  private final Map<MessageQueue, List<Message>> deferredQueue = new ConcurrentHashMap<MessageQueue, List<Message>>();
  private final Map<String, QueueSession> sessionLookup = new ConcurrentHashMap<String, QueueSession>();
  private final Map<String, ClusterWaitEntry> deadLetter = new ConcurrentHashMap<String, ClusterWaitEntry>();

  private final List<SubscribeListener> subscribeListeners = new ArrayList<SubscribeListener>();
  private final List<UnsubscribeListener> unsubscribeListeners = new ArrayList<UnsubscribeListener>();
  private final List<QueueClosedListener> queueClosedListeners = new ArrayList<QueueClosedListener>();

  private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

  private static final Logger log = getLogger(ServerMessageBus.class);

  private BusMonitor busMonitor;

  private final Set<String> reservedNames = new HashSet<String>();

  private final boolean hostedModeTesting;
  private final boolean doLongPolling;
  private final int messageQueueTimeoutSecs;
  private final boolean sseEnabled;
  private final boolean webSocketServlet;
  private final boolean webSocketServer;
  private final boolean useSecureWebsocket;

  private final boolean clustering;
  private final ClusteringProvider clusteringProvider;

  /**
   * Sets up the <tt>ServerMessageBusImpl</tt> with the configuration supplied. Also, initializes the bus' callback
   * functions, scheduler, and monitor
   * <p/>
   * When deploying services on the server-side, it is possible to obtain references to the
   * <tt>ErraiServiceConfigurator</tt> by declaring it as injection dependencies
   */
  @Inject
  public ServerMessageBusImpl(final ErraiService service, final ErraiServiceConfigurator config) {
    this.hostedModeTesting = ErraiConfigAttribs.HOSTED_MODE_TESTING.getBoolean(config);
    this.doLongPolling = !hostedModeTesting && ErraiConfigAttribs.DO_LONG_POLL.getBoolean(config);
    this.messageQueueTimeoutSecs = ErraiConfigAttribs.MESSAGE_QUEUE_TIMEOUT_SECS.getInt(config);
    this.sseEnabled = ErraiConfigAttribs.ENABLE_SSE_SUPPORT.getBoolean(config);
    this.webSocketServer = ErraiConfigAttribs.ENABLE_WEB_SOCKET_SERVER.getBoolean(config);

    final int webSocketPort;
    final String webSocketPath;

    webSocketServlet = ErraiConfigAttribs.WEBSOCKET_SERVLET_ENABLED.getBoolean(config);
    useSecureWebsocket = ErraiConfigAttribs.FORCE_SECURE_WEBSOCKET.getBoolean(config) 
            || ErraiConfigAttribs.SECURE_WEB_SOCKET_SERVER.getBoolean(config);

    if (webSocketServlet) {
      webSocketPath = ErraiConfigAttribs.WEBSOCKET_SERVLET_CONTEXT_PATH.get(config);
      webSocketPort = -1;
    }
    else {
      webSocketPath = ErraiConfigAttribs.WEB_SOCKET_URL.get(config);
      webSocketPort = ErraiConfigAttribs.WEB_SOCKET_PORT.getInt(config);
    }

    final Integer bufferSize = ErraiConfigAttribs.BUS_BUFFER_SIZE.getInt(config);
    Integer segmentSize = ErraiConfigAttribs.BUS_BUFFER_SEGMENT_SIZE.getInt(config);
    Integer segmentCount = ErraiConfigAttribs.BUS_BUFFER_SEGMENT_COUNT.getInt(config);
    final String allocMode = ErraiConfigAttribs.BUS_BUFFER_ALLOCATION_MODE.get(config);

    if (segmentSize == null) {
      segmentSize = 8 * 1024;
    }
    else {
      segmentSize = segmentSize * 1024;
    }

    if (bufferSize != null) {
      segmentCount = (bufferSize * 1024 * 1024) / segmentSize;
    }
    else if (segmentCount == null) {
      segmentCount = 4096;
    }

    final boolean directAlloc;
    if (allocMode != null) {
      if ("direct".equals(allocMode)) {
        directAlloc = true;
      }
      else if ("heap".equals(allocMode)) {
        directAlloc = false;
      }
      else {
        throw new ErraiBootstrapFailure("unrecognized option for property: "
            + ErraiConfigAttribs.BUS_BUFFER_ALLOCATION_MODE.get(config));
      }
    }
    else {
      directAlloc = false;
    }

    TransmissionBuffer buffer;
    if (directAlloc) {
      try {
        buffer = TransmissionBuffer.createDirect(segmentSize, segmentCount);
      }
      catch (OutOfMemoryError e) {
        log.warn("could not allocate direct memory buffer. insufficient direct memory. increase the direct memory " +
            "buffer size with the JVM argument: -XX:MaxDirectMemorySize=<size>");
        log.warn("falling back to a heap allocated buffer.");
        buffer = TransmissionBuffer.create(segmentSize, segmentCount);
      }
    }
    else {
      buffer = TransmissionBuffer.create(segmentSize, segmentCount);
    }

    transmissionbuffer = buffer;

    /**
     * Define the default ServerBus service used for intrabus communication.
     */
    subscribe(BuiltInServices.ServerBus.name(), new ServerBusMessageCallback(webSocketPath, webSocketPort));

    addSubscribeListener(new DefaultSubscribeListener());
    addUnsubscribeListener(new DefaultUnsubscribeListener());

    scheduler.scheduleAtFixedRate(new HousekeeeperRunnable(), 8, 8, TimeUnit.SECONDS);

    try {
      clustering = ErraiConfigAttribs.ENABLE_CLUSTERING.getBoolean(config);
      final String clusteringProviderCls = ErraiConfigAttribs.CLUSTERING_PROVIDER.get(config);
      //noinspection unchecked
      clusteringProvider = Guice.createInjector(new AbstractModule() {
        @Override
        protected void configure() {
          bind(ServerMessageBus.class).toInstance(ServerMessageBusImpl.this);
          bind(ErraiServiceConfigurator.class).toInstance(config);
          bind(ErraiService.class).toInstance(service);
        }
      }).getInstance((Class<ClusteringProvider>) Class.forName(clusteringProviderCls));
    }
    catch (Exception e) {
      throw new RuntimeException("could not initialize clustering provider", e);
    }
  }

  private void addQueue(final QueueSession session, final MessageQueue queue) {
    messageQueues.put(session, queue);
    sessionLookup.put(session.getSessionId(), session);
  }

  /**
   * Configures the server message bus with the specified <tt>ErraiServiceConfigurator</tt>.
   * Presently there are no configurable parameters.
   */
  @Override
  public void configure(final ErraiServiceConfigurator config) {
    // no configuration in current implementation
  }

  /**
   * Sends a message globally to all subscriptions containing the same subject as the specified message.
   *
   * @param message
   *     - The message to be sent.
   */
  @Override
  public void sendGlobal(final Message message) {
    message.commit();
    final String subject = message.getSubject();

    if (!subscriptions.containsKey(subject) && !subscriptions.containsKey("local:".concat(subject))
            && !remoteSubscriptions.containsKey(subject)) {

      delayOrFail(message, new Runnable() {
        @Override
        public void run() {
          sendGlobal(message);
        }
      });

      return;
    }

    if (isMonitor()) {
      if (message.isFlagSet(RoutingFlag.FromRemote)) {
        busMonitor.notifyIncomingMessageFromRemote(
            message.getResource(QueueSession.class, Resources.Session.name()).getSessionId(), message);
      }
      else {
        if (subscriptions.containsKey(subject)) {
          busMonitor.notifyInBusMessage(message);
        }
      }
    }

    if (subscriptions.containsKey(subject)) {
      subscriptions.get(subject).deliver(message);
    }
    else if (subscriptions.containsKey("local:".concat(subject))) {
      subscriptions.get("local:".concat(subject)).deliver(message);
    }
  }

  private void delayOrFail(final Message message, final Runnable deliveryTaskRunnable) {
    if (message.isFlagSet(RoutingFlag.RetryDelivery)
        && message.getResource(Integer.class, Resources.RetryAttempts.name()) > 3) {
      final NoSubscribersToDeliverTo ntdt = new NoSubscribersToDeliverTo(message.getSubject());
      if (message.getErrorCallback() != null) {
        message.getErrorCallback().error(message, ntdt);
      }
      throw ntdt;
    }
    message.setFlag(RoutingFlag.RetryDelivery);
    if (!message.hasResource(Resources.RetryAttempts.name())) {
      message.setResource(Resources.RetryAttempts.name(), 0);
    }
    message.setResource(Resources.RetryAttempts.name(),
        message.getResource(Integer.class, Resources.RetryAttempts.name()) + 1);
    getScheduler().schedule(new Runnable() {
      @Override
      public void run() {
        deliveryTaskRunnable.run();
      }
    }, 250, TimeUnit.MILLISECONDS);
  }

  /**
   * Sends the <tt>message</tt>
   *
   * @param message
   *     - the message to send
   */
  @Override
  public void send(final Message message) {
    message.commit();
    if (message.hasResource(Resources.Session.name())) {
      message.setFlag(RoutingFlag.NonGlobalRouting);
      send(getQueueByMessage(message), message, true);
    }
    else if (message.hasPart(MessageParts.SessionID)) {
      message.setFlag(RoutingFlag.NonGlobalRouting);
      try {
        send(getQueueBySession(message.get(String.class, MessageParts.SessionID)), message, true);
      }
      catch (final QueueUnavailableException e) {
        forwardToCluster(message, new Runnable() {
          @Override
          public void run() {
            log.debug("Failed to deliver message to queue with id:" + message.get(String.class, MessageParts.SessionID), e);
          }
        });
      }
    }
    else {
      sendGlobal(message);
    }
  }

  /**
   * Parses the message appropriately and enqueues it for delivery
   *
   * @param message
   *     - the message to be sent
   * @param fireListeners
   *     - true if all listeners attached should be notified of delivery
   */
  @Override
  public void send(final Message message, final boolean fireListeners) {
    message.commit();
    if (!message.hasResource(Resources.Session.name())) {
      handleMessageDeliveryFailure(this, message,
          "cannot automatically route message. no session contained in message.", null, false);
    }

    final MessageQueue queue = getQueue(getSession(message));

    if (queue == null) {
      handleMessageDeliveryFailure(this, message,
          "cannot automatically route message. no session contained in message.", null, false);
    }

    send(message.hasPart(MessageParts.SessionID) ? getQueueBySession(message.get(String.class, MessageParts.SessionID)) :
        getQueueByMessage(message), message, fireListeners);
  }

  private void send(final MessageQueue queue, final Message message, final boolean fireListeners) {
    try {
      if (isMonitor()) {
        busMonitor.notifyOutgoingMessageToRemote(queue.getSession().getSessionId(), message);
      }

      enqueueForDelivery(queue, message);
    }
    catch (NoSubscribersToDeliverTo nstdt) {
      // catch this so we can get a full trace
      handleMessageDeliveryFailure(this, message, "No subscribers to deliver to", nstdt, false);
    }
  }

  private final Random random = new Random(System.nanoTime());

  private void forwardToCluster(final Message message, final Runnable timeoutCallback) {
    if (clustering && message.hasPart(MessageParts.SessionID)) {
      final String sessionId = message.get(String.class, MessageParts.SessionID);
      if (!sessionLookup.containsKey(sessionId) && !BusTools.isReservedName(message.getSubject())) {
        final byte[] hashBytes = new byte[16];
        random.nextBytes(hashBytes);
        final String messageId = message.getSubject() + SecureHashUtil.hashToHexString(hashBytes);

        deadLetter.put(messageId, new ClusterWaitEntry(System.currentTimeMillis(), message, timeoutCallback));

        clusteringProvider.clusterTransmit(sessionId, message.getSubject(), messageId);
        message.setFlag(RoutingFlag.ClusterWait);
      }
    }
    else if (timeoutCallback != null) {
      timeoutCallback.run();
    }
  }

  private void enqueueForDelivery(final MessageQueue queue, final Message message) {
    try {
      if (queue != null && isAnyoneListening(queue, message.getSubject())) {
        queue.offer(message);
      }
      else {
        if (queue != null && !queue.isInitialized()) {
          deferDelivery(queue, message);
        }
        else {
          delayOrFail(message, new Runnable() {
            @Override
            public void run() {
              enqueueForDelivery(queue, message);
            }
          });
        }
      }
    }
    catch (QueueUnavailableException e) {
      closeQueue(queue);
    }
    catch (IOException e) {
      throw new RuntimeException("failed to enqueue message for delivery", e);
    }
  }

  @SuppressWarnings({"SynchronizationOnLocalVariableOrMethodParameter"})
  private void deferDelivery(final MessageQueue queue, final Message message) {
    synchronized (queue) {
      if (!deferredQueue.containsKey(queue)) deferredQueue.put(queue, new ArrayList<Message>());
      deferredQueue.get(queue).add(message);
    }
  }

  @SuppressWarnings({"SynchronizationOnLocalVariableOrMethodParameter"})
  private void drainDeferredDeliveryQueue(final MessageQueue queue) {
    try {
      synchronized (queue) {
        if (deferredQueue.containsKey(queue)) {
          final List<Message> deferredMessages = deferredQueue.get(queue);
          final Iterator<Message> dmIter = deferredMessages.iterator();

          Message m;
          while (dmIter.hasNext()) {
            if ((m = dmIter.next()).hasPart(MessageParts.PriorityProcessing.toString())) {
              queue.offer(m);
              dmIter.remove();
            }
          }

          for (final Message message : deferredQueue.get(queue)) {
            queue.offer(message);
          }

          deferredQueue.remove(queue);
        }
      }
    }
    catch (IOException e) {
      throw new RuntimeException("error draining deferred delivery queue", e);
    }
  }

  /**
   * Gets the queue corresponding to the session id given
   *
   * @param session
   *     - the session id of the queue
   *
   * @return the message queue
   */
  @Override
  public MessageQueue getQueue(final QueueSession session) {
    return messageQueues.get(session);
  }

  /**
   * Closes the queue with <tt>sessionId</tt>
   *
   * @param sessionId
   *     - the session context of the queue to close
   */
  @Override
  public void closeQueue(final String sessionId) {
    closeQueue(getQueueBySession(sessionId));
  }

  /**
   * Closes the message queue
   *
   * @param queue
   *     - the message queue to close
   */
  @Override
  public void closeQueue(final MessageQueue queue) {
    messageQueues.values().remove(queue);
    sessionLookup.values().remove(queue.getSession());

    for (Iterator<RemoteMessageCallback> iterator = remoteSubscriptions.values().iterator(); iterator.hasNext(); ) {
      final RemoteMessageCallback cb = iterator.next();
      cb.removeQueue(queue);
      if (cb.getQueueCount() == 0) {
        iterator.remove();
      }
    }

    fireQueueCloseListeners(new QueueCloseEvent(queue));
  }

  /**
   * Adds a subscription
   *
   * @param subject
   *     - the subject to subscribe to
   * @param receiver
   *     - the callback function called when a message is dispatched
   */
  @Override
  public Subscription subscribe(final String subject, final MessageCallback receiver) {
    if (reservedNames.contains(subject))
      throw new IllegalArgumentException("cannot modify or subscribe to reserved service: " + subject);

    final DeliveryPlan plan = createOrAddDeliveryPlan(subject, receiver);

    globalSubscriptions.add(subject);

    fireSubscribeListeners(new SubscriptionEvent(false, null, plan.getTotalReceivers(), true, subject));

    return new SubscriptionHandle(subject, receiver, plan);
  }

  @Override
  public Subscription subscribeLocal(final String subject, final MessageCallback receiver) {
    if (reservedNames.contains(subject))
      throw new IllegalArgumentException("cannot modify or subscribe to reserved service: " + subject);

    final String toSubscribe = "local:".concat(subject);

    final DeliveryPlan plan = createOrAddDeliveryPlan(toSubscribe, receiver);

    fireSubscribeListeners(
        new SubscriptionEvent(false, false, true, true, plan.getTotalReceivers(), "InBus", toSubscribe)
    );

    return new LocalSubscriptionHandle(toSubscribe, receiver);
  }

  private DeliveryPlan createOrAddDeliveryPlan(final String subject, final MessageCallback receiver) {
    if (receiver == null) {
      throw new NullPointerException("message callback cannot but null");
    }

    DeliveryPlan plan = subscriptions.get(subject);

    if (plan == null) {
      subscriptions.put(subject, plan = DeliveryPlan.newDeliveryPlan(receiver));
    }
    else {
      subscriptions.put(subject, plan.newDeliveryPlanWith(receiver));
    }

    return plan;
  }

  private DeliveryPlan removeFromDeliveryPlan(final String subject, final MessageCallback receiver) {
    final DeliveryPlan plan = subscriptions.get(subject);

    if (plan != null) {
      subscriptions.put(subject, plan.newDeliveryPlanWithOut(receiver));
      fireUnsubscribeListeners(
          new SubscriptionEvent(false, "InBus", plan.getTotalReceivers(), false, subject));
    }

    return plan;
  }

  private static final Set<String> broadcastExclusionSet = new HashSet<String>() {
    {
      add(BuiltInServices.ClientBus.name());
      add(BuiltInServices.ClientBusErrors.name());
    }
  };

  /**
   * Adds a new remote subscription and fires subscription listeners
   *
   * @param sessionContext
   *     - session context of queue
   * @param queue
   *     - the message queue
   * @param subject
   *     - the subject to subscribe to
   */
  public void remoteSubscribe(final QueueSession sessionContext, final MessageQueue queue, final String subject) {
    if (subject == null) return;

    boolean isNew = false;

    RemoteMessageCallback rmc;
    synchronized (remoteSubscriptions) {
      rmc = remoteSubscriptions.get(subject);
      if (rmc == null) {
        rmc = new RemoteMessageCallback(!broadcastExclusionSet.contains(subject), subject);
        rmc.addQueue(queue);

        isNew = true;

        remoteSubscriptions.put(subject, rmc);
        createOrAddDeliveryPlan(subject, rmc);
      }
      else if (!rmc.contains(queue)) {
        rmc.addQueue(queue);
      }
    }

    fireSubscribeListeners(
        new SubscriptionEvent(true, sessionContext.getSessionId(), rmc.getQueueCount(), isNew, subject)
    );
  }

  public class RemoteMessageCallback implements MessageCallback {

    private final String svc;
    private final Set<MessageQueue> queues = Collections.newSetFromMap(new ConcurrentHashMap<MessageQueue, Boolean>());
    private final boolean broadcastable;

    private final AtomicInteger totalBroadcasted = new AtomicInteger();

    public RemoteMessageCallback(final boolean broadcastable, final String svc) {
      this.broadcastable = broadcastable;
      this.svc = svc;
    }

    @Override
    public void callback(final Message message) {
      // do not pipeline if this message is addressed to a specified session.
      if (broadcastable && !message.isFlagSet(RoutingFlag.NonGlobalRouting) && queues.size() == messageQueues.size()) {
        // all queues are listening to this subject. therefore we can save memory and time by
        // writing to the broadcast color on the buffer

        try {
          if (queues.isEmpty()) return;

          BufferHelper.encodeAndWrite(transmissionbuffer, BufferColor.getAllBuffersColor(), message);

          for (final MessageQueue q : queues) {
            q.wake();
          }

          if (log.isDebugEnabled() && totalBroadcasted.incrementAndGet() % 1000 == 0) {
            log.debug(totalBroadcasted.get() + " messages have been broadcasted to service: " + svc);
          }
        }
        catch (IOException e) {
          throw new RuntimeException("transmission error", e);
        }
      }
      else {
        for (final MessageQueue q : queues) {
          send(q, message, true);
        }
      }

      if (clustering &&
          !message.isFlagSet(RoutingFlag.FromPeer)
          && !message.hasPart(MessageParts.SessionID)
          && !BusTools.isReservedName(message.getSubject())) {

        clusteringProvider.clusterTransmitGlobal(message);
      }
    }

    public void addQueue(final MessageQueue queue) {
      queues.add(queue);
    }

    public void removeQueue(final MessageQueue queue) {
      queues.remove(queue);
    }

    public Collection<MessageQueue> getQueues() {
      return queues;
    }

    public int getQueueCount() {
      return queues.size();
    }

    public boolean contains(final MessageQueue queue) {
      return queues.contains(queue);
    }
  }

  /**
   * Unsubscribes a remote subscription and fires the appropriate listeners
   *
   * @param sessionContext
   *     - session context of queue
   * @param queue
   *     - the message queue
   * @param subject
   *     - the subject to unsubscribe from
   */
  public void remoteUnsubscribe(final QueueSession sessionContext, final MessageQueue queue, final String subject) {
    if (!remoteSubscriptions.containsKey(subject)) {
      return;
    }

    final RemoteMessageCallback rmc = remoteSubscriptions.get(subject);
    rmc.removeQueue(queue);

    try {
      fireUnsubscribeListeners(new SubscriptionEvent(true, rmc.getQueueCount() == 0, false, false, rmc.getQueueCount(),
          sessionContext.getSessionId(), subject));
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Unsubscribe all subscriptions attached to <tt>subject</tt>
   *
   * @param subject
   *     - the subject to unsubscribe from
   */
  @Override
  public void unsubscribeAll(final String subject) {
    if (reservedNames.contains(subject))
      throw new IllegalArgumentException("Attempt to modify lockdown service: " + subject);

    subscriptions.remove(subject);
    globalSubscriptions.remove(subject);

    fireUnsubscribeListeners(new SubscriptionEvent(false, null, 0, false, subject));
  }

  /**
   * Checks if a subscription exists for <tt>subject</tt>
   *
   * @param subject
   *     - the subject to search the subscriptions for
   *
   * @return true if a subscription exists
   */
  @Override
  public boolean isSubscribed(final String subject) {
    return subscriptions.containsKey(subject);
  }

  private boolean isAnyoneListening(final MessageQueue queue, final String subject) {
    return (subject.endsWith(":RespondTo:RPC") || subject.endsWith(":Errors:RPC")
        || subscriptions.containsKey(subject) || (remoteSubscriptions.containsKey(subject)
        && remoteSubscriptions.get(subject).contains(queue)));
  }

  @Override
  public boolean hasRemoteSubscriptions(final String subject) {
    return remoteSubscriptions.containsKey(subject);
  }

  @Override
  public boolean hasRemoteSubscription(final String sessionId, final String subject) {
    return remoteSubscriptions.containsKey(subject) && remoteSubscriptions.get(subject)
        .contains(getQueueBySession(sessionId));
  }

  private void fireSubscribeListeners(final SubscriptionEvent event) {
    if (isMonitor()) {
      busMonitor.notifyNewSubscriptionEvent(event);
    }

    synchronized (subscribeListeners) {
      event.setDisposeListener(false);

      for (Iterator<SubscribeListener> iter = subscribeListeners.iterator(); iter.hasNext(); ) {
        iter.next().onSubscribe(event);
        if (event.isDisposeListener()) {
          iter.remove();
          event.setDisposeListener(false);
        }
      }
    }
  }

  private void fireUnsubscribeListeners(final SubscriptionEvent event) {
    if (isMonitor()) {
      busMonitor.notifyUnSubcriptionEvent(event);
    }

    synchronized (unsubscribeListeners) {
      event.setDisposeListener(false);

      for (Iterator<UnsubscribeListener> iter = unsubscribeListeners.iterator(); iter.hasNext(); ) {
        iter.next().onUnsubscribe(event);
        if (event.isDisposeListener()) {
          iter.remove();
          event.setDisposeListener(false);
        }
      }
    }
  }

  private void fireQueueCloseListeners(final QueueCloseEvent event) {
    if (isMonitor()) {
      busMonitor.notifyQueueDetached(event.getQueue().getSession().getSessionId(), event.getQueue());
    }

    synchronized (queueClosedListeners) {
      event.setDisposeListener(false);

      for (Iterator<QueueClosedListener> iter = queueClosedListeners.iterator(); iter.hasNext(); ) {
        iter.next().onQueueClosed(event);
        if (event.isDisposeListener()) {
          iter.remove();
          event.setDisposeListener(false);
        }
      }
    }
  }

  /**
   * Adds subscription listener
   *
   * @param listener
   *     - subscription listener to add
   */
  @Override
  public void addSubscribeListener(final SubscribeListener listener) {
    synchronized (subscribeListeners) {
      subscribeListeners.add(listener);
    }
  }

  /**
   * Adds unsubscription listener
   *
   * @param listener
   *     - adds an unsubscription listener
   */
  @Override
  public void addUnsubscribeListener(final UnsubscribeListener listener) {
    synchronized (unsubscribeListeners) {
      unsubscribeListeners.add(listener);
    }
  }

  private static QueueSession getSession(final Message message) {
    return message.getResource(QueueSession.class, Resources.Session.name());
  }

  private MessageQueue getQueueByMessage(final Message message) {
    final MessageQueue queue = getQueue(getSession(message));
    if (queue == null) {
      throw new QueueUnavailableException("no queue available to send. (queue or session may have expired): " +
          "(session id: " + getSession(message).getSessionId() + ")");
    }
    else {
      return queue;
    }
  }

  @Override
  public void associateNewQueue(final QueueSession oldSession, final QueueSession newSession) {
    sessionLookup.put(newSession.getSessionId(), oldSession);
    messageQueues.put(newSession, getQueue(oldSession));
  }

  @Override
  public MessageQueue getQueueBySession(final String sessionId) {
    final QueueSession session = sessionLookup.get(sessionId);
    if (session == null) {
      throw new QueueUnavailableException("no queue for sessionId=" + sessionId);
    }
    return getQueue(session);
  }

  @Override
  public QueueSession getSessionBySessionId(final String id) {
    return sessionLookup.get(id);
  }

  /**
   * Gets all the message queues
   *
   * @return a map of the message queues that exist
   */
  @Override
  public Map<QueueSession, MessageQueue> getMessageQueues() {
    return messageQueues;
  }

  /**
   * Gets the scheduler being used within this message bus for housekeeping and
   * other periodic or deferred tasks.
   *
   * @return the scheduler
   */
  @Override
  public ScheduledExecutorService getScheduler() {
    return scheduler;
  }

  @Override
  public void addQueueClosedListener(final QueueClosedListener listener) {
    synchronized (queueClosedListeners) {
      queueClosedListeners.add(listener);
    }
  }

  @Override
  public Collection<MessageCallback> getReceivers(final String subject) {
    return Collections.unmodifiableCollection(subscriptions.get(subject).getDeliverTo());
  }

  private boolean isMonitor() {
    return this.busMonitor != null;
  }

  @Override
  public void attachMonitor(final BusMonitor monitor) {
    if (this.busMonitor != null) {
      log.warn("new monitor attached, but a monitor was already attached: old monitor has been detached.");
    }
    this.busMonitor = monitor;

    for (final Map.Entry<QueueSession, MessageQueue> entry : messageQueues.entrySet()) {
      busMonitor.notifyQueueAttached(entry.getKey().getSessionId(), entry.getValue());
    }

    for (final String subject : subscriptions.keySet()) {
      busMonitor.notifyNewSubscriptionEvent(new SubscriptionEvent(false, "None", 1, false, subject));
    }
    for (final Map.Entry<String, RemoteMessageCallback> entry : remoteSubscriptions.entrySet()) {
      for (final MessageQueue queue : entry.getValue().getQueues()) {
        busMonitor.notifyNewSubscriptionEvent(
            new SubscriptionEvent(true, queue.getSession().getSessionId(), 1, false, entry.getKey())
        );
      }
    }

    monitor.attach(this);
  }

  @Override
  public Message getDeadLetterMessage(final String messageId) {
    final ClusterWaitEntry entry = deadLetter.get(messageId);
    if (entry != null) {
      return entry.getMessage();
    }
    return null;
  }

  @Override
  public boolean removeDeadLetterMessage(final String messageId) {
    return deadLetter.remove(messageId) != null;
  }

  @Override
  public void stop() {
    for (final MessageQueue queue : messageQueues.values()) {
      queue.stopQueue();
    }

    scheduler.shutdown();

    transmissionbuffer.clear();
    subscriptions.clear();
    remoteSubscriptions.clear();
    deferredQueue.clear();
    sessionLookup.clear();
  }

  public void finishInit() {
    reservedNames.addAll(subscriptions.keySet());
  }

  private class ServerBusMessageCallback implements MessageCallback {

    private final String webSocketPath;
    private final int webSocketPort;
    public ServerBusMessageCallback(String webSocketPath, int webSocketPort) {
      this.webSocketPath = webSocketPath;
      this.webSocketPort = webSocketPort;
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public void callback(final Message message) {
      try {
        final QueueSession session = getSession(message);
        MessageQueueImpl queue = (MessageQueueImpl) messageQueues.get(session);

        switch (BusCommand.valueOf(message.getCommandType())) {
          case Heartbeat:
            if (queue != null) {
              queue.heartBeat();
            }
            break;

          case RemoteSubscribe:
            if (queue == null) return;

            if (message.hasPart(MessageParts.SubjectsList)) {
              for (final String subject : (List<String>) message.get(List.class, MessageParts.SubjectsList)) {
                remoteSubscribe(session, queue, subject);
              }
            }
            else if (message.hasPart(MessageParts.RemoteServices)) {
              for (final String subject : message.get(String.class, MessageParts.RemoteServices).split(",")) {
                remoteSubscribe(session, queue, subject);
              }
            }
            else {
              remoteSubscribe(session, messageQueues.get(session),
                  message.get(String.class, MessageParts.Subject));
            }

            break;

          case RemoteUnsubscribe:
            if (queue == null) return;

            remoteUnsubscribe(session, queue,
                message.get(String.class, MessageParts.Subject));
            break;

          case Disconnect:
            if (queue == null) return;

            synchronized (messageQueues) {
              queue.stopQueue();
              closeQueue(queue);
              session.endSession();
            }
            break;

          case Resend:
            if (queue == null) return;

          case Associate: {
            List<Message> deferred = null;
            synchronized (messageQueues) {
              if (messageQueues.containsKey(session)) {
                final MessageQueue q = messageQueues.get(session);
                synchronized (q) {
                  if (deferredQueue.containsKey(q)) {
                    deferred = deferredQueue.remove(q);
                  }
                }

                messageQueues.get(session).stopQueue();
              }

              queue = new MessageQueueImpl(transmissionbuffer, session, messageQueueTimeoutSecs);

              addQueue(session, queue);

              if (deferred != null) {
                deferredQueue.put(queue, deferred);
              }

              remoteSubscribe(session, queue, BuiltInServices.ClientBus.name());
            }

            for (final String svc : message.get(String.class, MessageParts.RemoteServices).split(",")) {
              remoteSubscribe(session, queue, svc);
            }

            if (isMonitor()) {
              busMonitor.notifyQueueAttached(session.getSessionId(), queue);
            }

            final Message msg = ConversationMessage.create(message)
                .toSubject(BuiltInServices.ClientBus.name())
                .command(BusCommand.FinishAssociation);

            final StringBuilder subjects = new StringBuilder();
            for (final String s : new HashSet<String>(globalSubscriptions)) {
              if (subjects.length() != 0) {
                subjects.append(',');
              }
              subjects.append(s);
            }

            msg.set(RemoteServices, subjects.toString());

            final StringBuilder capabilitiesBuffer = new StringBuilder(25);

            final boolean first;
            if (doLongPolling) {
              capabilitiesBuffer.append(Capabilities.LongPolling.name());
              first = false;
            }
            else {
              capabilitiesBuffer.append(Capabilities.ShortPolling.name());
              first = false;
              msg.set(MessageParts.PollFrequency, hostedModeTesting ? 50 : 250);
            }

            if (webSocketServer || webSocketServlet) {
              if (!first) {
                capabilitiesBuffer.append(',');
              }
              capabilitiesBuffer.append(Capabilities.WebSockets.name());
              final String webSocketURL;
              final HttpServletRequest request = message.getResource(HttpServletRequest.class,
                      HttpServletRequest.class.getName());
  
              String websocketScheme = "ws";
              if (request.getScheme().equals("https") || useSecureWebsocket || "https".equalsIgnoreCase(request.getHeader("X-Forwarded-Proto"))) {
                websocketScheme = "wss";
                log.debug("use secure websocket");
              }

              if (webSocketServlet) {
                webSocketURL = websocketScheme + "://" + request.getHeader("Host") + webSocketPath;
              }
              else {
                webSocketURL = websocketScheme + "://" + request.getServerName() + ":" + webSocketPort + webSocketPath;
              }
              msg.set(MessageParts.WebSocketURL, webSocketURL);
              msg.set(MessageParts.WebSocketToken, WebSocketTokenManager.getNewOneTimeToken(session));
            }

            if (sseEnabled && !session.hasAttribute("NoSSE")) {
              capabilitiesBuffer.append(",").append(Capabilities.SSE.name());
            }

            msg.set(MessageParts.CapabilitiesFlags, capabilitiesBuffer.toString());

            msg.set(ConnectionSessionKey, queue.getSession().getSessionId());
            send(msg, false);

            queue.finishInit();
            drainDeferredDeliveryQueue(queue);
            break;
          }

          case WebsocketChannelVerify:
            if (message.hasPart(MessageParts.WebSocketToken)) {
              if (verifyOneTimeToken(session, message.get(String.class, MessageParts.WebSocketToken))) {
                final String reconnectionToken = getNewOneTimeToken(session);

                final LocalContext localContext = LocalContext.get(session);

                localContext.setAttribute(WebSocketServerHandler.SESSION_ATTR_WS_STATUS,
                    WebSocketServerHandler.WEBSOCKET_ACTIVE);

                createConversation(message)
                    .toSubject(BuiltInServices.ClientBus.name())
                    .command(BusCommand.WebsocketChannelOpen)
                    .with(MessageParts.WebSocketToken, reconnectionToken)
                    .done().sendNowWith(ServerMessageBusImpl.this, false);
              }
              else {
              }
            }
            break;
        }
      }
      catch (Throwable t) {
        t.printStackTrace();
      }
    }
  }

  private BufferStatus bufferStatus() {
    final int headBytes = transmissionbuffer.getHeadPositionBytes();
    final int bufSize = transmissionbuffer.getBufferSize();

    long lowTail = -1;
    long highTail = -1;
    int activeTails = 0;

    final int free;
    long lowSegBytes = 0;
    long highSegBytes = 0;


    for (final MessageQueue q : messageQueues.values()) {
      activeTails++;
      final long seq = q.getCurrentBufferSequenceNumber();
      if (lowTail == -1) {
        lowTail = highTail = seq;
      }
      else {
        if (seq > highTail) highTail = seq;
        if (seq < lowTail) lowTail = seq;
      }
    }

    if (activeTails > 0) {
      lowSegBytes = (lowTail % transmissionbuffer.getBufferSize()) * transmissionbuffer.getSegmentSize();
      highSegBytes = (highTail % transmissionbuffer.getBufferSize()) * transmissionbuffer.getSegmentSize();

      if (lowSegBytes < headBytes) {
        free = (int) ((bufSize - headBytes) + lowSegBytes);
      }
      else if (lowSegBytes > headBytes) {
        free = (int) (lowSegBytes - bufSize);
      }
      else {
        free = bufSize;
      }
    }
    else {
      free = bufSize;
    }

    return new BufferStatus(free, (int) (highSegBytes - lowSegBytes), activeTails, ((float) free) / bufSize);
  }

  private class DefaultSubscribeListener implements SubscribeListener {
    @Override
    public void onSubscribe(final SubscriptionEvent event) {
      if (event.isLocalOnly() || event.isRemote() || event.getSubject().startsWith("local:")) return;

      MessageBuilder.createMessage()
          .toSubject(BuiltInServices.ClientBus.name())
          .command(BusCommand.RemoteSubscribe)
          .with(MessageParts.Subject, event.getSubject())
          .noErrorHandling().sendGlobalWith(ServerMessageBusImpl.this);

    }
  }

  private class DefaultUnsubscribeListener implements UnsubscribeListener {
    @Override
    public void onUnsubscribe(final SubscriptionEvent event) {
      if (event.isLocalOnly() || event.isRemote() || event.getSubject().startsWith("local:")) return;
      if (messageQueues.isEmpty()) return;

      MessageBuilder.createMessage()
          .toSubject(BuiltInServices.ClientBus.name())
          .command(BusCommand.RemoteUnsubscribe)
          .with(MessageParts.Subject, event.getSubject())
          .noErrorHandling().sendGlobalWith(ServerMessageBusImpl.this);
    }
  }

  private class HousekeeeperRunnable implements Runnable {
    int runCount = 0;
    boolean lastWasEmpty = false;

    @Override
    public void run() {
      runCount++;
      boolean houseKeepingPerformed = false;
      final List<MessageQueue> endSessions = new LinkedList<MessageQueue>();

      int paged = 0, killed = 0;

      while (!houseKeepingPerformed) {
        try {
          final Iterator<MessageQueue> iter = ServerMessageBusImpl.this.messageQueues.values().iterator();
          MessageQueue q;
          while (iter.hasNext()) {
            if ((q = iter.next()).isStale()) {
              iter.remove();
              endSessions.add(q);
              killed++;
            }

            if (PageUtil.pageIfStraddling(q)) {
              paged++;
            }
          }

          houseKeepingPerformed = true;
        }
        catch (ConcurrentModificationException cme) {
          // fall-through and try again.
        }
      }

      if (paged > 0 || killed > 0) {
        log.debug("[bus] killed " + killed + " sessions and paged out " + paged + " queues");
      }

      for (final MessageQueue ref : endSessions) {
        for (final String subject : new HashSet<String>(ServerMessageBusImpl.this.remoteSubscriptions.keySet())) {
          ServerMessageBusImpl.this.remoteUnsubscribe(ref.getSession(), ref, subject);
        }

        ServerMessageBusImpl.this.closeQueue(ref);
        ref.getSession().endSession();
        deferredQueue.remove(ref);
        ref.discard();
      }

      final Iterator<ClusterWaitEntry> entryIterator = deadLetter.values().iterator();

      while (entryIterator.hasNext()) {
        final ClusterWaitEntry entry = entryIterator.next();
        if (entry.isStale()) {
          entryIterator.remove();
          try {
            entry.notifyTimeout();
          }
          catch (Exception e) {
            log.warn("exception occurred expunging from dead letter queue", e);
          }
        }
      }

      final BufferStatus stat = bufferStatus();
      if (stat.getFree() == 1.0f) {
        if (lastWasEmpty) {
          return;
        }
        else {
          lastWasEmpty = true;
        }
      }
      else {
        lastWasEmpty = false;
      }

      log.debug("[bus] buffer status [freebytes: " + stat.getFreeBytes()
          + " (" + (stat.getFree() * 100) + "%) tail rng: " + stat.getTailRange() + "; actv tails: "
          + stat.getActiveTails() + "]");

      if (stat.getFree() < 0.50f) {
        log.debug("[bus] high load condition detected!");
      }
    }

    @Override
    public String toString() {
      return "Bus Housekeeper";
    }
  }

  private class LocalSubscriptionHandle implements Subscription {
    private final String toSubscribe;
    private final MessageCallback receiver;

    public LocalSubscriptionHandle(String toSubscribe, MessageCallback receiver) {
      this.toSubscribe = toSubscribe;
      this.receiver = receiver;
    }

    @Override
    public void remove() {
      removeFromDeliveryPlan(toSubscribe, receiver);
    }
  }

  private class SubscriptionHandle implements Subscription {
    private final String subject;
    private final MessageCallback receiver;
    private final DeliveryPlan plan;

    public SubscriptionHandle(String subject, MessageCallback receiver, DeliveryPlan plan) {
      this.subject = subject;
      this.receiver = receiver;
      this.plan = plan;
    }

    @Override
    public void remove() {
      if (removeFromDeliveryPlan(subject, receiver).getTotalReceivers() == 0) {
        globalSubscriptions.remove(subject);
        subscriptions.remove(subject);
      }
      else {
        boolean nonRemote = true;
        for (final MessageCallback callback : plan.getDeliverTo()) {
          if (!(callback instanceof RemoteMessageCallback)) {
            nonRemote = false;
            break;
          }
        }
        if (nonRemote) {
          globalSubscriptions.remove(subject);
          subscriptions.remove(subject);
        }
      }
    }
  }
}
