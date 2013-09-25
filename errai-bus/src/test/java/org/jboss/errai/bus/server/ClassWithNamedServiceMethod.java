package org.jboss.errai.bus.server;

import javax.inject.Inject;

import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.bus.client.api.messaging.MessageBus;
import org.jboss.errai.bus.server.annotations.Service;

public class ClassWithNamedServiceMethod {

  @Inject
  private MessageBus bus;

  @Service("ANamedServiceMethod")
  public void serviceMethod(Message message) {
    MessageBuilder.createConversation(message).subjectProvided().done().sendNowWith(bus);
  }
}
