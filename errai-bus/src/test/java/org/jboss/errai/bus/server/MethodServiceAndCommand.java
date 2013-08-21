package org.jboss.errai.bus.server;

import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.bus.server.annotations.Command;
import org.jboss.errai.bus.server.annotations.Service;

public class MethodServiceAndCommand extends BaseServiceTester {
  
  @Service("commandService")
  @Command("commandTest")
  public void commandMethod(Message message) {
    System.out.println("message to ClassServiceMethodAnnotation with command commandTest received");
    sendResponse(message);
  }
}
