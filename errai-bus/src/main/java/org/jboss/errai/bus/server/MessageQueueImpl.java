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
import org.jboss.errai.bus.client.api.QueueSession;
import org.jboss.errai.bus.server.api.MessageQueue;
import org.jboss.errai.bus.server.api.QueueActivationCallback;
import org.jboss.errai.bus.server.io.BufferHelper;
import org.jboss.errai.bus.server.io.QueueChannel;
import org.jboss.errai.bus.server.io.buffers.BufferCallback;
import org.jboss.errai.bus.server.io.buffers.BufferColor;
import org.jboss.errai.bus.server.io.buffers.TransmissionBuffer;
import org.jboss.errai.bus.server.util.LocalContext;
import org.jboss.errai.bus.server.util.MarkedOutputStream;
import org.jboss.errai.bus.server.util.ServerBusTools;
import org.jboss.errai.marshalling.server.util.UnwrappedByteArrayOutputStream;
import org.slf4j.Logger;

import java.io.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import static java.lang.System.nanoTime;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * A message queue is keeps track of which messages need to be sent outbound. It keeps track of the amount of messages
 * that can be stored, transmitted and those which timeout. The <tt>MessageQueue</tt> is implemented using a
 * {@link java.util.concurrent.LinkedBlockingQueue} to store the messages, and a <tt>ServerMessageBus</tt> to send the
 * messages.
 */
public class MessageQueueImpl implements MessageQueue {
  private static final long TIMEOUT = Boolean.getBoolean("org.jboss.errai.debugmode") ?
          secs(1600) : secs(60);

  private static final long DOWNGRADE_THRESHOLD = Boolean.getBoolean("org.jboss.errai.debugmode") ?
          secs(1600) : secs(10);

  private final QueueSession session;

  private boolean initLock = true;
  private boolean queueRunning = true;
  private volatile long lastTransmission = nanoTime();
  private volatile boolean pagedOut = false;

  private volatile QueueActivationCallback activationCallback;

  private final TransmissionBuffer buffer;
  private final BufferColor bufferColor;

  private volatile boolean useDirectSocketChannel = false;
  private QueueChannel directSocketChannel;

  private final Object activationLock = new Object();
  private final AtomicInteger messageCount = new AtomicInteger();

  private static final Logger log = getLogger(MessageQueueImpl.class);

  public MessageQueueImpl(final TransmissionBuffer buffer, final QueueSession session) {
    this.buffer = buffer;
    this.session = session;
    this.bufferColor = BufferColor.getNewColorFromHead(buffer);
  }

  /**
   * Gets the next message to send, and returns the <tt>Payload</tt>, which contains the current messages that
   * need to be sent from the specified bus to another.</p>
   * <p/>
   * Fodod</p>
   *
   * @param wait      - boolean is true if we should wait until the queue is ready. In this case, a
   *                  <tt>RuntimeException</tt> will be thrown if the polling is active already. Concurrent polling is not allowed.
   * @param outstream - output stream to write the polling results to.
   */
  public boolean poll(final boolean wait, final OutputStream outstream) throws IOException {
    if (!queueRunning) {
      throw new QueueUnavailableException("queue is not available");
    }

    lastTransmission = nanoTime();
    if (pagedOut) {
      synchronized (pageLock) {
        if (pagedOut) {
          readInPageFile(outstream, new BufferHelper.MultiMessageHandlerCallback());
          return false;
        }
      }
    }

    final MarkedOutputStream markedOutputStream = new MarkedOutputStream(outstream);

    try {
      if (wait) {
        buffer.readWait(TimeUnit.SECONDS, 20, markedOutputStream, bufferColor,
                new BufferHelper.MultiMessageHandlerCallback());
      }
      else {
        buffer.read(markedOutputStream, bufferColor, new BufferHelper.MultiMessageHandlerCallback());
      }

      outstream.flush();
      if (markedOutputStream.dataWasWritten()) {
        messageCount.set(0);
        return true;
      }
    }
    catch (InterruptedException e) {
      e.printStackTrace();
    }

    return false;
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

    if (useDirectSocketChannel && directSocketChannel.isConnected()) {
      try {
        directSocketChannel.write("[" + ServerBusTools.encodeMessage(message) + "]");
      }
      catch (Throwable e) {
        log.info("error writing to socket for queue " + session.getSessionId());
        LocalContext.get(session).destroy();
        directSocketChannel = null;
        stopQueue();
        e.printStackTrace();
      }
    }
    else {
      try {
        if (pagedOut) {
          try {
            synchronized (pageLock) {
              if (pagedOut) {
                writeToPageFile(ServerBusTools.encodeMessageToByteArrayInputStream(message), true);
                return true;
              }
            }
          }
          finally {
            ReentrantLock lock = bufferColor.getLock();
            lock.lock();
            try {
              bufferColor.wake();
            }
            finally {
              lock.unlock();
            }
          }
        }

        BufferHelper.encodeAndWrite(buffer, bufferColor, message);

        if (messageCount.incrementAndGet() > 5 && !lastTransmissionWithin(secs(3))) {
          // disconnect this client
          stopQueue();
        }
      }
      finally {
        activateActivationCallback();
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
    if (!queueRunning) return;

    try {
      if (isDirectChannelOpen()) {
        UnwrappedByteArrayOutputStream outputStream = new UnwrappedByteArrayOutputStream();
        buffer.read(outputStream, bufferColor, new BufferHelper.MultiMessageHandlerCallback());
        // directSocketChannel.write(new TextWebSocketFrame(new String(outputStream.toByteArray(), 0, outputStream.size())));
        directSocketChannel.write(new String(outputStream.toByteArray(), 0, outputStream.size()));
      }
      else {
        BufferHelper.encodeAndWriteNoop(buffer, bufferColor);
      }

      activateActivationCallback();
    }
    catch (Throwable e) {
      log.debug("unable to wake queue: " + session.getSessionId());
      stopQueue();
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
    //  synchronized (activationLock) {
    this.activationCallback = activationCallback;
    //   }
  }

  private void activateActivationCallback() {
//    if (activationCallback != null) {
    synchronized (activationLock) {
      if (activationCallback != null) {
        activationCallback.activate(this);
      }
    }
    //  }
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
    return !queueRunning || !isDirectChannelOpen() || (((nanoTime() - lastTransmission) > TIMEOUT));
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
    return useDirectSocketChannel && directSocketChannel.isConnected();
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
  public void setDirectSocketChannel(final QueueChannel channel) {
    this.directSocketChannel = channel;
    this.useDirectSocketChannel = channel != null;

    if (useDirectSocketChannel) {
      log.debug("queue " + getSession().getSessionId() + " transitioned to direct channel mode.");
    }
  }

  @Override
  public String toString() {
    return "MessageQueueImpl{" +
            "session=" + session +
            '}';
  }
}
