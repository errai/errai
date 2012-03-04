/*
 * Copyright 2011 JBoss, by Red Hat, Inc
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

package org.jboss.errai.bus.server;

import junit.framework.TestCase;
import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.api.MessageCallback;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.server.mock.MockErraiServiceConfigurator;

/**
 * @author Mike Brock
 */
public class BusPerformanceTests extends TestCase {
  public void testBusThroughput() {
    class TestCallback implements MessageCallback {
      int calls;

      @Override
      public void callback(Message message) {
        calls++;
      }
    }

    TestCallback callback = new TestCallback();

    ServerMessageBusImpl bus = new ServerMessageBusImpl(new MockErraiServiceConfigurator());
    bus.subscribe("Foo", callback);

    int iterations = 10000000;

    long start = System.currentTimeMillis();
    for (int i = 0; i < iterations; i++) {
      MessageBuilder.createMessage()
              .toSubject("Foo")
              .done().sendNowWith(bus);
    }
    long time = System.currentTimeMillis() - start;
    
    System.out.println("tps: " + (iterations / (time / 1000d)));

    assertEquals(iterations, callback.calls);

  }
}
