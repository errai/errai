/*
 * Copyright 2010 JBoss, a divison Red Hat, Inc
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
import org.jboss.errai.bus.client.api.base.MessageDeliveryFailure;
import org.jboss.errai.bus.client.framework.RoutingFlags;
import org.jboss.errai.bus.client.util.ErrorHelper;
import org.jboss.errai.bus.server.async.TimedTask;
import org.jboss.errai.bus.server.service.ErraiService;
import org.jboss.errai.bus.server.service.ErraiServiceConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * The <tt>WorkerFactory</tt> maintains a pool of <tt>Worker</tt>s, and takes care of running and terminating them
 */
public class WorkerFactory {
  private static final int DEFAULT_DELIVERY_QUEUE_SIZE = 100;
  private static final int DEFAULT_THREAD_POOL_SIZE = Runtime.getRuntime().availableProcessors();

  private static final String CONFIG_ASYNC_THREAD_POOL_SIZE = "errai.async.thread_pool_size";
  private static final String CONFIG_ASYNC_WORKER_TIMEOUT = "errai.async.worker.timeout";
  private static final String CONFIG_ASYNC_DELIVERY_QUEUE_SIZE = "errai.async.delivery.queue_size";

  private Worker[] workerPool;

  private ErraiService svc;

  private SaturationPolicy saturationPolicy = SaturationPolicy.CallerRuns;

  private BlockingQueue<Message> messages;

  private int poolSize = DEFAULT_THREAD_POOL_SIZE;
  private int deliveryQueueSize = DEFAULT_DELIVERY_QUEUE_SIZE;
  private long workerTimeout = Boolean.getBoolean("org.jboss.errai.debugmode") ? seconds(360) : seconds(30);

  private Logger log = LoggerFactory.getLogger(this.getClass());

  /**
   * Initializes the worker factory with a new thread group, service, all the properties, messages and workers
   *
   * @param svc - the <tt>ErraiService</tt> that is to be associated to this factory of workers
   */
  public WorkerFactory(ErraiService svc) {
    this.svc = svc;

    ErraiServiceConfigurator cfg = svc.getConfiguration();

    if (cfg.hasProperty(CONFIG_ASYNC_DELIVERY_QUEUE_SIZE)) {
      deliveryQueueSize = Integer.parseInt(cfg.getProperty(CONFIG_ASYNC_DELIVERY_QUEUE_SIZE));
    }

    if (cfg.hasProperty(CONFIG_ASYNC_THREAD_POOL_SIZE)) {
      poolSize = Integer.parseInt(cfg.getProperty(CONFIG_ASYNC_THREAD_POOL_SIZE));
    }

    if (cfg.hasProperty(CONFIG_ASYNC_WORKER_TIMEOUT)) {
      workerTimeout = seconds(Integer.parseInt(cfg.getProperty(CONFIG_ASYNC_WORKER_TIMEOUT)));
    }

    this.messages = new ArrayBlockingQueue<Message>(deliveryQueueSize);

    log.info("initializing async worker pools (poolSize: " + poolSize + "; workerTimeout: " + workerTimeout + ")");

    this.workerPool = new Worker[poolSize];

    for (int i = 0; i < poolSize; i++) {
      workerPool[i] = new Worker(this, svc);
    }

    if (svc.getBus() instanceof ServerMessageBusImpl) {
      ServerMessageBusImpl busImpl = (ServerMessageBusImpl) svc.getBus();
      /**
       * Add a housekeeper task to the bus housekeeper to timeout long-running tasks.
       */
      busImpl.getScheduler().addTask(new TimedTask() {
        {
          period = 1000;
        }

        public void run() {
          for (Worker w : workerPool) {
            if (!w.isValid()) {
              log.warn("Terminating worker.  Process exceeds maximum time to live.");
              w.timeoutInterrupt();
            }
          }
        }

        public void setExitHandler(Runnable runnable) {
          //To change body of implemented methods use File | Settings | File Templates.
        }

        public boolean isFinished() {
          return false;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public String toString() {
          return "WorkerTimeout";
        }
      });
    }

    startPool();
  }

  /**
   * Attempts to deliver the specified message globally
   *
   * @param m - message to be delivered
   */
  public void deliverGlobal(Message m) throws InterruptedException {
    if (messages.offer(m, 30, java.util.concurrent.TimeUnit.SECONDS)) {
      return;
    } else {
      switch (saturationPolicy) {
        case CallerRuns:
          svc.getBus().sendGlobal(m);
          break;
        case Fail:
          sendDeliveryFailure(m);
          throw new RuntimeException("delivery queue is overloaded!");
      }
    }
  }

  /**
   * Attempts to send the message
   *
   * @param m - message to be sent
   */
  public void deliver(Message m) throws InterruptedException {
    m.setFlag(RoutingFlags.NonGlobalRouting);
    if (messages.offer(m, 30, java.util.concurrent.TimeUnit.SECONDS)) {
      return;
    } else {
      switch (saturationPolicy) {
        case CallerRuns:
          svc.getBus().send(m);
          break;
        case Fail:
          sendDeliveryFailure(m);
          throw new RuntimeException("delivery queue is overloaded!");
      }
    }
  }

  private void sendDeliveryFailure(Message m) {
    MessageDeliveryFailure mdf
            = new MessageDeliveryFailure("could not deliver message because the outgoing queue is full");

    if (m.getErrorCallback() == null || m.getErrorCallback().error(m, mdf)) {
      ErrorHelper.sendClientError(svc.getBus(), m, mdf.getMessage(), mdf);
      throw mdf;
    }
  }

  /**
   * Gets the messages in the queue
   *
   * @return the messages in the queue
   */
  protected BlockingQueue<Message> getMessages() {
    return messages;
  }

  /**
   * Gets the timeout
   *
   * @return the timeout time
   */
  protected long getWorkerTimeout() {
    return workerTimeout;
  }

  /**
   * Starts execution of all the threads in the pool of threads
   */
  public void startPool() {
    log.info("starting worker pool.");
    for (int i = 0; i < poolSize; i++) {
      workerPool[i].start();
    }
  }

  private long seconds(int seconds) {
    return seconds * 1000;
  }

  enum SaturationPolicy {
    Fail, CallerRuns
  }
}
