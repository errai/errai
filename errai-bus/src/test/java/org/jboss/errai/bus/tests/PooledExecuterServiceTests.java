/*
 * Copyright 2009 JBoss, a divison Red Hat, Inc
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

package org.jboss.errai.bus.tests;

import junit.framework.TestCase;
import org.jboss.errai.bus.client.api.base.TimeUnit;
import org.jboss.errai.bus.server.async.scheduling.PooledExecutorService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;


public class PooledExecuterServiceTests extends TestCase {
  class ConcurrentTestObj {
    public final List<Long> list = Collections.synchronizedList(new ArrayList<Long>(10000));

    public void add(Long longVal) {
      synchronized (list) {
        list.add(longVal);
      }
    }

    public Long getLast() {
      synchronized (list) {
        if (list.isEmpty()) return 1l;
        else return list.get(list.size() - 1);
      }
    }

    public Long getSum() {
      synchronized (list) {
        long sum = 0;
        for (Long l : list) {
          sum += l;
        }
        return sum;
      }
    }
  }

  private ConcurrentTestObj generateBaseLine() {
    ConcurrentTestObj baseline = new ConcurrentTestObj();

    for (int i = 0; i < 1000; i++) {
      baseline.add(baseline.getLast() + 2);
    }

    return baseline;
  }

  private long startTime1 = 0;
  private long startTime2 = 0;


  private long timer1 = 0;
  private final Object timerLock1 = new Object();

  private long timer2 = 0;
  private final Object timerLock2 = new Object();

  public void incrementTimer1(long amt) {
    synchronized (timerLock1) {
      timer1 += amt;
    }
  }

  public void incrementTimer2(long amt) {
    synchronized (timerLock2) {
      timer2 += amt;
    }
  }

  public void testExecutorServiceStress() {
    PooledExecutorService svc = new PooledExecutorService(10, PooledExecutorService.SaturationPolicy.CallerRuns);
    svc.start();

    int size = 50;
    int seconds = 20;

    final int[] vals = new int[size];
    final int[] vals2 = new int[size];

    for (int i = 0; i < size; i++) {
      final int x = i;
      svc.scheduleRepeating(new Runnable() {
        public void run() {
          vals[x]++;
        }
      }, TimeUnit.MILLISECONDS, 0, 1);
    }

    boolean loop = true;
    int loops = 0;
    try {
      do {
        Thread.sleep(1000);
        System.out.println("vals=" + Arrays.toString(vals));

        if (loops != 0)
          for (int i = 0; i < vals.length; i++) {
            assertTrue(vals[i] > vals2[i]);
          }

        for (int i = 0; i < vals.length; i++) {
          vals2[i] = vals[i];
        }

        if (++loops == seconds) loop = false;
      } while (loop);
    }
    catch (InterruptedException e) {
      e.printStackTrace();
    }
  }
}
