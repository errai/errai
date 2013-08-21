package org.jboss.errai.bus.server;

import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.bus.client.api.messaging.MessageCallback;
import org.jboss.errai.bus.server.annotations.Command;
import org.jboss.errai.bus.server.annotations.Service;

@Service
public class ClassServiceMethodCommandWithCallback extends BaseServiceTester implements MessageCallback {
  
  @Command("commandTest")
  public void commandMethod(Message message) {
    System.out.println("message to ClassServiceMethodAnnotation with command commandTest received");
    sendResponse(message);
  }

  @Override
  public void callback(Message message) {
    throw new RuntimeException("This method should not be called!");
  }
}
