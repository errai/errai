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
import org.jboss.errai.bus.client.api.MessageCallback;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.framework.BusMonitor;
import org.jboss.errai.bus.client.framework.MessageBus;
import org.jboss.errai.bus.client.framework.SubscriptionEvent;
import org.jboss.errai.bus.client.protocols.BusCommands;
import org.jboss.errai.bus.client.protocols.MessageParts;
import org.jboss.errai.bus.client.protocols.MonitorCommands;
import org.jboss.errai.bus.server.QueueSession;
import org.jboss.errai.bus.server.ServerMessageBusImpl;
import org.jboss.errai.bus.server.annotations.ExtensionComponent;
import org.jboss.errai.bus.server.ext.ErraiConfigExtension;

import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static java.lang.System.currentTimeMillis;

@ExtensionComponent
public class MonitorExtension implements ErraiConfigExtension {
    public static String MONITOR_SVC = "BusMonitorService_000";

    private MessageBus bus;
    private ActivityProcessor proc;

    @Inject
    public MonitorExtension(MessageBus bus) {
        this.bus = bus;
    }

    public void configure(Map<Class, Provider> bindings, Map<String, Provider> resourceProviders) {
        if (Boolean.getBoolean("errai.tools.bus_monitor_attach")) {
            ServerMessageBusImpl sBus = (ServerMessageBusImpl) bus;

            proc = new ActivityProcessor();

            try {
                new Bootstrapper(proc, bus).init();
            }
            catch (Throwable t) {
                t.printStackTrace();
            }
            sBus.attachMonitor(new BusMonitor() {
                MessageBus bus;

                public void attach(MessageBus bus) {
                    this.bus = bus;

//                    bus.subscribe(MONITOR_SVC, new MessageCallback() {
//                        public void callback(Message message) {
//                            String queueId = message.getResource(QueueSession.class, "Session").getSessionId();
//                            switch (MonitorCommands.valueOf(message.getCommandType())) {
//                                case SuccessfulAttach:
//                                     proc.notifyEvent(EventType.BUS_EVENT, SubEventType.REMOTE_ATTACHED, String.valueOf(queueId),
//                                            "Server", null, null, null, false);
//                                    break;
//                                case SubscribeEvent:
//                                     proc.notifyEvent(EventType.BUS_EVENT, SubEventType.SERVER_SUBSCRIBE,
//                                "Server", "Server", queueId, null, null, false);
//                                    break;
//                                case UnsubcribeEvent:
//                                    proc.notifyEvent(EventType.BUS_EVENT, SubEventType.SERVER_UNSUBSCRIBE,
//                                            "Server", "Server", queueId, null, null, false);
//                                     break;
//                                case IncomingMessage:
//                                    proc.notifyEvent(EventType.MESSAGE, SubEventType.RX_REMOTE, queueId,
//                                            "Server", message.getSubject(), message, null, false);
//                                    break;
//
//                            }
//                        }
//                    });
                }

                public void notifyNewSubscriptionEvent(final SubscriptionEvent event) {
                    if (MONITOR_SVC.equals(event.getSubject())) return;

                    if (event.isRemote()) {
                        proc.notifyEvent(EventType.BUS_EVENT, SubEventType.REMOTE_SUBSCRIBE,
                                String.valueOf(event.getSessionData()), "Server", event.getSubject(), null, null, false);
                    } else {
                        proc.notifyEvent(EventType.BUS_EVENT, SubEventType.SERVER_SUBSCRIBE,
                                "Server", "Server", event.getSubject(), null, null, false);
                    }
                }

                public void notifyUnSubcriptionEvent(final SubscriptionEvent event) {
                    if (MONITOR_SVC.equals(event.getSubject())) return;

                    if (event.isRemote()) {
                        proc.notifyEvent(EventType.BUS_EVENT, SubEventType.REMOTE_UNSUBSCRIBE,
                                String.valueOf(event.getSessionData()), "Server", event.getSubject(), null, null, false);
                    } else {
                        proc.notifyEvent(EventType.BUS_EVENT, SubEventType.SERVER_UNSUBSCRIBE,
                                "Server", "Server", event.getSubject(), null, null, false);
                    }
                }

                 public void notifyQueueAttached(final Object queueId, Object queueInstance) {
//                    MessageBuilder.createMessage()
//                            .toSubject("ClientBus")
//                            .command(BusCommands.RemoteMonitorAttach)
//                            .with(MessageParts.Subject, MONITOR_SVC)
//                            .with(MessageParts.SessionID, String.valueOf(queueId))
//                            .noErrorHandling().sendNowWith(bus);

                    proc.notifyEvent(EventType.BUS_EVENT, SubEventType.REMOTE_ATTACHED, String.valueOf(queueId), "Server", null, null, null, false);
                }

                public void notifyIncomingMessageFromRemote(Object queue, final Message message) {
                  //  if (MONITOR_SVC.equals(message.getSubject())) return;

                    proc.notifyEvent(EventType.MESSAGE, SubEventType.RX_REMOTE, String.valueOf(queue), "Server", message.getSubject(), message, null, false);
                }

                public void notifyOutgoingMessageToRemote(Object queue, final Message message) {
                //    if (MONITOR_SVC.equals(message.getSubject())) return;
                    proc.notifyEvent(EventType.MESSAGE, SubEventType.TX_REMOTE, "Server", String.valueOf(queue), message.getSubject(), message, null, false);
                }

                public void notifyInBusMessage(Message message) {
                    proc.notifyEvent(EventType.MESSAGE, SubEventType.INBUS, "Server", "Server", message.getSubject(), message, null, false);
                }

                public void notifyMessageDeliveryFailure(Object queue, Message message, Throwable throwable) {
                    proc.notifyEvent(EventType.ERROR, SubEventType.INBUS, String.valueOf(queue), "Server", message.getSubject(), message, throwable, false);
                }
            });

        }
    }
}
