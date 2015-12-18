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

package org.jboss.errai.bus.server;

import java.util.concurrent.CountDownLatch;

import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.bus.client.api.messaging.MessageCallback;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.server.annotations.Service;
import org.jboss.errai.bus.server.io.buffers.TransmissionBuffer;

@Service
public class GiantStringTestService implements MessageCallback {

  public void callback(final Message message) {
    final CountDownLatch bigMessagesLatch = new CountDownLatch(1);

    System.out.println("GiantStringTestService callback()");

    final Thread giantStringThread = new Thread(new Runnable() {
      @Override
      public void run() {
        for (int i = 0; i < 50; i++) {
          MessageBuilder.createConversation(message)
              .subjectProvided()
              .with("string", createGiantString())
              .done().reply();
          
          // ensure some big messages are in the queue before small message is injected
          if (i == 10) {
            System.out.println("countDown()");
            bigMessagesLatch.countDown();
          }
        }
      }
    });

    final Thread smallStringThread = new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          System.out.println("await()");
          bigMessagesLatch.await();
          System.out.println("done await()");
        }
        catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          return;
        }
          MessageBuilder.createConversation(message)
              .subjectProvided()
              .with("string", "a smaller string")
              .done().reply();
      }
    });

    smallStringThread.start();
    giantStringThread.start();
  }

  public String createGiantString() {
    int size = TransmissionBuffer.DEFAULT_SEGMENT_SIZE * 3;
    StringBuilder sb = new StringBuilder(size + 10);
    int i = 0;
    while (sb.length() < size) {
      sb.append(String.format("%10d,", i++));
    }
    return sb.toString();
  }
}
