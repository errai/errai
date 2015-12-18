/*
 * Copyright (C) 2011 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.tools.monitoring;

import com.google.inject.Inject;
import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.bus.client.api.BusMonitor;
import org.jboss.errai.bus.client.api.messaging.MessageBus;
import org.jboss.errai.bus.client.framework.SubscriptionEvent;
import org.jboss.errai.bus.server.ServerMessageBusImpl;
import org.jboss.errai.common.server.api.annotations.ExtensionComponent;
import org.jboss.errai.common.server.api.ErraiConfig;
import org.jboss.errai.common.server.api.ErraiConfigExtension;

@ExtensionComponent
public class MonitorExtension implements ErraiConfigExtension {
  public static String MONITOR_SVC = "BusMonitorService_000";

  private MessageBus bus;
  private ActivityProcessor proc;

  @Inject
  public MonitorExtension(MessageBus bus) {
    this.bus = bus;
  }

  public void configure(ErraiConfig config) {
    if (Boolean.getBoolean("errai.tools.bus_monitor_attach")) {

      proc = new ActivityProcessor();
      ServerMessageBusImpl sBus = (ServerMessageBusImpl) bus;

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
        }

        public void notifyNewSubscriptionEvent(final SubscriptionEvent event) {
          if (MONITOR_SVC.equals(event.getSubject())) return;

          if (event.isRemote()) {
            proc.notifyEvent(System.currentTimeMillis(), EventType.BUS_EVENT, SubEventType.REMOTE_SUBSCRIBE,
                event.getSessionId(), "Server", event.getSubject(), null, null, false);
          }
          else {
            proc.notifyEvent(System.currentTimeMillis(), EventType.BUS_EVENT, SubEventType.SERVER_SUBSCRIBE,
                "Server", "Server", event.getSubject(), null, null, false);
          }
        }

        public void notifyUnSubcriptionEvent(final SubscriptionEvent event) {
          if (MONITOR_SVC.equals(event.getSubject())) return;

          if (event.isRemote()) {
            proc.notifyEvent(System.currentTimeMillis(), EventType.BUS_EVENT, SubEventType.REMOTE_UNSUBSCRIBE,
                event.getSessionId(), "Server", event.getSubject(), null, null, false);
          }
          else {
            proc.notifyEvent(System.currentTimeMillis(), EventType.BUS_EVENT, SubEventType.SERVER_UNSUBSCRIBE,
                "Server", "Server", event.getSubject(), null, null, false);
          }
        }

        public void notifyQueueAttached(final String queueId, Object queueInstance) {
          proc.notifyEvent(System.currentTimeMillis(), EventType.BUS_EVENT, SubEventType.REMOTE_ATTACHED, queueId, "Server", null, null, null, false);
        }

        public void notifyQueueDetached(String queueId, Object queueInstance) {
          proc.notifyEvent(System.currentTimeMillis(), EventType.BUS_EVENT, SubEventType.REMOTE_DETATCHED, queueId, "Server", null, null, null, false);
        }

        public void notifyIncomingMessageFromRemote(String queue, final Message message) {
          proc.notifyEvent(System.currentTimeMillis(), EventType.MESSAGE, SubEventType.RX_REMOTE, String.valueOf(queue), "Server", message.getSubject(), message, null, false);
        }

        public void notifyOutgoingMessageToRemote(String queue, final Message message) {
          proc.notifyEvent(System.currentTimeMillis(), EventType.MESSAGE, SubEventType.TX_REMOTE, "Server", String.valueOf(queue), message.getSubject(), message, null, false);
        }

        public void notifyInBusMessage(Message message) {
          proc.notifyEvent(System.currentTimeMillis(), EventType.MESSAGE, SubEventType.INBUS, "Server", "Server", message.getSubject(), message, null, false);
        }

        public void notifyMessageDeliveryFailure(String queue, Message message, Throwable throwable) {
          proc.notifyEvent(System.currentTimeMillis(), EventType.ERROR, SubEventType.INBUS, String.valueOf(queue), "Server", message.getSubject(), message, throwable, false);
        }
      });


    }
  }
}
