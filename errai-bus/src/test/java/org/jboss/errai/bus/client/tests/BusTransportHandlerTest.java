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
