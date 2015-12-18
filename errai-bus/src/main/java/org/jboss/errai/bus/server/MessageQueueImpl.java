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

import static org.slf4j.LoggerFactory.getLogger;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import org.jboss.errai.bus.client.api.QueueSession;
import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.bus.server.api.MessageQueue;
import org.jboss.errai.bus.server.api.QueueActivationCallback;
import org.jboss.errai.bus.server.io.BufferDeliveryHandler;
import org.jboss.errai.bus.server.io.Buffered;
import org.jboss.errai.bus.server.io.ByteWriteAdapter;
import org.jboss.errai.bus.server.io.Cleanable;
import org.jboss.errai.bus.server.io.DirectChannel;
import org.jboss.errai.bus.server.io.MessageDeliveryHandler;
import org.jboss.errai.bus.server.io.Wakeable;
import org.jboss.errai.bus.server.io.buffers.Buffer;
import org.jboss.errai.bus.server.io.buffers.BufferColor;
import org.jboss.errai.bus.server.io.buffers.TransmissionBuffer;
import org.slf4j.Logger;

/**
 * A message queue is keeps track of which messages need to be sent outbound. It keeps track of the amount of messages
 * that can be stored, transmitted and those which timeout. The <tt>MessageQueue</tt> is implemented using a
 * {@link java.util.concurrent.LinkedBlockingQueue} to store the messages, and a <tt>ServerMessageBus</tt> to send the
 * messages.
 */
public class MessageQueueImpl implements MessageQueue {
  private final QueueSession session;

  private boolean initLock = true;
  private boolean queueRunning = true;
  private volatile long lastTransmission = System.currentTimeMillis();
  private volatile boolean pagedOut = false;

  private volatile MessageDeliveryHandler deliveryHandler = BufferDeliveryHandler.getInstance();
  private volatile QueueActivationCallback activationCallback;
  private volatile long timeout;

  private final TransmissionBuffer buffer;
  private final BufferColor bufferColor;

  private final Object activationLock = new Object();
  private final Object pageLock = new Object();
  private final AtomicInteger messageCount = new AtomicInteger();

  private static final Logger log = getLogger(MessageQueueImpl.class);

  public MessageQueueImpl(final TransmissionBuffer buffer, final QueueSession session, final int timeoutSecs) {
    this.buffer = buffer;
    this.session = session;
    this.bufferColor = BufferColor.getNewColorFromHead(buffer);
    this.timeout = (timeoutSecs * 1000);
  }

  @Override
  public boolean poll(final ByteWriteAdapter stream) throws IOException {
    if (!queueRunning) {
      throw new QueueUnavailableException("queue is not available");
    }

    if (deliveryHandler instanceof Buffered) {
      return ((Buffered) deliveryHandler).copyFromBuffer(this, stream);
    }
    else {
      // this can happen during the hand off to WebSockets.
      log.debug("call to poll() when DeliveryHandler does not implement Buffered.");
    }

    return false;
  }

  @Override
  public boolean poll(final java.util.concurrent.TimeUnit timeUnit, final int time, final ByteWriteAdapter stream) throws IOException {
    if (!queueRunning) {
      throw new QueueUnavailableException("queue is not available");
    }

    if (deliveryHandler instanceof Buffered) {
      return ((Buffered) deliveryHandler).copyFromBuffer(timeUnit, time, this, stream);
    }
    else {
      // this can happen during the hand off to WebSockets.
      log.debug("call to poll() when DeliveryHandler does not implement Buffered.");
    }

    return false;
  }

  /**
   * Inserts the specified message into the queue, and returns true if it was successful
   *
   * @param message
   *     - the message to insert into the queue
   *
   * @return true if insertion was successful
   */
  @Override
  public boolean offer(final Message message) throws IOException {
    if (!queueRunning) {
      throw new QueueUnavailableException("queue is not available");
    }

    return deliveryHandler.deliver(this, message);
  }

  @Override
  public long getCurrentBufferSequenceNumber() {
    return bufferColor.getSequence().get();
  }

