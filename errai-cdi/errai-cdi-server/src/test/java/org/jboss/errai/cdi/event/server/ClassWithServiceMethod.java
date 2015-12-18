package org.jboss.errai.cdi.event.server;

import javax.inject.Inject;

import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.bus.client.api.messaging.MessageBus;
import org.jboss.errai.bus.server.annotations.Service;

public class ClassWithServiceMethod {

  @Inject
  private MessageBus bus;

  @Service
  public void serviceMethod(Message message) {
    MessageBuilder.createConversation(message).subjectProvided().done().sendNowWith(bus);
  }
}
