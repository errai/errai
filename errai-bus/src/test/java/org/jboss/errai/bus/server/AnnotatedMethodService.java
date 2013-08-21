package org.jboss.errai.bus.server;

import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.bus.server.annotations.Service;

public class AnnotatedMethodService extends BaseServiceTester {
  
  @Service
  public void serviceMethod1(Message message) {
    System.out.println("message to serviceMethod1 received");
    sendResponse(message);
  }
}
