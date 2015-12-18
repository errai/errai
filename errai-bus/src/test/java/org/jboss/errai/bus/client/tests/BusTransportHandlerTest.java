/*
 * Copyright (C) 2015 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.bus.client.tests;

import java.util.Collection;

import org.jboss.errai.bus.client.ErraiBus;
import org.jboss.errai.bus.client.framework.ClientMessageBusImpl;
import org.jboss.errai.bus.client.framework.transports.TransportHandler;

public class BusTransportHandlerTest extends AbstractErraiTest {

  @Override
  public String getModuleName() {
    return "org.jboss.errai.bus.ErraiBusTests";
  }

  public void testTransportHandlerPriority() {
    runAfterInit(new Runnable() {
      @Override
      public void run() {
        ClientMessageBusImpl bus = (ClientMessageBusImpl) ErraiBus.get();
        Collection<TransportHandler> handlers = bus.getAllAvailableHandlers();
        int webSocketHandlerPriority = indexOf(handlers, "WebSockets");
        int sseHandlerPriority = indexOf(handlers, "HTTP + Server-Sent Events");
        int longPollingHandlerPriority = indexOf(handlers, "HTTP Long Polling");;
        int shortPollingHandlerPriority = indexOf(handlers, "HTTP Short Polling");;

        System.out.println("Handler priorities:" +
        		" " + webSocketHandlerPriority +
        		" " + sseHandlerPriority +
        		" " + longPollingHandlerPriority +
        		" " + shortPollingHandlerPriority);

        assertTrue(webSocketHandlerPriority < sseHandlerPriority);
        assertTrue(sseHandlerPriority < longPollingHandlerPriority);
        assertTrue(longPollingHandlerPriority < shortPollingHandlerPriority);
        finishTest();
      }
    });

    delayTestFinish(30000);
  }

  private static int indexOf(Collection<TransportHandler> handlers, String type) {
    int i = 0;
    for (TransportHandler handler : handlers) {
      if (handler.getStatistics().getTransportDescription().equals(type)) {
        return i;
      }
      i++;
    }
    fail("Couldn't find bus transport type \"" + type + "\"");
    return -1; // NOTREACHED
  }
}
