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

package org.jboss.errai.bus.server.async;


import java.util.ArrayList;
import java.util.List;

public class ThreadBalancedSchedulerService implements SchedulerService {
    private static final Thread houseKeeper;
    private static final List<SchedulerInstance> instances;
    private static final CurrentInstance current;

    static {
        houseKeeper = new Housekeeper();
        instances = new ArrayList<SchedulerInstance>(10);
        current = new CurrentInstance();

        houseKeeper.start();
    }

    public void addTask(TimedTask task) {
        synchronized (current) {
            current.calculateLoad(task);
            if (current.isAtLoad()) {
                newSchedulerInstance();
                System.out.println("** NEW THREAD **");
            }

            _addTask(task);
        }
    }

    private void _addTask(TimedTask task) {
        synchronized (current) {
            SchedulerInstance instance = getSchedulerInstance();
            instance.addTask(task);
        }
    }

    private static SchedulerInstance getSchedulerInstance() {
        int cursor = current.getCursor();
        if (instances.size() - 1 < cursor) {
            newSchedulerInstance();
        }
        return instances.get(cursor);
    }

    private static void newSchedulerInstance() {
        synchronized (current) {
            if (!instances.isEmpty() && current.cursor < instances.size()) {
                SchedulerInstance inst;
                for (int i = current.cursor; i < instances.size(); i++) {
                    inst = instances.get(i);
                    if (!inst.isClosed()) {
                        current.setCursor(i);
                    }
                }
            } else {
                current.mark();
                SimpleSchedulerService svc = new SimpleSchedulerService();
                svc.setAutoStartStop(true);
                instances.add(new SchedulerInstance(current.cursor, svc));
            }
        }
    }

    public void requestStop() {
        synchronized (current) {
            for (SchedulerInstance instance : instances)
                instance.getScheduler().requestStop();
        }
    }

    public void start() {
        // do nothing.
    }

    private static class SchedulerInstance {
        private int cursor;
        private SimpleSchedulerService scheduler;
        private Thread thread;

        private int count;
        private double load;

        private boolean closed = false;

        private SchedulerInstance(int cursor, SimpleSchedulerService scheduler) {
            this.cursor = cursor;
            this.scheduler = scheduler;

            thread = new Thread(scheduler);
            thread.start();
        }

        public void addTask(TimedTask task) {
            scheduler.addTask(task);
        }

        public SimpleSchedulerService getScheduler() {
            return scheduler;
        }


        public void setCountAndLoad(int count, double load) {
            this.count = count;
            this.load = load;
        }

        public void reset() {
            count = 0;
            load = 0;
        }

        public boolean isClosed() {
            return closed;
        }

        public void setClosed(boolean closed) {
            this.closed = closed;
        }
    }

    private static class CurrentInstance {
        private int cursor = -1;

        private int count;
        private double load;

        public void calculateLoad(TimedTask task) {
            if (task.getPeriod() != -1) {
                count++;
                load = (60000d - (double) task.getPeriod()) / 60000d;
            }
        }

        public boolean isAtLoad() {
            return (count > 10 || load > 0.99d);
        }

        public void mark() {
            SchedulerInstance inst = getSchedulerInstance();
            inst.setCountAndLoad(count, load);
            inst.setClosed(true);

            cursor++;
            count = 0;
            load = 0;
        }

        public int getCursor() {
            return cursor;
        }

        public void setCursor(int cursor) {
            this.cursor = cursor;
        }
    }

    private static class Housekeeper extends Thread {
        private boolean running = true;

        @Override
        public void run() {
            while (running) {
                try {
                    Thread.sleep(5000);
                    houseKeep();
                }
                catch (InterruptedException e) {
                    if (!running) return;
                }
            }
        }

        private void houseKeep() {
            synchronized (current) {
                for (SchedulerInstance instance : instances) {
                    if (instance.scheduler.isFinished()) {

                        instance.setClosed(false);
                        instance.reset();

                        if (instance.cursor < current.cursor) {
                            current.setCursor(instance.cursor);
                        }
                    } else {
                        final CurrentInstance curr = new CurrentInstance();
                        instance.getScheduler().visitAllTasks(new SimpleSchedulerService.TaskVisitor() {
                            public void visit(TimedTask task) {
                                curr.calculateLoad(task);
                            }
                        });
                        if (!curr.isAtLoad()) {
                            instance.setClosed(false);
                            instance.setCountAndLoad(curr.count, curr.load);

                            if (instance.cursor < current.cursor) {
                                current.setCursor(instance.cursor);
                            }
                        }
                    }
                }

                System.out.println("** Housekeeper: active threads = " + instances.size());
            }
        }

    }
}
