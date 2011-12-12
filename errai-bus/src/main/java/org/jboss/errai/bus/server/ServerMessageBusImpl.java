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

import com.google.inject.Singleton;
import org.jboss.errai.bus.client.api.*;
import org.jboss.errai.bus.client.api.base.*;
import org.jboss.errai.bus.client.framework.*;
import org.jboss.errai.bus.client.protocols.BusCommands;
import org.jboss.errai.bus.server.api.*;
import org.jboss.errai.bus.server.async.SchedulerService;
import org.jboss.errai.bus.server.async.SimpleSchedulerService;
import org.jboss.errai.bus.server.async.TimedTask;
import org.jboss.errai.bus.server.io.BufferHelper;
import org.jboss.errai.bus.server.io.buffers.BufferColor;
import org.jboss.errai.bus.server.io.buffers.TransmissionBuffer;
import org.jboss.errai.bus.server.service.ErraiServiceConfigurator;
import org.jboss.errai.common.client.protocols.MessageParts;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
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
  private static final String ERRAI_BUS_QUEUESIZE = "errai.bus.queuesize";

  private final static int DEFAULT_QUEUE_SIZE = 250;

  private int queueSize = DEFAULT_QUEUE_SIZE;

  private final List<MessageListener> listeners = new ArrayList<MessageListener>();

  // 32 kb buffers * 4096 segments = 128 megabytes
  private final TransmissionBuffer transmissionbuffer = TransmissionBuffer.create((32 * 1024), 4096);

  private final Map<String, DeliveryPlan> subscriptions = new ConcurrentHashMap<String, DeliveryPlan>();
  private final Map<String, RemoteMessageCallback> remoteSubscriptions = new ConcurrentHashMap<String, RemoteMessageCallback>();

  private final Map<QueueSession, MessageQueue> messageQueues = new ConcurrentHashMap<QueueSession, MessageQueue>();

  private final Map<MessageQueue, List<Message>> deferredQueue = new ConcurrentHashMap<MessageQueue, List<Message>>();
  private final Map<String, QueueSession> sessionLookup = new ConcurrentHashMap<String, QueueSession>();

  private final List<SubscribeListener> subscribeListeners = new LinkedList<SubscribeListener>();
  private final List<UnsubscribeListener> unsubscribeListeners = new LinkedList<UnsubscribeListener>();
  private final List<QueueClosedListener> queueClosedListeners = new LinkedList<QueueClosedListener>();

  private final SchedulerService houseKeeper = new SimpleSchedulerService(); // GAESchedulerService.INSTANCE;

  private Logger log = getLogger(getClass());

  private BusMonitor busMonitor;

  private Set<String> reservedNames = new HashSet<String>();

  /**
   * Sets up the <tt>ServerMessageBusImpl</tt> with the configuration supplied. Also, initializes the bus' callback
   * functions, scheduler, and monitor
   * <p/>
   * When deploying services on the server-side, it is possible to obtain references to the
   * <tt>ErraiServiceConfigurator</tt> by declaring it as injection dependencies
   */
  public ServerMessageBusImpl() {
    /**
     * Define the default ServerBus service used for intrabus communication.
     */
    subscribe("ServerBus", new MessageCallback() {
      @SuppressWarnings({"unchecked", "SynchronizationOnLocalVariableOrMethodParameter"})
      public void callback(Message message) {
        try {
          QueueSession session = getSession(message);
          MessageQueueImpl queue = (MessageQueueImpl) messageQueues.get(session);

          switch (BusCommands.valueOf(message.getCommandType())) {
            case Heartbeat:
              if (queue != null) {
                queue.heartBeat();
              }
              break;

            case RemoteSubscribe:
              if (queue == null) return;

              if (message.hasPart("SubjectsList")) {
                for (String subject : (List<String>) message.get(List.class, "SubjectsList")) {
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
                queue = (MessageQueueImpl) messageQueues.get(session);
                queue.stopQueue();
                closeQueue(queue);
              }

              break;
            case Resend:
              if (queue == null) return;


            case ConnectToQueue:
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

                addQueue(session, queue = new MessageQueueImpl(transmissionbuffer, session));

                if (deferred != null) {
                  deferredQueue.put(queue, deferred);
                }

                remoteSubscribe(session, queue, "ClientBus");
              }

              if (isMonitor()) {
                busMonitor.notifyQueueAttached(session.getSessionId(), queue);
              }

              List<String> subjects = new LinkedList<String>();
              for (String service : subscriptions.keySet()) {
                if (service.startsWith("local:")) {
                }
                else if (!remoteSubscriptions.containsKey(service)) {
                  subjects.add(service);
                }
              }

              createConversation(message)
                      .toSubject("ClientBus")
                      .command(BusCommands.RemoteSubscribe)
                      .with("SubjectsList", subjects)
                      .with(MessageParts.PriorityProcessing, "1")
                      .noErrorHandling().sendNowWith(ServerMessageBusImpl.this, false);

              CommandMessage msg = ConversationMessage.create(message);
              msg.toSubject("ClientBus")
                      .command(BusCommands.CapabilitiesNotice);

              if (ErraiServiceConfigurator.LONG_POLLING) {
                msg.set("Flags", Capabilities.LongPollAvailable.name());
              }
              else {
                msg.set("Flags", Capabilities.NoLongPollAvailable.name());
                msg.set("PollFrequency", ErraiServiceConfigurator.HOSTED_MODE_TESTING ? 50 : 250);
              }

              send(msg, false);

              createConversation(message)
                      .toSubject("ClientBus")
                      .command(BusCommands.FinishStateSync)
                      .noErrorHandling().sendNowWith(ServerMessageBusImpl.this, false);

              break;
          }

        }
        catch (Throwable t) {
          t.printStackTrace();
        }
      }
    });

    addSubscribeListener(new SubscribeListener() {
      public void onSubscribe(SubscriptionEvent event) {
        if (event.isLocalOnly() || event.isRemote() || event.getSubject().startsWith("local:")) return;
        synchronized (messageQueues) {
          if (messageQueues.isEmpty()) return;

          MessageBuilder.createMessage()
                  .toSubject("ClientBus")
                  .command(BusCommands.RemoteSubscribe)
                  .with(MessageParts.Subject, event.getSubject())
                  .noErrorHandling().sendGlobalWith(ServerMessageBusImpl.this);
        }
      }
    });

    addUnsubscribeListener(new UnsubscribeListener() {
      public void onUnsubscribe(SubscriptionEvent event) {
        if (event.isLocalOnly() || event.isRemote() || event.getSubject().startsWith("local:")) return;
        synchronized (messageQueues) {
          if (messageQueues.isEmpty()) return;

          MessageBuilder.createMessage()
                  .toSubject("ClientBus")
                  .command(BusCommands.RemoteUnsubscribe)
                  .with(MessageParts.Subject, event.getSubject())
                  .noErrorHandling().sendGlobalWith(ServerMessageBusImpl.this);
        }
      }
    });

    houseKeeper.addTask(new TimedTask() {
      {
        this.period = (1000 * 10);
      }

      @SuppressWarnings({"UnusedParameters"})
      public void setExceptionHandler(AsyncExceptionHandler handler) {
      }

      public void run() {
        boolean houseKeepingPerformed = false;
        List<MessageQueue> endSessions = new LinkedList<MessageQueue>();

        while (!houseKeepingPerformed) {
          try {
            Iterator<MessageQueue> iter = ServerMessageBusImpl.this.messageQueues.values().iterator();
            MessageQueue q;
            while (iter.hasNext()) {
              if ((q = iter.next()).isStale()) {
                iter.remove();
                endSessions.add(q);
              }
            }

            houseKeepingPerformed = true;
          }
          catch (ConcurrentModificationException cme) {
            // fall-through and try again.
          }
        }


        for (MessageQueue ref : endSessions) {
          for (String subject : new HashSet<String>(ServerMessageBusImpl.this.remoteSubscriptions.keySet())) {
            ServerMessageBusImpl.this.remoteUnsubscribe(ref.getSession(), ref, subject);
          }

          ServerMessageBusImpl.this.closeQueue(ref);
          ref.getSession().endSession();
          deferredQueue.remove(ref);
        }

        BufferStatus stat = bufferStatus();
        log.info("[experimental] buffer status [bytes free: " + stat.getFreeBytes()
                + " (" + (stat.getFree() * 100) + "%) tail range: " + stat.getTailRange() + "]");
      }

      public boolean isFinished() {
        return false;
      }

      @Override
      public String toString() {
        return "Bus Housekeeper";
      }
    });

    houseKeeper.start();
  }

  private static class BufferStatus {
    private int freeBytes;
    private int tailRange;
    private float free;

    private BufferStatus(int freeBytes, int tailRange, float free) {
      this.freeBytes = freeBytes;
      this.tailRange = tailRange;
      this.free = free;
    }

    public int getFreeBytes() {
      return freeBytes;
    }

    public int getTailRange() {
      return tailRange;
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
    for (MessageQueue q : messageQueues.values()) {
      long seq = q.getCurrentBufferSequenceNumber();
      if (lowTail == -1) {
        lowTail = highTail = seq;
      }
      else {
        if (seq > highTail) highTail = seq;
        if (seq < lowTail) lowTail = seq;
      }
    }

    long lowSegBytes = (lowTail % transmissionbuffer.getBufferSize()) * transmissionbuffer.getSegmentSize();
    long highSegBytes = (highTail % transmissionbuffer.getBufferSize()) * transmissionbuffer.getSegmentSize();


    int free;
    if (lowSegBytes < headBytes) {
      free = (int) ((bufSize - headBytes) + lowSegBytes);
    }
    else if (lowSegBytes > headBytes) {
      free = (int) (lowSegBytes - bufSize);
    }
    else {
      free = bufSize;
    }

    return new BufferStatus(free, (int) (highSegBytes - lowSegBytes), ((float) free) / bufSize);
  }


  private void addQueue(QueueSession session, MessageQueueImpl queue) {
    messageQueues.put(session, queue);
    sessionLookup.put(session.getSessionId(), session);
  }

  /**
   * Configures the server message bus with the specified <tt>ErraiServiceConfigurator</tt>. It only takes the queue
   * size specified by the configuration
   *
   * @param config -
   */
  public void configure(ErraiServiceConfigurator config) {
    queueSize = DEFAULT_QUEUE_SIZE;
    if (config.hasProperty(ERRAI_BUS_QUEUESIZE)) {
      queueSize = Integer.parseInt(config.getProperty(ERRAI_BUS_QUEUESIZE));
    }

    //   this.modelAdapter = config.getResource(ModelAdapter.class);
  }


  private static final String RETRY_COUNT_KEY = "retryAttempts";

  /**
   * Sends a message globally to all subscriptions containing the same subject as the specified message.
   *
   * @param message - The message to be sent.
   */
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
      if (message.hasPart(ReplyTo) && message.hasResource("Session")) {
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
      if (message.isFlagSet(RoutingFlags.FromRemote)) {
        busMonitor.notifyIncomingMessageFromRemote(
                message.getResource(QueueSession.class, "Session").getSessionId(), message);
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
  }

  private void delayOrFail(Message message, final Runnable deliveryTaskRunnable) {
    if (message.isFlagSet(RoutingFlags.RetryDelivery) && message.getResource(Integer.class, RETRY_COUNT_KEY) > 3) {
      throw new NoSubscribersToDeliverTo(message.getSubject());
    }
    else {
      message.setFlag(RoutingFlags.RetryDelivery);
      if (!message.hasResource(RETRY_COUNT_KEY)) {
        message.setResource("retryAttempts", 0);
      }
      message.setResource("retryAttempts", message.getResource(Integer.class, RETRY_COUNT_KEY) + 1);
      getScheduler().addTaskConcurrently(new TimedTask() {
        {
          period = 250;
        }

        @Override
        public void run() {
          deliveryTaskRunnable.run();
          cancel();
        }
      });
    }
  }

  /**
   * Sends the <tt>message</tt>
   *
   * @param message - the message to send
   */
  public void send(Message message) {
    message.commit();
    if (message.hasResource("Session")) {
      message.setFlag(RoutingFlags.NonGlobalRouting);
      send(getQueueByMessage(message), message, true);
    }
    else if (message.hasPart(MessageParts.SessionID)) {
      message.setFlag(RoutingFlags.NonGlobalRouting);
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
  public void send(Message message, boolean fireListeners) {
    message.commit();
    if (!message.hasResource("Session")) {
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

      asyncEnqueue(queue, message);
    }
    catch (NoSubscribersToDeliverTo nstdt) {
      // catch this so we can get a full trace
      handleMessageDeliveryFailure(this, message, "No subscribers to deliver to", nstdt, false);
    }
  }

  private void asyncEnqueue(final MessageQueue queue, final Message message) {
    TaskManagerFactory.get().execute(new Runnable() {
      public void run() {
        try {
          enqueueForDelivery(queue, message);
        }
        catch (QueueOverloadedException e) {
          String queueSessionId = (queue == null || queue.getSession() == null) ?
                  "(no queue session)" : "(session id=" + queue.getSession().getSessionId() + ")";
          handleMessageDeliveryFailure(ServerMessageBusImpl.this, message, "Queue overloaded " + queueSessionId, e, false);
        }
      }
    });
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
  public MessageQueue getQueue(QueueSession session) {
    return messageQueues.get(session);
  }

  /**
   * Closes the queue with <tt>sessionId</tt>
   *
   * @param sessionId - the session context of the queue to close
   */
  public void closeQueue(String sessionId) {
    closeQueue(getQueueBySession(sessionId));
  }

  /**
   * Closes the message queue
   *
   * @param queue - the message queue to close
   */
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
  public void subscribe(String subject, MessageCallback receiver) {
    if (reservedNames.contains(subject))
      throw new IllegalArgumentException("cannot modify or subscribe to reserved service: " + subject);

    DeliveryPlan plan = createOrAddDeliveryPlan(subject, receiver);

    fireSubscribeListeners(new SubscriptionEvent(false, null, plan.getTotalReceivers(), true, subject));
  }

  public void subscribeLocal(String subject, MessageCallback receiver) {
    if (reservedNames.contains(subject))
      throw new IllegalArgumentException("cannot modify or subscribe to reserved service: " + subject);

    DeliveryPlan plan = createOrAddDeliveryPlan(subject, receiver);

    fireSubscribeListeners(new SubscriptionEvent(false, false, true, true, plan.getTotalReceivers(), "InBus", subject));
  }

  private DeliveryPlan createOrAddDeliveryPlan(final String subject, final MessageCallback receiver) {
    synchronized (subscriptions) {
      DeliveryPlan plan = subscriptions.get(subject);

      if (plan == null) {
        subscriptions.put(subject, plan = new DeliveryPlan(new MessageCallback[]{receiver}));
      }
      else {
        subscriptions.put(subject, plan.newDeliveryPlanWith(receiver));
      }

      return plan;
    }
  }


  private static final Set<String> pipeLineExclusionSet = new HashSet<String>() {
    {
      add("ClientBus");
      add("ClientBusErrors");
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
        rmc = new RemoteMessageCallback(!pipeLineExclusionSet.contains(subject), subject);
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

    private final boolean pipelinable;
    private final AtomicInteger totalPipelined = new AtomicInteger();

    public RemoteMessageCallback(boolean pipelineable, String svc) {
      this.pipelinable = pipelineable;
      this.svc = svc;
    }

    public void callback(Message message) {
      // do not pipeline if this message is addressed to a specified session.
      if (pipelinable && !message.isFlagSet(RoutingFlags.NonGlobalRouting)) {
        // all queues are listening to this subject. therefore we can save memory and time by
        // writing to the broadcast color on the buffer
        try {

          BufferHelper.encodeAndWrite(transmissionbuffer, BufferColor.getAllBuffersColor(), message);
          for (MessageQueue q : queues) q.wake();

          if (totalPipelined.incrementAndGet() % 1000 == 0) {
            log.info("[experimental] " + totalPipelined.get() + " messages have been pipelined to service: " + svc);
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
  public void unsubscribeAll(String subject) {
    if (reservedNames.contains(subject))
      throw new IllegalArgumentException("Attempt to modify lockdown service: " + subject);

    subscriptions.remove(subject);

    fireUnsubscribeListeners(new SubscriptionEvent(false, null, 0, false, subject));
  }

  /**
   * Starts a conversation using the specified message
   *
   * @param message  - the message to initiate the conversation
   * @param callback - the message's callback function
   */
  public void conversationWith(Message message, MessageCallback callback) {
    throw new RuntimeException("conversationWith not yet implemented.");
  }

  /**
   * Checks if a subscription exists for <tt>subject</tt>
   *
   * @param subject - the subject to search the subscriptions for
   * @return true if a subscription exists
   */
  public boolean isSubscribed(String subject) {
    return subscriptions.containsKey(subject);
  }

  private boolean isAnyoneListening(MessageQueue queue, String subject) {
    return subscriptions.containsKey(subject) ||
            (remoteSubscriptions.containsKey(subject) && remoteSubscriptions.get(subject).contains(queue));
  }

  public boolean hasRemoteSubscriptions(String subject) {
    return remoteSubscriptions.containsKey(subject);
  }

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
  public void addUnsubscribeListener(UnsubscribeListener listener) {
    synchronized (unsubscribeListeners) {
      unsubscribeListeners.add(listener);
    }
  }

  private static QueueSession getSession(Message message) {
    return message.getResource(QueueSession.class, "Session");
  }

  private MessageQueue getQueueByMessage(Message message) {
    MessageQueue queue = getQueue(getSession(message));
    if (queue == null)
      throw new QueueUnavailableException("no queue available to send. (queue or session may have expired)");
    return queue;
  }

  public MessageQueue getQueueBySession(String sessionId) {
    return getQueue(sessionLookup.get(sessionId));
  }

  /**
   * Gets all the message queues
   *
   * @return a map of the message queues that exist
   */
  public Map<QueueSession, MessageQueue> getMessageQueues() {
    return messageQueues;
  }

  /**
   * Gets the scheduler being used for the housekeeping
   *
   * @return the scheduler
   */
  public SchedulerService getScheduler() {
    return houseKeeper;
  }

  public void addQueueClosedListener(QueueClosedListener listener) {
    synchronized (queueClosedListeners) {
      queueClosedListeners.add(listener);
    }
  }

  public List<MessageCallback> getReceivers(String subject) {
    return Collections.unmodifiableList(Arrays.asList(subscriptions.get(subject).getDeliverTo()));
  }

  private boolean isMonitor() {
    return this.busMonitor != null;
  }

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

  public void stop() {
    for (MessageQueue queue : messageQueues.values()) {
      queue.stopQueue();
    }

    houseKeeper.requestStop();
  }

  public void finishInit() {
    reservedNames.addAll(subscriptions.keySet());
  }
}