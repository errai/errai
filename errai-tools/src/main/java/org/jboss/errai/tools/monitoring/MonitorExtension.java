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

package org.jboss.errai.tools.monitoring;

import com.google.inject.Inject;
import com.google.inject.Provider;
import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.framework.BusMonitor;
import org.jboss.errai.bus.client.framework.MessageBus;
import org.jboss.errai.bus.client.framework.SubscriptionEvent;
import org.jboss.errai.bus.server.ServerMessageBusImpl;
import org.jboss.errai.bus.server.annotations.ExtensionComponent;
import org.jboss.errai.bus.server.ext.ErraiConfigExtension;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@ExtensionComponent
public class MonitorExtension implements ErraiConfigExtension {
    private MessageBus bus;
    private MainMonitorGUI monitorGUI;

    private ThreadPoolExecutor workers;

    @Inject
    public MonitorExtension(MessageBus bus) {
        this.bus = bus;
    }

    public void configure(Map<Class, Provider> bindings, Map<String, Provider> resourceProviders) {
        if (Boolean.getBoolean("errai.tools.bus_monitor_attach")) {
            monitorGUI = new MainMonitorGUI();

            workers = new ThreadPoolExecutor(2, 10, 1, TimeUnit.MINUTES, new ArrayBlockingQueue<Runnable>(10, false));

            ServerMessageBusImpl sBus = (ServerMessageBusImpl) bus;

            sBus.attachMonitor(new BusMonitor() {
                MessageBus bus;

                public void attach(MessageBus bus) {
                    this.bus = bus;
                }

                public void notifyNewSubscriptionEvent(final SubscriptionEvent event) {
                    System.out.println("ADD:" + event.getSubject());
                    workers.execute(new Runnable() {
                        public void run() {
                            if (event.isRemote()) {
                                monitorGUI.getRemoteBus(event.getSessionData()).addServiceName(event.getSubject());

                            } else {
                                monitorGUI.getServerMonitorPanel().addServiceName(event.getSubject());
                            }
                        }
                    });
                }

                public void notifyUnSubcriptionEvent(final SubscriptionEvent event) {
                    workers.execute(new Runnable() {
                        public void run() {
                            if (event.isRemote()) {
                                monitorGUI.getRemoteBus(event.getSessionData()).removeServiceName(event.getSubject());

                            } else {
                                monitorGUI.getServerMonitorPanel().removeServiceName(event.getSubject());
                            }
                        }
                    });
                }

                public void notifyQueueAttached(final Object queueId, Object queueInstance) {
                    workers.execute(new Runnable() {
                        public void run() {
                            monitorGUI.attachRemoteBus(queueId);
                        }
                    });

                }

                public void notifyIncomingMessageFromRemote(Object queue, Message message) {
                    updateMonitor(message);
                }

                public void notifyOutgoingMessageToRemote(Object queue, Message message) {
                    updateMonitor(message);
                }

                public void notifyInBusMessage(Message message) {
                    updateMonitor(message);
                }

                public void notifyMessageDeliveryFailure(Object queue, Message mesage) {
                }

                private void updateMonitor(final Message m) {
                    workers.execute(new Runnable() {
                        public void run() {
                            ServiceActityMonitor s = monitorGUI.getServerMonitorPanel().getMonitor(m.getSubject());
                            if (s != null) s.notifyMessage(m);
                        }
                    });
                }
            });


            monitorGUI.setVisible(true);
        }
    }
}
