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

import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.server.api.MessageQueue;
import org.jboss.errai.bus.server.api.QueueActivationCallback;
import org.jboss.errai.bus.server.api.QueueSession;
import org.jboss.errai.bus.server.io.BufferHelper;
import org.jboss.errai.bus.server.io.buffers.BufferCallback;
import org.jboss.errai.bus.server.io.buffers.BufferColor;
import org.jboss.errai.bus.server.io.buffers.TransmissionBuffer;
import org.jboss.errai.marshalling.server.JSONEncoder;
import org.slf4j.Logger;

import java.io.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.System.nanoTime;
import static java.lang.System.out;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * A message queue is keeps track of which messages need to be sent outbound. It keeps track of the amount of messages
 * that can be stored, transmitted and those which timeout. The <tt>MessageQueue</tt> is implemented using a
 * {@link java.util.concurrent.LinkedBlockingQueue} to store the messages, and a <tt>ServerMessageBus</tt> to send the
 * messages.
 */
public class MessageQueueImpl implements MessageQueue {
  private static final long TIMEOUT = Boolean.getBoolean("org.jboss.errai.debugmode") ?
          secs(360) : secs(60);

  private static final long DOWNGRADE_THRESHOLD = Boolean.getBoolean("org.jboss.errai.debugmode") ?
          secs(360) : secs(5);


  private final QueueSession session;

  private boolean initLock = true;
  private boolean queueRunning = true;
  private volatile long lastTransmission = nanoTime();
  private volatile boolean pagedOut = false;

  private volatile QueueActivationCallback activationCallback;

  private final TransmissionBuffer buffer;
  private final BufferColor bufferColor;

  private volatile boolean useDirectSocketChanne = false;
  private Channel directSocketChannel;

  private final Object activationLock = new Object();
  private final AtomicInteger messageCount = new AtomicInteger();

  private Logger log = getLogger(getClass());

  public MessageQueueImpl(TransmissionBuffer buffer, final QueueSession session) {
    this.buffer = buffer;
    this.session = session;
    this.bufferColor = BufferColor.getNewColor();
  }

  /**
   * Gets the next message to send, and returns the <tt>Payload</tt>, which contains the current messages that
   * need to be sent from the specified bus to another.
   *
   * @param wait      - boolean is true if we should wait until the queue is ready. In this case, a
   *                  <tt>RuntimeException</tt> will be thrown if the polling is active already. Concurrent polling is not allowed.
   * @param outstream - output stream to write the polling results to.
   */
  public int poll(final boolean wait, final OutputStream outstream) throws IOException {
    if (!queueRunning) {
      throw new QueueUnavailableException("queue is not available");
    }

    lastTransmission = nanoTime();

    if (pagedOut) {
      synchronized (pageLock) {
        if (pagedOut) {
          readInPageFile(outstream, new BufferHelper.MultiMessageHandlerCallback());
          return -1;
        }
      }
    }

    int seg;
    try {
      if (wait) {
        seg = buffer.readWait(TimeUnit.SECONDS, 20, outstream, bufferColor, new BufferHelper.MultiMessageHandlerCallback());
      }
      else {
        seg = buffer.read(outstream, bufferColor, new BufferHelper.MultiMessageHandlerCallback());
      }
      messageCount.set(0);
      outstream.flush();
    }
    catch (InterruptedException e) {
      e.printStackTrace();
      seg = -1;
    }
    return seg;
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

    if (useDirectSocketChanne && directSocketChannel.isConnected()) {
      directSocketChannel.write(new TextWebSocketFrame("[" + JSONEncoder.encode(message.getParts()) + "]"));
    }
    else {
      if (pagedOut) {
        try {
          synchronized (pageLock) {
            if (pagedOut) {
              writeToPageFile(JSONEncoder.encodeToByteArrayInputStream(message.getParts()), true);
              return true;
            }
          }
        }
        finally {
          bufferColor.getLock().lock();
          try {
            bufferColor.wake();
          }
          finally {
            bufferColor.getLock().unlock();
          }
        }
      }

      BufferHelper.encodeAndWrite(buffer, bufferColor, message);

      if (messageCount.incrementAndGet() > 5 && !lastTransmissionWithin(secs(3))) {
        // disconnect this client
        stopQueue();
      }

      if (activationCallback != null) {
        synchronized (activationLock) {
          if (activationCallback != null) activationCallback.activate(this);
        }
      }

    }
    return true;
  }

