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

import org.jboss.errai.bus.client.api.messaging.Message;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ActivityProcessor {
  private List<List<MessageMonitor>> messageMonitors = new ArrayList<List<MessageMonitor>>(20);
  private ThreadPoolExecutor workers;

  public ActivityProcessor() {
    messageMonitors = new ArrayList<List<MessageMonitor>>(20);
    workers = new ThreadPoolExecutor(2, Runtime.getRuntime().availableProcessors(), 30, TimeUnit.SECONDS,
            new ArrayBlockingQueue<Runnable>(500, false));
    workers.setRejectedExecutionHandler(new RejectedExecutionHandler() {
      public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
        // just run on calling thread.
        r.run();
      }
    });
  }

  private void padList(int size) {
    for (int i = messageMonitors.size() - 1; i < size; i++) {
      messageMonitors.add(new ArrayList<MessageMonitor>(10));
    }
  }

  public Handle registerEvent(EventType type, MessageMonitor monitor) {
    padList(type.ordinal());
    messageMonitors.get(type.ordinal()).add(monitor);
    return new Handle(this, type, monitor);
  }

  public void notifyEvent(final long time, final EventType type, final SubEventType subType,
                          final String fromBus, final String toBus, final String subject,
                          final Message message, final Throwable error, final boolean replay) {
    workers.execute(new Runnable() {
      public void run() {
        MessageEvent evt = type == EventType.ERROR ? new MessageEvent() {
          public long getTime() {
            return time;
          }

          public SubEventType getSubType() {
            return subType;
          }

          public String getSubject() {
            return subject;
          }

          public String getFromBus() {
            return fromBus;
          }

          public String getToBus() {
            return toBus;
          }

          public Object getContents() {
            return error;
          }

          public boolean isReplay() {
            return replay;
          }
        } : new MessageEvent() {
          public long getTime() {
            return time;
          }

          public SubEventType getSubType() {
            return subType;
          }

          public String getSubject() {
            return subject;
          }

          public String getFromBus() {
            return fromBus;
          }

          public String getToBus() {
            return toBus;
          }

          public Object getContents() {
            return message;
          }

          public boolean isReplay() {
            return replay;
          }
        };

        if (type.ordinal() > messageMonitors.size()) {
          return;
        }

        for (MessageMonitor monitor : messageMonitors.get(type.ordinal())) {
          monitor.monitorEvent(evt);
        }
      }
    });
  }

  public class Handle {
    private ActivityProcessor processor;
    private EventType type;
    private MessageMonitor monitor;

    public Handle(ActivityProcessor processor, EventType type, MessageMonitor monitor) {
      this.processor = processor;
      this.type = type;
      this.monitor = monitor;
    }

    public void dispose() {
      processor.messageMonitors.get(type.ordinal()).remove(monitor);
    }
  }
}
