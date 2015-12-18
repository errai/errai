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

package org.jboss.errai.bus.server.async.scheduling;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.LockSupport;

public class ThreadWorkerPool {
  /**
   * The worker pool.
   */
  private final List<ThreadWorker> workers;
  private final TaskProvider provider;

  private int maximumPoolSize = Runtime.getRuntime().availableProcessors();

  private volatile boolean stop = false;

  public ThreadWorkerPool(TaskProvider provider) {
    this.workers = new CopyOnWriteArrayList<ThreadWorker>();
    this.provider = provider;
  }

  public ThreadWorkerPool(TaskProvider provider, int maximumPoolSize) {
    this.workers = new CopyOnWriteArrayList<ThreadWorker>();
    this.provider = provider;
    this.maximumPoolSize = maximumPoolSize;
  }

  public void addWorker() {
    synchronized (this) {
      if (workers.size() == maximumPoolSize) return;

      if (stop) {
        return;
      }

      ThreadWorker worker = new ThreadWorker(provider);
      workers.add(worker);
      worker.start();
    }
  }

  public void removeWorker() {
    synchronized (this) {
      if (workers.size() <= 1) return;

      if (stop) {
        return;
      }

      ThreadWorker worker = workers.get(workers.size() - 1);
      worker.requestStop();
      workers.remove(worker);
    }
  }

  public void startPool() {
    addWorker();
  }

  public void requestStopAll() {
    synchronized (this) {
      stop = true;

      for (ThreadWorker worker : workers)
        worker.requestStop();


      Thread shutdownThread = new Thread() {
        @Override
        public void run() {
          boolean allStopped = false;
          boolean anyActive;
          while (!allStopped) {
            LockSupport.parkNanos(1000);

            anyActive = false;
            for (ThreadWorker worker : workers) {

              if (!worker.isStopped()) {
                anyActive = true;
              }
            }

            if (!anyActive) {
              allStopped = true;
            }
          }
        }
      };

      shutdownThread.setPriority(Thread.MIN_PRIORITY);
      shutdownThread.start();

      try {
        shutdownThread.join();
      }
      catch (InterruptedException e) {
        System.err.println("was interuppted waiting to shutdown thread worker pool");
        e.printStackTrace();
      }
    }
  }
}