  private final Object pageLock = new Object();

  @Override
  public boolean pageWaitingToDisk() {
    synchronized (pageLock) {
      try {
        boolean alreadyPaged = pagedOut;

        OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(getOrCreatePageFile(), alreadyPaged));
        buffer.read(outputStream, bufferColor);
        outputStream.flush();
        outputStream.close();

        pagedOut = true;

        return alreadyPaged;
      }
      catch (IOException e) {
        throw new RuntimeException("paging error", e);
      }
    }
  }

  private void writeToPageFile(InputStream inputStream, boolean append) {

    try {
      OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(getOrCreatePageFile(), append));

      int read;
      while ((read = inputStream.read()) != -1) outputStream.write(read);

      outputStream.flush();
      outputStream.close();

    }
    catch (IOException e) {
      throw new RuntimeException("paging error", e);
    }
  }

  private File getOrCreatePageFile() throws IOException {
    File pageFile = new File(getPageFileName());
    if (!pageFile.exists()) {
      pageFile.getParentFile().mkdirs();
      pageFile.createNewFile();
      pageFile.deleteOnExit();
    }
    return pageFile;
  }

  private void readInPageFile(OutputStream outputStream, BufferCallback callback) {
    synchronized (pageLock) {
      try {
        if (pagedOut) {
          File pageFile = new File(getPageFileName());
          if (!pageFile.exists()) {
            pagedOut = false;
            return;
          }

          InputStream inputStream = new BufferedInputStream(new FileInputStream(pageFile));

          callback.before(outputStream);

          int read;
          while ((read = inputStream.read()) != -1) {
            outputStream.write(callback.each(read, outputStream));
          }
          inputStream.close();

          callback.after(outputStream);

          pagedOut = false;
        }
      }
      catch (IOException e) {
        throw new RuntimeException("paging error", e);
      }
    }
  }

  private static final String tempDir = System.getProperty("java.io.tmpdir");

  private String getPageFileName() {
    return tempDir + "/queueCache/" + session.getSessionId().replaceAll("\\-", "_");
  }

  @Override
  public long getCurrentBufferSequenceNumber() {
    return bufferColor.getSequence().get();
  }

  @Override
  public void wake() {
    try {
      if (isDirectChannelOpen()) {
        JSONEncoder.UnwrappedByteArrayOutputStream outputStream = new JSONEncoder.UnwrappedByteArrayOutputStream();
        buffer.read(outputStream, bufferColor, new BufferHelper.MultiMessageHandlerCallback());
        directSocketChannel.write(new TextWebSocketFrame(new String(outputStream.toByteArray(), 0, outputStream.size())));
      }
      else {
        BufferHelper.encodeAndWriteNoop(buffer, bufferColor);
      }
    }
    catch (IOException e) {
      e.printStackTrace();
    }
  }

  private boolean lastTransmissionWithin(long nanos) {
    return (nanoTime() - lastTransmission) < nanos;
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
    return !isDirectChannelOpen() && (!queueRunning || ((nanoTime() - lastTransmission) > TIMEOUT));
  }


  @Override
  public boolean isDowngradeCandidate() {
    return !isDirectChannelOpen() && ((nanoTime() - lastTransmission) > DOWNGRADE_THRESHOLD);
  }

  public boolean isInitialized() {
    return !initLock;
  }

  @Override
  public boolean messagesWaiting() {
    return messageCount.intValue() > 0;
  }

  private boolean isDirectChannelOpen() {
    return useDirectSocketChanne && directSocketChannel.isOpen();
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

  @Override
  public boolean isPaged() {
    return pagedOut;
  }

  @Override
  public void discard() {
    queueRunning = false;
    if (pagedOut) {
      File pageFile = new File(getPageFileName());
      if (pageFile.exists()) {
        pageFile.delete();
      }
    }
  }

  /**
   * Stops the queue, closes it on the bus and clears it completely
   */
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

  private static long secs(long secs) {
    return secs * 1000000000;
  }

  @Override
  public Object getActivationLock() {
    return activationLock;
  }

  @Override
  public void setDirectSocketChannel(Channel channel) {
    this.directSocketChannel = channel;
    this.useDirectSocketChanne = true;

    log.info("queue " + getSession().getSessionId() + " transitioned to direct channel mode.");
  }
}
