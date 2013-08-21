package org.jboss.errai.bus.server;

import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.bus.server.annotations.Service;

public class NamedAnnotatedService extends BaseServiceTester {
  
  @Service("namedMethodTest")
  public void serviceMethod2(Message message) {
    System.out.println("message to namedMethodTest received");
    sendResponse(message);
  }
}
