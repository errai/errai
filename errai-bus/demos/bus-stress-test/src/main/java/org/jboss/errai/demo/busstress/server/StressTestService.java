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
package org.jboss.errai.demo.busstress.server;

import javax.inject.Inject;

import org.jboss.errai.bus.client.api.BusErrorCallback;
import org.jboss.errai.bus.client.api.SubscribeListener;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.bus.client.api.messaging.MessageBus;
import org.jboss.errai.bus.client.api.messaging.MessageCallback;
import org.jboss.errai.bus.client.api.messaging.RequestDispatcher;
import org.jboss.errai.bus.client.framework.SubscriptionEvent;
import org.jboss.errai.bus.server.annotations.Service;
import org.jboss.errai.common.client.api.ResourceProvider;
import org.jboss.errai.common.client.api.tasks.AsyncTask;
import org.jboss.errai.common.client.protocols.MessageParts;
import org.jboss.errai.common.client.util.TimeUnit;

@Service
public class StressTestService implements MessageCallback {

  private volatile int nextBroadcastId;
  private volatile AsyncTask broadcastTask;

  @Inject
  public StressTestService(final RequestDispatcher dispatcher, MessageBus bus) {
    bus.addSubscribeListener(new SubscribeListener() {

      @Override
      public void onSubscribe(SubscriptionEvent event) {
        synchronized (StressTestService.class) {
          if (event.getCount() > 0 && (broadcastTask == null || broadcastTask.isCancelled())) {
            System.out.println("Starting broadcast task");
            broadcastTask = MessageBuilder.createMessage("broadcasts")
                    .withProvided(MessageParts.Value, new ResourceProvider<Integer>() {
                      @Override
                      public Integer get() {
                        return nextBroadcastId++;
                      }
                    })
                    .errorsHandledBy(new BusErrorCallback() {
                      @Override
                      public boolean error(Message message, Throwable throwable) {
                        System.out.println("Failed to send message: " + message);
                        throwable.printStackTrace(System.out);
                        return true;
                      }
                    })
                    .sendRepeatingWith(dispatcher, TimeUnit.SECONDS, 1);

            broadcastTask.setExitHandler(new Runnable() {
              @Override
              public void run() {
                System.out.println("Broadcast task ended");
                broadcastTask = null;
              }
            });
          }
        }
      }
    });
  }

  @Override
  public void callback(Message message) {
    MessageBuilder.createConversation(message)
      .subjectProvided()
      .withValue(message.get(String.class, MessageParts.Value))
      .done().reply();
  }


}
