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

import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.server.api.*;
import org.jboss.errai.bus.server.async.TimedTask;
import org.jboss.errai.bus.server.io.buffers.BufferColor;
import org.jboss.errai.bus.server.io.buffers.TransmissionBuffer;
import org.jboss.errai.marshalling.server.JSONStreamEncoder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.System.nanoTime;

/**
 * A message queue is keeps track of which messages need to be sent outbound. It keeps track of the amount of messages
 * that can be stored, transmitted and those which timeout. The <tt>MessageQueue</tt> is implemented using a
 * {@link java.util.concurrent.LinkedBlockingQueue} to store the messages, and a <tt>ServerMessageBus</tt> to send the
 * messages.
 */
public class MessageQueueImpl implements MessageQueue {
  private static final long HEARTBEAT_PERIOD = secs(30);
  private static final long TIMEOUT = Boolean.getBoolean("org.jboss.errai.debugmode") ?
          secs(360) : secs(30);

  private final QueueSession session;

  private volatile long lastTransmission = nanoTime();
  private long endWindow;

  private boolean queueRunning = true;

  private SessionControl sessionControl;
  private volatile QueueActivationCallback activationCallback;

  private final TransmissionBuffer buffer;
  private final BufferColor bufferColor;

  private final ServerMessageBus bus;
  private volatile TimedTask task;

  private volatile boolean initLock = true;
  private final Object activationLock = new Object();

  private final AtomicInteger messageCount = new AtomicInteger();

  private static final AtomicInteger bufferColorCounter = new AtomicInteger();


  public MessageQueueImpl(TransmissionBuffer buffer, final ServerMessageBus bus, final QueueSession session) {
    this.buffer = buffer;
    this.bus = bus;
    this.session = session;
    this.bufferColor = new BufferColor(bufferColorCounter.incrementAndGet());
  }

  /**
   * Gets the next message to send, and returns the <tt>Payload</tt>, which contains the current messages that
   * need to be sent from the specified bus to another.
   *
   * @param wait      - boolean is true if we should wait until the queue is ready. In this case, a
   *                  <tt>RuntimeException</tt> will be thrown if the polling is active already. Concurrent polling is not allowed.
   * @param outstream - output stream to write the polling results to.
   */
  public void poll(final boolean wait, final OutputStream outstream) throws IOException {

    checkSession();

    try {
      outstream.write('[');

      if (wait) {
        buffer.readWait(TimeUnit.SECONDS, 45, outstream, bufferColor);
      }
      else {
        buffer.read(outstream, bufferColor);
      }

      messageCount.set(0);

      outstream.write(']');

    }
    catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  //private static final byte[] heartBeatBytes = "{\"ToSubject\":\"ClientBus\",\"CommandType\":\"Heartbeat\"}".getBytes();


  private static class StreamWrapper extends ByteArrayOutputStream {

    StreamWrapper(int size) {
      super(size);
    }

    public byte[] getRawArray() {
      return super.buf;
    }
  }

  /**
   * Inserts the specified message into the queue, and returns true if it was successful
   *
   * @param message - the message to insert into the queue
   * @return true if insertion was successful
   */
  public boolean offer(final Message message) throws IOException {

    if (!queueRunning) {
      throw new QueueUnavailableException("queue is not available");
    }

    activity();

    StreamWrapper out = new StreamWrapper(1024);
    if (messageCount.intValue() > 0) {
      out.write(',');
    }
    JSONStreamEncoder.encode(message.getParts(), out);
    buffer.write(out.size(), new ByteArrayInputStream(out.getRawArray(), 0, out.size()), bufferColor);

    messageCount.incrementAndGet();

    if (activationCallback != null) {
      synchronized (activationLock) {
        if (activationCallback != null) activationCallback.activate(this);
      }
    }

    return true;
  }

  /**
   * Schedules the activation, by sending off the queue. All message should be processed and sent once the task is
   * processed
   */
  public void scheduleActivation() {
    synchronized (activationLock) {
      bus.getScheduler().addTask(
              task = new TimedTask() {
                {
                  period = -1; // only fire once.
                  nextRuntime = getEndOfWindow();
                }

                public void run() {
                  if (activationCallback != null)
                    activationCallback.activate(MessageQueueImpl.this);

                  task = null;
                }

                public boolean isFinished() {
                  return false;
                }

                @Override
                public String toString() {
                  return "MessageResumer";
                }
              }
      );
    }
  }

  private void checkSession() {
    if (sessionControl != null && !sessionControl.isSessionValid()) {
      throw new MessageQueueExpired("session has expired");
    }
  }

  public void setSessionControl(SessionControl sessionControl) {
    this.sessionControl = sessionControl;
  }

  /**
   * This function indicates activity on the session, so the session knows when the last time there was activity.
   * The <tt>MessageQueue</tt> relies on this to figure out whether or not to timeout
   */
  public void activity() {
    if (sessionControl != null) sessionControl.activity();
  }

  private boolean isWindowExceeded() {
    return nanoTime() > endWindow;
  }

  private boolean isHeartbeatNeeded() {
    return (nanoTime() - lastTransmission) > HEARTBEAT_PERIOD;
  }

  private long getEndOfWindow() {
    return endWindow - nanoTime();
  }

  private void descheduleTask() {
    synchronized (activationLock) {
      if (task != null) {
        task.cancel(true);
        task = null;
      }
    }
  }

  /**
   * Sets the activation callback function which is called when the queue is scheduled for activation
   *
   * @param activationCallback - new activation callback function
   */
  public void setActivationCallback(QueueActivationCallback activationCallback) {
    synchronized (activationLock) {
      this.activationCallback = activationCallback;
    }
  }

  /**
   * Returns the current activation callback function
   *
   * @return the current activation callback function
   */
  public QueueActivationCallback getActivationCallback() {
    return activationCallback;
  }


  public QueueSession getSession() {
    return session;
  }

  /**
   * Returns true if the queue is not running, or it has timed out
   *
   * @return true if the queue is stale
   */
  public boolean isStale() {
    return !queueRunning || (!isActive() && (nanoTime() - lastTransmission) > TIMEOUT);
  }

  /**
   * Returns true if the queue is currently active and polling
   *
   * @return true if the queue is actively polling
   */
  public boolean isActive() {
    return true;
  }


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
  public void heartBeat() {
    lastTransmission = nanoTime();
  }


  public void finishInit() {
    initLock = false;
  }

  /**
   * Stops the queue, closes it on the bus and clears it completely
   */
  public void stopQueue() {
    //  queue.offer(new QueueStopMessage());
  }

  private static long secs(long secs) {
    return secs * 1000000000;
  }

  private static long millis(long millis) {
    return millis * 1000000;
  }

  @Override
  public Object getActivationLock() {
    return activationLock;
  }

  @Override
  public BufferColor getBufferColor() {
    return null;
  }
}