  @Override
  public void wake() {
    if (!queueRunning) return;

    try {
      if (deliveryHandler instanceof Wakeable) {
        ((Wakeable) deliveryHandler).onWake(this);
      }
      else {
        deliveryHandler.noop(this);
      }

      fireActivationCallback();
    }
    catch (Throwable e) {
      log.debug("unable to wake queue: " + session.getSessionId());
      stopQueue();
    }
  }

  /**
   * Sets the activation callback function which is called when the queue is scheduled for activation
   *
   * @param activationCallback
   *     - new activation callback function
   */
  @Override
  public void setActivationCallback(final QueueActivationCallback activationCallback) {
    synchronized (activationLock) {
      this.activationCallback = activationCallback;
    }
  }

  @Override
  public void fireActivationCallback() {
    synchronized (activationLock) {
      if (activationCallback != null) {
        activationCallback.activate(this);
      }
    }
  }

  /**
   * Returns the current activation callback function
   *
   * @return the current activation callback function
   */
  @Override
  public QueueActivationCallback getActivationCallback() {
    return activationCallback;
  }


  @Override
  public QueueSession getSession() {
    return session;
  }

  /**
   * Returns true if the queue is not running, or it has timed out
   *
   * @return true if the queue is stale
   */
  @Override
  public boolean isStale() {
    if (!queueRunning) {
      return true;
    }
    else {
      return !isDirectChannelOpen() && (((System.currentTimeMillis() - lastTransmission) > timeout));
    }
  }

  private boolean isDirectChannelOpen() {
    return deliveryHandler instanceof DirectChannel && ((DirectChannel) deliveryHandler).isConnected();
  }

  @Override
  public boolean isInitialized() {
    return !initLock;
  }

  @Override
  public boolean messagesWaiting() {
    return messageCount.intValue() > 0;
  }

  /**
   * Fakes a transmission, shows life with a heartbeat
   */
  @Override
  public void heartBeat() {
    lastTransmission = System.currentTimeMillis();
  }

  @Override
  public void finishInit() {
    initLock = false;
  }

  @Override
  public boolean isPaged() {
    return pagedOut;
  }

  @Override
  public void setPaged(final boolean pagedOut) {
    this.pagedOut = pagedOut;
  }

  @Override
  public void discard() {
    queueRunning = false;
    if (deliveryHandler instanceof Cleanable) {
      ((Cleanable) deliveryHandler).clean(this);
    }
  }

  /**
   * Stops the queue, closes it on the bus and clears it completely
   */
  @Override
  public void stopQueue() {
    try {
      queueRunning = false;

      /**
       * we write a single byte to the buffer, with the color for this queue. this is to knock any
       * waiting thread loose and return it to the work pool.
       */
      buffer.write(1, new ByteArrayInputStream(new byte[]{-1}), bufferColor);
    }
    catch (Exception e) {
      throw new RuntimeException("error trying to stop queue");
    }
  }


  @Override
  public Object getActivationLock() {
    return activationLock;
  }

  @Override
  public Object getPageLock() {
    return pageLock;
  }

  @Override
  public MessageDeliveryHandler getDeliveryHandler() {
    return deliveryHandler;
  }

  @Override
  public void setDeliveryHandler(final MessageDeliveryHandler handler) {
    this.deliveryHandler = handler;
  }

  @Override
  public void setDeliveryHandlerToDefault() {
    this.deliveryHandler = BufferDeliveryHandler.getInstance();
  }

  @Override
  public BufferColor getBufferColor() {
    return bufferColor;
  }

  @Override
  public Buffer getBuffer() {
    return buffer;
  }

  @Override
  public int incrementMessageCount() {
    return messageCount.incrementAndGet();
  }

  @Override
  public void resetMessageCount() {
    messageCount.set(0);
  }

  @Override
  public long getLastTransmissionTime() {
    return lastTransmission;
  }

  @Override
  public void setTimeout(final long timeout) {
    this.timeout = timeout;
  }

  @Override
  public String toString() {
    return "MessageQueueImpl{" +
        "session=" + session +
        '}';
  }
}
