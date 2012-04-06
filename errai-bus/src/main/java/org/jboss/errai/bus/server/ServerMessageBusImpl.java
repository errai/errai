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

package org.jboss.errai.bus.server;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.api.MessageCallback;
import org.jboss.errai.bus.client.api.MessageListener;
import org.jboss.errai.bus.client.api.QueueSession;
import org.jboss.errai.bus.client.api.SubscribeListener;
import org.jboss.errai.bus.client.api.UnsubscribeListener;
import org.jboss.errai.bus.client.api.base.Capabilities;
import org.jboss.errai.bus.client.api.base.CommandMessage;
import org.jboss.errai.bus.client.api.base.ConversationMessage;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.api.base.NoSubscribersToDeliverTo;
import org.jboss.errai.bus.client.api.base.RuleDelegateMessageCallback;
import org.jboss.errai.bus.client.framework.BooleanRoutingRule;
import org.jboss.errai.bus.client.framework.BuiltInServices;
import org.jboss.errai.bus.client.framework.BusMonitor;
import org.jboss.errai.bus.client.framework.DeliveryPlan;
import org.jboss.errai.bus.client.framework.RoutingFlag;
import org.jboss.errai.bus.client.framework.Subscription;
import org.jboss.errai.bus.client.framework.SubscriptionEvent;
import org.jboss.errai.bus.client.protocols.BusCommands;
import org.jboss.errai.bus.server.api.MessageQueue;
import org.jboss.errai.bus.server.api.QueueCloseEvent;
import org.jboss.errai.bus.server.api.QueueClosedListener;
import org.jboss.errai.bus.server.api.ServerMessageBus;
import org.jboss.errai.bus.server.io.BufferHelper;
import org.jboss.errai.bus.server.io.IOConfigAttribs;
import org.jboss.errai.bus.server.io.buffers.BufferColor;
import org.jboss.errai.bus.server.io.buffers.TransmissionBuffer;
import org.jboss.errai.bus.server.io.websockets.WebSocketServer;
import org.jboss.errai.bus.server.io.websockets.WebSocketServerHandler;
import org.jboss.errai.bus.server.service.ErraiServiceConfigurator;
import org.jboss.errai.bus.server.util.SecureHashUtil;
import org.jboss.errai.common.client.protocols.MessageParts;
import org.jboss.errai.common.client.protocols.Resources;
import org.slf4j.Logger;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.jboss.errai.bus.client.api.base.MessageBuilder.createConversation;
import static org.jboss.errai.bus.client.protocols.SecurityCommands.MessageNotDelivered;
import static org.jboss.errai.bus.client.util.ErrorHelper.handleMessageDeliveryFailure;
import static org.jboss.errai.common.client.protocols.MessageParts.ReplyTo;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * The <tt>ServerMessageBusImpl</tt> implements the <tt>ServerMessageBus</tt>, making it possible for the server to
 * send and receive messages
 *
 * @author Mike Brock
 */
@Singleton
public class ServerMessageBusImpl implements ServerMessageBus {

  private final List<MessageListener> listeners = new ArrayList<MessageListener>();

  // 16 kb buffers * 8192 segments = 128 megabytes
  private final TransmissionBuffer transmissionbuffer;

  private final Map<String, DeliveryPlan> subscriptions = new ConcurrentHashMap<String, DeliveryPlan>();
  private final Map<String, RemoteMessageCallback> remoteSubscriptions = new ConcurrentHashMap<String, RemoteMessageCallback>();

  private final Map<QueueSession, MessageQueue> messageQueues = new ConcurrentHashMap<QueueSession, MessageQueue>();

  private final Map<MessageQueue, List<Message>> deferredQueue = new ConcurrentHashMap<MessageQueue, List<Message>>();
  private final Map<String, QueueSession> sessionLookup = new ConcurrentHashMap<String, QueueSession>();

