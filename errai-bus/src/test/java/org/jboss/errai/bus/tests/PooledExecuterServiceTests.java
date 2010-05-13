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
import org.jboss.errai.bus.server.async.scheduling.PooledExecutorService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;


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

        for (int i = 0; i < 10000; i++) {
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

    public void testHighLoad() throws InterruptedException {
        PooledExecutorService svc = new PooledExecutorService(100);
        svc.start();

        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(100);

        final ConcurrentTestObj baseline = generateBaseLine();
        System.out.println("Generated baseline ...");

        double average1 = 0;
        double average2 = 0;


        for (int x = 0; x < 100; x++) {
            final ConcurrentTestObj test = new ConcurrentTestObj();

            long tm = System.nanoTime();

            svc.execute(new Runnable() {
                public void run() {
                    startTime1 = System.nanoTime();
                }
            });

            for (int i = 0; i < 10000; i++) {
//                if ((i % 1000) == 1) {
//                    System.out.println("(A) Scheduled " + i + " of 10,000");
//                }
                svc.execute(new Runnable() {
                    public void run() {
                        synchronized (test) {
                            test.add(test.getLast() + 2);
                        }
                    }
                });
            }

            System.out.println("(A) = " + (tm = (System.nanoTime() - tm)));

            svc.execute(new Runnable() {
                public void run() {
                    incrementTimer1(System.nanoTime() - startTime1);
                }
            });

            average1 += tm;

            final ConcurrentTestObj test2 = new ConcurrentTestObj();
            tm = System.nanoTime();


            executor.execute(new Runnable() {
                public void run() {
                    startTime2 = System.nanoTime();
                }
            });

            for (int i = 0; i < 10000; i++) {
//                if ((i % 1000) == 1) {
//                    System.out.println("(B) Scheduled " + i + " of 10,000");
//                }
                executor.execute(new Runnable() {
                    public void run() {
                        synchronized (test2) {
                            long tm = System.nanoTime();
                            test2.add(test2.getLast() + 2);
                            incrementTimer2(System.nanoTime() - tm);
                        }
                    }
                });
            }

            System.out.println("(B) = " + (tm = (System.nanoTime() - tm)));

            executor.execute(new Runnable() {
                public void run() {
                    incrementTimer2(System.nanoTime() - startTime2);
                }
            });

            average2 += tm;

            svc.execute(new Runnable() {
                public void run() {
                    try {
                        assertEquals(baseline.getSum(), test.getSum());
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            executor.execute(new Runnable() {
                public void run() {
                    try {
                        assertEquals(baseline.getSum(), test2.getSum());
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        System.out.println("Average Scheduling Perf (A):" + (average1 / 100));
        System.out.println("Average Scheduling Perf (B):" + (average2 / 100));
        System.out.println("Average Run Perf (A):" + timer1);
        System.out.println("Average Run Perf (B):" + timer2);
    }
}
