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

import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.bus.client.api.messaging.MessageCallback;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.server.mock.MockErraiService;
import org.jboss.errai.bus.server.mock.MockErraiServiceConfigurator;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * @author Mike Brock
 */
public class BusPerformanceTests {
  @Test
  @Ignore
  public void testBusThroughput() {
    class TestCallback implements MessageCallback {
      int calls;

      @Override
      public void callback(Message message) {
        calls++;
      }
    }

    TestCallback callback = new TestCallback();

    ServerMessageBusImpl bus = new ServerMessageBusImpl(new MockErraiService(), new MockErraiServiceConfigurator());
    bus.subscribe("Foo", callback);

    int iterations = 25000000;

    long start = System.currentTimeMillis();
    for (int i = 0; i < iterations; i++) {
      MessageBuilder.createMessage()
              .toSubject("Foo")
              .done().sendNowWith(bus);
    }
    long time = System.currentTimeMillis() - start;

    NumberFormat nf = new DecimalFormat("###,###.###");

    System.out.println("Total Test Time    : " + nf.format(time / 1000d) + " seconds.");
    System.out.println("Total Messages Sent: " + nf.format(iterations));
    System.out.println("Total Messages Rcvd: " + nf.format(callback.calls));
    System.out.println("Transaction Rate   : " + nf.format(iterations / (time / 1000d)) + " per second.");

    Assert.assertEquals(iterations, callback.calls);
  }
}
