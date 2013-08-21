package org.jboss.errai.bus.server;

import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.bus.server.annotations.Service;

public class ClassWithMultipleServices extends BaseServiceTester {

  @Service
  public void service1(Message message) {
    sendResponse(message);
  }
  
  @Service
  public void service2(Message message) {
    sendResponse(message);
  }
}
