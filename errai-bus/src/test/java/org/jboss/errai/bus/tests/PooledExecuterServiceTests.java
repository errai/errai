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


    double average1 = 0;
    double average2 = 0;

    public void testHighLoad() throws InterruptedException {
        PooledExecutorService svc = new PooledExecutorService(10);
        svc.start();

        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(100);

        final ConcurrentTestObj baseline = generateBaseLine();
        System.out.println("Generated baseline ...");


        for (int x = 0; x < 30; x++) {
            final ConcurrentTestObj test = new ConcurrentTestObj();

            long tm = System.nanoTime();

            svc.execute(new Runnable() {
                public void run() {
                    startTime1 = System.nanoTime();
                }
            });

            for (int i = 0; i < 1000; i++) {
                svc.execute(new Runnable() {
                    public void run() {
                        synchronized (test) {
                            test.add(test.getLast() + 2);
                        }

           //             MVEL.eval("for (int i = 0; i < 10000; i++) { }");


                        //      System.out.println("x=" + x);


//                        try {
//                            Thread.sleep(10);
//                        }
//                        catch (InterruptedException e) {
//                        }
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

            for (int i = 0; i < 1000; i++) {
                executor.execute(new Runnable() {
                    public void run() {
                        synchronized (test2) {
                            test2.add(test2.getLast() + 2);
                        }


                  //      MVEL.eval("for (int i = 0; i < 10000; i++) { }");

                        //      System.out.println("x=" + x);
//                        try {
//                            Thread.sleep(10);
//                        }
//                        catch (InterruptedException e) {
//                        }

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
                        System.out.println("CHECK");
                        assertEquals(baseline.getSum(), test.getSum());
                        System.out.println("(A) GOOD!");
                    }
                    catch (Throwable e) {
                        System.out.println("(A) ERR");
                        e.printStackTrace();
                    }

                }
            });

            executor.execute(new Runnable() {
                public void run() {
                    try {
                        System.out.println("CHECK2");
                        assertEquals(baseline.getSum(), test2.getSum());
                        System.out.println("(B) GOOD!");
                    }
                    catch (Throwable e) {
                        System.out.println("(B) ERR");
                        e.printStackTrace();
                    }
                }
            });
        }

        System.out.println("Schedule");
        svc.execute(new Runnable() {
            public void run() {
                System.out.println("Average Scheduling Perf (A):" + (average1 / 100));
                System.out.println("Average Run Perf (A):" + timer1);
            }
        });

        System.out.println("Schedule2");
        executor.execute(new Runnable() {
            public void run() {
                System.out.println("Average Scheduling Perf (B):" + (average2 / 100));
                System.out.println("Average Run Perf (B):" + timer2);
            }
        });

    }
}
