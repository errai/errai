package org.jboss.errai.cdi.event.server;

import javax.inject.Inject;

import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.bus.client.api.messaging.MessageBus;
import org.jboss.errai.bus.server.annotations.Service;

public class ClassWithMultipleServices {

  @Inject
  MessageBus bus;
  
  @Service
  public void service1(Message message) {
    sendResponse(message);
  }
  
  @Service
  public void service2(Message message) {
    sendResponse(message);
  }
  
  private void sendResponse(Message message) {
    MessageBuilder.createConversation(message).subjectProvided().done().sendNowWith(bus);
  }
}