  private final List<SubscribeListener> subscribeListeners = new LinkedList<SubscribeListener>();
  private final List<UnsubscribeListener> unsubscribeListeners = new LinkedList<UnsubscribeListener>();
  private final List<QueueClosedListener> queueClosedListeners = new LinkedList<QueueClosedListener>();

  private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

  private Logger log = getLogger(getClass());

  private BusMonitor busMonitor;

  private Set<String> reservedNames = new HashSet<String>();

  private final boolean webSocketServlet;
  private final boolean webSocketServer;

  /**
   * Sets up the <tt>ServerMessageBusImpl</tt> with the configuration supplied. Also, initializes the bus' callback
   * functions, scheduler, and monitor
   * <p/>
   * When deploying services on the server-side, it is possible to obtain references to the
   * <tt>ErraiServiceConfigurator</tt> by declaring it as injection dependencies
   */
  @Inject
  public ServerMessageBusImpl(final ErraiServiceConfigurator config) {
    this.webSocketServer = config
            .getBooleanProperty(ErraiServiceConfigurator.ENABLE_WEB_SOCKET_SERVER);

    final int webSocketPort;
    final String webSocketPath;

    webSocketServlet = Boolean.getBoolean("org.jboss.errai.websocket_servlet");

    webSocketPort = WebSocketServer.getWebSocketPort(config);
    webSocketPath = config.hasProperty(ErraiServiceConfigurator.WEB_SOCKET_URL) ?
            config.getProperty(ErraiServiceConfigurator.WEB_SOCKET_URL) :
            WebSocketServerHandler.WEBSOCKET_PATH;

    Integer bufferSize = config.getIntProperty(IOConfigAttribs.BUS_BUFFER_SIZE);
    Integer segmentSize = config.getIntProperty(IOConfigAttribs.BUS_BUFFER_SEGMENT_SIZE);
    Integer segmentCount = config.getIntProperty(IOConfigAttribs.BUS_BUFFER_SEGMENT_COUNT);
    String allocMode = config.getProperty(IOConfigAttribs.BUF_BUFFER_ALLOCATION_MODE);

    if (segmentSize == null) {
      segmentSize = 8 * 1024;
    }
    else {
      segmentSize = segmentSize * 1024;
    }

    if (bufferSize != null) {
      segmentCount = (bufferSize * 1024 * 1024) * segmentSize;
    }

    else if (segmentCount == null) {
      segmentCount = 16384;
    }

    boolean directAlloc;
    if (allocMode != null) {
      if ("direct".equals(allocMode)) {
        directAlloc = true;
      }
      else if ("heap".equals(allocMode)) {
        directAlloc = false;
      }
      else {
        throw new ErraiBootstrapFailure("unrecognized option for property: "
                + IOConfigAttribs.BUF_BUFFER_ALLOCATION_MODE);
      }
    }
    else {
      directAlloc = false;
    }

    if (directAlloc) {
      transmissionbuffer = TransmissionBuffer.createDirect(segmentSize, segmentCount);
    }
    else {
      transmissionbuffer = TransmissionBuffer.create(segmentSize, segmentCount);
    }

    /**
     * Define the default ServerBus service used for intrabus communication.
     */
    subscribe(BuiltInServices.ServerBus.name(), new MessageCallback() {
      @Override
      @SuppressWarnings({"unchecked", "SynchronizationOnLocalVariableOrMethodParameter"})
      public void callback(Message message) {
        try {
          final QueueSession session = getSession(message);
          MessageQueueImpl queue = (MessageQueueImpl) messageQueues.get(session);

          switch (BusCommands.valueOf(message.getCommandType())) {
            case Heartbeat:
              if (queue != null) {
                queue.heartBeat();
              }
              break;

            case RemoteSubscribe:
              if (queue == null) return;

              if (message.hasPart(MessageParts.SubjectsList)) {
                for (String subject : (List<String>) message.get(List.class, MessageParts.SubjectsList)) {
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

            case FinishStateSync:
              if (queue == null) return;
              queue.finishInit();

              drainDeferredDeliveryQueue(queue);
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

            case ConnectToQueue: {
              List<Message> deferred = null;
              synchronized (messageQueues) {
                if (messageQueues.containsKey(session)) {
                  MessageQueue q = messageQueues.get(session);
                  synchronized (q) {
                    if (deferredQueue.containsKey(q)) {
                      deferred = deferredQueue.remove(q);
                    }
                  }

                  messageQueues.get(session).stopQueue();
                }

                queue = new MessageQueueImpl(transmissionbuffer, session);

                addQueue(session, queue);

                if (deferred != null) {
                  deferredQueue.put(queue, deferred);
                }

                remoteSubscribe(session, queue, BuiltInServices.ClientBus.name());
              }

              if (isMonitor()) {
                busMonitor.notifyQueueAttached(session.getSessionId(), queue);
              }

              final List<String> subjects = new ArrayList<String>(subscriptions.size());
              for (String service : subscriptions.keySet()) {
                if (service.startsWith("local:")) {
                }
                else if (!remoteSubscriptions.containsKey(service)) {
                  subjects.add(service);
                }
              }

              createConversation(message)
                      .toSubject(BuiltInServices.ClientBus.name())
                      .command(BusCommands.RemoteSubscribe)
                      .with(MessageParts.SubjectsList, subjects)
                      .with(MessageParts.PriorityProcessing, "1")
                      .noErrorHandling().sendNowWith(ServerMessageBusImpl.this, false);

              final CommandMessage msg = ConversationMessage.create(message);
              msg.toSubject(BuiltInServices.ClientBus.name())
                      .command(BusCommands.CapabilitiesNotice);

              StringBuilder capabilitiesBuffer = new StringBuilder(25);

              boolean first;
              if (ErraiServiceConfigurator.LONG_POLLING) {
                capabilitiesBuffer.append(Capabilities.LongPollAvailable.name());
                first = false;
              }
              else {
                capabilitiesBuffer.append(Capabilities.NoLongPollAvailable.name());
                first = false;
                msg.set(MessageParts.PollFrequency, ErraiServiceConfigurator.HOSTED_MODE_TESTING ? 50 : 250);
              }

              if (webSocketServer || webSocketServlet) {
                if (!first) {
                  capabilitiesBuffer.append(',');
                }
                capabilitiesBuffer.append(Capabilities.WebSockets.name());
                /**
                 * Advertise where the client can find a websocket.
                 */
                HttpServletRequest request = message.getResource(HttpServletRequest.class, HttpServletRequest.class.getName());
                msg.set(MessageParts.WebSocketURL, "ws://" + request.getLocalAddr()
                        + ":" + webSocketPort + webSocketPath);

                String connectionToken = SecureHashUtil.nextSecureHash("SHA-256", session.getSessionId());
                session.setAttribute(MessageParts.WebSocketToken.name(), connectionToken);
                msg.set(MessageParts.WebSocketToken, connectionToken);
              }

              msg.set(MessageParts.CapabilitiesFlags, capabilitiesBuffer.toString());

              send(msg, false);

              createConversation(message)
                      .toSubject(BuiltInServices.ClientBus.name())
                      .command(BusCommands.FinishStateSync)
                      .with(MessageParts.ConnectionSessionKey, queue.getSession().getSessionId())
                      .noErrorHandling().sendNowWith(ServerMessageBusImpl.this, false);

              break;
            }

            case WebsocketChannelVerify:
              if (session.hasAttribute(MessageParts.WebSocketToken.name())
                      && message.hasPart(MessageParts.WebSocketToken)) {

                if (message.get(String.class, MessageParts.WebSocketToken)
                        .equals(session.getAttribute(String.class, MessageParts.WebSocketToken.name()))) {

                  session.setAttribute(WebSocketServerHandler.SESSION_ATTR_WS_STATUS,
                          WebSocketServerHandler.WEBSOCKET_ACTIVE);

                  createConversation(message)
                          .toSubject(BuiltInServices.ClientBus.name())
                          .command(BusCommands.WebsocketChannelOpen)
                          .done().sendNowWith(ServerMessageBusImpl.this, false);
                }
              }

              break;
          }

        }
        catch (Throwable t) {
          t.printStackTrace();
        }
      }
    });

    addSubscribeListener(new SubscribeListener() {
      @Override
      public void onSubscribe(SubscriptionEvent event) {
        if (event.isLocalOnly() || event.isRemote() || event.getSubject().startsWith("local:")) return;

        MessageBuilder.createMessage()
                .toSubject(BuiltInServices.ClientBus.name())
                .command(BusCommands.RemoteSubscribe)
                .with(MessageParts.Subject, event.getSubject())
                .noErrorHandling().sendGlobalWith(ServerMessageBusImpl.this);

      }
    });

    addUnsubscribeListener(new UnsubscribeListener() {
      @Override
      public void onUnsubscribe(SubscriptionEvent event) {
        if (event.isLocalOnly() || event.isRemote() || event.getSubject().startsWith("local:")) return;
        if (messageQueues.isEmpty()) return;

        MessageBuilder.createMessage()
                .toSubject(BuiltInServices.ClientBus.name())
                .command(BusCommands.RemoteUnsubscribe)
                .with(MessageParts.Subject, event.getSubject())
                .noErrorHandling().sendGlobalWith(ServerMessageBusImpl.this);
      }
    });

    scheduler.scheduleAtFixedRate(new Runnable() {
      int runCount = 0;
      boolean lastWasEmpty = false;

      @Override
      public void run() {
        runCount++;
        boolean houseKeepingPerformed = false;
        List<MessageQueue> endSessions = new LinkedList<MessageQueue>();

        int paged = 0, killed = 0;

        while (!houseKeepingPerformed) {
          try {
            Iterator<MessageQueue> iter = ServerMessageBusImpl.this.messageQueues.values().iterator();
            MessageQueue q;
            while (iter.hasNext()) {
              if ((q = iter.next()).isStale()) {
                iter.remove();
                endSessions.add(q);
                killed++;
              }
              else if (q.isDowngradeCandidate()) {
                if (!q.pageWaitingToDisk()) {
                  paged++;
                }
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


        for (MessageQueue ref : endSessions) {
          for (String subject : new HashSet<String>(ServerMessageBusImpl.this.remoteSubscriptions.keySet())) {
            ServerMessageBusImpl.this.remoteUnsubscribe(ref.getSession(), ref, subject);
          }

          ServerMessageBusImpl.this.closeQueue(ref);
          ref.getSession().endSession();
          deferredQueue.remove(ref);
          ref.discard();
        }

        BufferStatus stat = bufferStatus();
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
    }, 8, 8, TimeUnit.SECONDS);
  }

  private static class BufferStatus {
    private int freeBytes;
    private int tailRange;
    private int activeTails;
    private float free;

    private BufferStatus(int freeBytes, int tailRange, int activeTails, float free) {
      this.freeBytes = freeBytes;
      this.tailRange = tailRange;
      this.activeTails = activeTails;
      this.free = free;
    }

    public int getFreeBytes() {
      return freeBytes;
    }

    public int getTailRange() {
      return tailRange;
    }

    public int getActiveTails() {
      return activeTails;
    }

    public float getFree() {
      return free;
    }
  }

  private BufferStatus bufferStatus() {
    int headBytes = transmissionbuffer.getHeadPositionBytes();
    int bufSize = transmissionbuffer.getBufferSize();

    long lowTail = -1;
    long highTail = -1;
    int activeTails = 0;

    int free;
    long lowSegBytes = 0;
    long highSegBytes = 0;


    for (MessageQueue q : messageQueues.values()) {
      activeTails++;
      long seq = q.getCurrentBufferSequenceNumber();
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

  private void addQueue(QueueSession session, MessageQueue queue) {
    messageQueues.put(session, queue);
    sessionLookup.put(session.getSessionId(), session);
  }

  /**
   * Configures the server message bus with the specified <tt>ErraiServiceConfigurator</tt>.
   * Presently there are no configurable parameters.
   */
  @Override
  public void configure(ErraiServiceConfigurator config) {
    // no configuration in current implementation
  }

  /**
   * Sends a message globally to all subscriptions containing the same subject as the specified message.
   *
   * @param message - The message to be sent.
   */
  @Override
  public void sendGlobal(final Message message) {
    message.commit();
    final String subject = message.getSubject();

    if (!subscriptions.containsKey(subject) && !remoteSubscriptions.containsKey(subject)) {
      delayOrFail(message, new Runnable() {
        @Override
        public void run() {
          sendGlobal(message);
        }
      });

      return;
    }

    if (!fireGlobalMessageListeners(message)) {
      if (message.hasPart(ReplyTo) && message.hasResource(Resources.Session.name())) {
        /**
         * Inform the sender that we did not dispatchGlobal the message.
         */

        Map<String, Object> rawMsg = new HashMap<String, Object>();
        rawMsg.put(MessageParts.CommandType.name(), MessageNotDelivered.name());

        try {
          enqueueForDelivery(getQueueByMessage(message), CommandMessage.createWithParts(rawMsg));
        }
        catch (NoSubscribersToDeliverTo nstdt) {
          handleMessageDeliveryFailure(this, message, "No subscribers to deliver to", nstdt, false);
        }
      }

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

  private void delayOrFail(Message message, final Runnable deliveryTaskRunnable) {
    if (message.isFlagSet(RoutingFlag.RetryDelivery)
            && message.getResource(Integer.class, Resources.RetryAttempts.name()) > 3) {
      NoSubscribersToDeliverTo ntdt = new NoSubscribersToDeliverTo(message.getSubject());
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
   * @param message - the message to send
   */
  @Override
  public void send(Message message) {
    message.commit();
    if (message.hasResource(Resources.Session.name())) {
      message.setFlag(RoutingFlag.NonGlobalRouting);
      send(getQueueByMessage(message), message, true);
    }
    else if (message.hasPart(MessageParts.SessionID)) {
      message.setFlag(RoutingFlag.NonGlobalRouting);
      send(getQueueBySession(message.get(String.class, MessageParts.SessionID)), message, true);
    }
    else {
      sendGlobal(message);
    }
  }

  /**
   * Parses the message appropriately and enqueues it for delivery
   *
   * @param message       - the message to be sent
   * @param fireListeners - true if all listeners attached should be notified of delivery
   */
  @Override
  public void send(Message message, boolean fireListeners) {
    message.commit();
    if (!message.hasResource(Resources.Session.name())) {
      handleMessageDeliveryFailure(this, message, "cannot automatically route message. no session contained in message.", null, false);
    }

    final MessageQueue queue = getQueue(getSession(message));

    if (queue == null) {
      handleMessageDeliveryFailure(this, message, "cannot automatically route message. no session contained in message.", null, false);
    }

    send(message.hasPart(MessageParts.SessionID) ? getQueueBySession(message.get(String.class, MessageParts.SessionID)) :
            getQueueByMessage(message), message, fireListeners);
  }

  private void send(MessageQueue queue, Message message, boolean fireListeners) {
    try {
      if (fireListeners && !fireGlobalMessageListeners(message)) {
        if (message.hasPart(ReplyTo)) {
          Map<String, Object> rawMsg = new HashMap<String, Object>();
          rawMsg.put(MessageParts.CommandType.name(), MessageNotDelivered.name());
          enqueueForDelivery(queue, CommandMessage.createWithParts(rawMsg));
        }
        return;
      }

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
  private void deferDelivery(final MessageQueue queue, Message message) {
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
          List<Message> deferredMessages = deferredQueue.get(queue);
          Iterator<Message> dmIter = deferredMessages.iterator();

          Message m;
          while (dmIter.hasNext()) {
            if ((m = dmIter.next()).hasPart(MessageParts.PriorityProcessing.toString())) {
              queue.offer(m);
              dmIter.remove();
            }
          }

          for (Message message : deferredQueue.get(queue)) {
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
   * @param session - the session id of the queue
   * @return the message queue
   */
  @Override
  public MessageQueue getQueue(QueueSession session) {
    return messageQueues.get(session);
  }

  /**
   * Closes the queue with <tt>sessionId</tt>
   *
   * @param sessionId - the session context of the queue to close
   */
  @Override
  public void closeQueue(String sessionId) {
    closeQueue(getQueueBySession(sessionId));
  }

  /**
   * Closes the message queue
   *
   * @param queue - the message queue to close
   */
  @Override
  public void closeQueue(MessageQueue queue) {
    messageQueues.values().remove(queue);
    sessionLookup.values().remove(queue.getSession());

    for (RemoteMessageCallback cb : remoteSubscriptions.values()) {
      cb.removeQueue(queue);
    }

    fireQueueCloseListeners(new QueueCloseEvent(queue));
  }

  /**
   * Adds a rule for a specific subscription. The <tt>BooleanRoutingRule</tt> determines if a message should
   * be routed based on the already specified rules or not.
   *
   * @param subject - the subject of the subscription
   * @param rule    - the <tt>BooleanRoutingRule</tt> instance specifying the routing rules
   */
  @Override
  public void addRule(String subject, BooleanRoutingRule rule) {
    DeliveryPlan plan = subscriptions.get(subject);
    if (plan == null) {
      throw new RuntimeException("no such subject: " + subject);
    }

    subscriptions.put(subject, new RuleDelegateMessageCallback(plan, rule));
  }

  /**
   * Adds a subscription
   *
   * @param subject  - the subject to subscribe to
   * @param receiver - the callback function called when a message is dispatched
   */
  @Override
  public Subscription subscribe(final String subject, final MessageCallback receiver) {
    if (reservedNames.contains(subject))
      throw new IllegalArgumentException("cannot modify or subscribe to reserved service: " + subject);

    DeliveryPlan plan = createOrAddDeliveryPlan(subject, receiver);

    fireSubscribeListeners(new SubscriptionEvent(false, null, plan.getTotalReceivers(), true, subject));

    return new Subscription() {
      @Override
      public void remove() {
        removeFromDeliveryPlan(subject, receiver);
      }
    };
  }

  @Override
  public Subscription subscribeLocal(final String subject, final MessageCallback receiver) {
    if (reservedNames.contains(subject))
      throw new IllegalArgumentException("cannot modify or subscribe to reserved service: " + subject);

    final String toSubscribe = "local:".concat(subject);

    DeliveryPlan plan = createOrAddDeliveryPlan(toSubscribe, receiver);

    fireSubscribeListeners(new SubscriptionEvent(false, false, true, true, plan.getTotalReceivers(), "InBus", toSubscribe));

    return new Subscription() {
      @Override
      public void remove() {
        removeFromDeliveryPlan(toSubscribe, receiver);
      }
    };
  }

  private DeliveryPlan createOrAddDeliveryPlan(final String subject, final MessageCallback receiver) {
    DeliveryPlan plan = subscriptions.get(subject);

    if (plan == null) {
      subscriptions.put(subject, plan = new DeliveryPlan(new MessageCallback[]{receiver}));
    }
    else {
      subscriptions.put(subject, plan.newDeliveryPlanWith(receiver));
    }

    return plan;
  }

  private DeliveryPlan removeFromDeliveryPlan(final String subject, final MessageCallback receiver) {
    DeliveryPlan plan = subscriptions.get(subject);

    if (plan != null) {
      subscriptions.put(subject, plan = plan.newDeliveryPlanWithOut(receiver));
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
   * @param sessionContext - session context of queue
   * @param queue          - the message queue
   * @param subject        - the subject to subscribe to
   */
  public void remoteSubscribe(QueueSession sessionContext, MessageQueue queue, String subject) {
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

    fireSubscribeListeners(new SubscriptionEvent(true, sessionContext.getSessionId(), rmc.getQueueCount(), isNew, subject));
  }

  public class RemoteMessageCallback implements MessageCallback {
    private final String svc;
    private final Set<MessageQueue> queues = Collections.newSetFromMap(new ConcurrentHashMap<MessageQueue, Boolean>());

    private final boolean broadcastable;
    private final AtomicInteger totalBroadcasted = new AtomicInteger();

    public RemoteMessageCallback(boolean broadcastable, String svc) {
      this.broadcastable = broadcastable;
      this.svc = svc;
    }

    @Override
    public void callback(Message message) {
      // do not pipeline if this message is addressed to a specified session.
      if (broadcastable && !message.isFlagSet(RoutingFlag.NonGlobalRouting)) {
        // all queues are listening to this subject. therefore we can save memory and time by
        // writing to the broadcast color on the buffer
        try {

          BufferHelper.encodeAndWrite(transmissionbuffer, BufferColor.getAllBuffersColor(), message);

          for (MessageQueue q : queues) {
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
        for (MessageQueue q : queues) {
          send(q, message, true);
        }
      }
    }

    public void addQueue(MessageQueue queue) {
      queues.add(queue);
    }

    public void removeQueue(MessageQueue queue) {
      queues.remove(queue);
    }

    public Collection<MessageQueue> getQueues() {
      return queues;
    }

    public int getQueueCount() {
      return queues.size();
    }

    public boolean contains(MessageQueue queue) {
      return queues.contains(queue);
    }
  }

  /**
   * Unsubscribes a remote subsciption and fires the appropriate listeners
   *
   * @param sessionContext - session context of queue
   * @param queue          - the message queue
   * @param subject        - the subject to unsubscribe from
   */
  public void remoteUnsubscribe(QueueSession sessionContext, MessageQueue queue, String subject) {
    if (!remoteSubscriptions.containsKey(subject)) {
      return;
    }

    RemoteMessageCallback rmc = remoteSubscriptions.get(subject);
    rmc.removeQueue(queue);

    try {
      fireUnsubscribeListeners(new SubscriptionEvent(true, rmc.getQueueCount() == 0, false, false, rmc.getQueueCount(),
              sessionContext.getSessionId(), subject));
    }
    catch (Exception e) {
      e.printStackTrace();
      System.out.println("Exception running listeners");
    }
  }

  /**
   * Unsubscribe all subscriptions attached to <tt>subject</tt>
   *
   * @param subject - the subject to unsubscribe from
   */
  @Override
  public void unsubscribeAll(String subject) {
    if (reservedNames.contains(subject))
      throw new IllegalArgumentException("Attempt to modify lockdown service: " + subject);

    subscriptions.remove(subject);

    fireUnsubscribeListeners(new SubscriptionEvent(false, null, 0, false, subject));
  }

  /**
   * Checks if a subscription exists for <tt>subject</tt>
   *
   * @param subject - the subject to search the subscriptions for
   * @return true if a subscription exists
   */
  @Override
  public boolean isSubscribed(String subject) {
    return subscriptions.containsKey(subject);
  }

  private boolean isAnyoneListening(MessageQueue queue, String subject) {
    return subscriptions.containsKey(subject) ||
            (remoteSubscriptions.containsKey(subject) && remoteSubscriptions.get(subject).contains(queue));
  }

  @Override
  public boolean hasRemoteSubscriptions(String subject) {
    return remoteSubscriptions.containsKey(subject);
  }

  @Override
  public boolean hasRemoteSubscription(String sessionId, String subject) {
    MessageQueue q = getQueueBySession(sessionId);
    return remoteSubscriptions.containsKey(subject) && remoteSubscriptions.get(subject)
            .contains(q);
  }


  private boolean fireGlobalMessageListeners(Message message) {
    boolean allowContinue = true;

    for (MessageListener listener : listeners) {
      if (!listener.handleMessage(message)) {
        allowContinue = false;
      }
    }

    return allowContinue;
  }

  private void fireSubscribeListeners(SubscriptionEvent event) {
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

  private void fireUnsubscribeListeners(SubscriptionEvent event) {
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

  private void fireQueueCloseListeners(QueueCloseEvent event) {
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
   * Adds a global listener
   *
   * @param listener - global listener to add
   */
  @Override
  public void addGlobalListener(MessageListener listener) {
    synchronized (listeners) {
      listeners.add(listener);
    }
  }

  /**
   * Adds subscription listener
   *
   * @param listener - subscription listener to add
   */
  @Override
  public void addSubscribeListener(SubscribeListener listener) {
    synchronized (subscribeListeners) {
      subscribeListeners.add(listener);
    }
  }

  /**
   * Adds unsubscription listener
   *
   * @param listener - adds an unsubscription listener
   */
  @Override
  public void addUnsubscribeListener(UnsubscribeListener listener) {
    synchronized (unsubscribeListeners) {
      unsubscribeListeners.add(listener);
    }
  }

  private static QueueSession getSession(Message message) {
    return message.getResource(QueueSession.class, Resources.Session.name());
  }

  private MessageQueue getQueueByMessage(Message message) {
    MessageQueue queue = getQueue(getSession(message));
    if (queue == null) {
      throw new QueueUnavailableException("no queue available to send. (queue or session may have expired): " +
              "(session id: " + getSession(message).getSessionId() + ")");
    }
    else {
      return queue;
    }
  }

  @Override
  public MessageQueue getQueueBySession(String sessionId) {
    return getQueue(sessionLookup.get(sessionId));
  }

  @Override
  public QueueSession getSessionBySessionId(String id) {
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
  public void addQueueClosedListener(QueueClosedListener listener) {
    synchronized (queueClosedListeners) {
      queueClosedListeners.add(listener);
    }
  }

  @Override
  public List<MessageCallback> getReceivers(String subject) {
    return Collections.unmodifiableList(Arrays.asList(subscriptions.get(subject).getDeliverTo()));
  }

  private boolean isMonitor() {
    return this.busMonitor != null;
  }

  @Override
  public void attachMonitor(BusMonitor monitor) {
    if (this.busMonitor != null) {
      log.warn("new monitor attached, but a monitor was already attached: old monitor has been detached.");
    }
    this.busMonitor = monitor;

    for (Map.Entry<QueueSession, MessageQueue> entry : messageQueues.entrySet()) {
      busMonitor.notifyQueueAttached(entry.getKey().getSessionId(), entry.getValue());
    }

    for (String subject : subscriptions.keySet()) {
      busMonitor.notifyNewSubscriptionEvent(new SubscriptionEvent(false, "None", 1, false, subject));
    }
    for (Map.Entry<String, RemoteMessageCallback> entry : remoteSubscriptions.entrySet()) {
      for (MessageQueue queue : entry.getValue().getQueues()) {
        busMonitor.notifyNewSubscriptionEvent(new SubscriptionEvent(true, queue.getSession().getSessionId(), 1, false, entry.getKey()));
      }
    }

    monitor.attach(this);
  }

  @Override
  public void stop() {
    for (MessageQueue queue : messageQueues.values()) {
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
}