package org.jboss.errai.cdi.event.server;

import javax.inject.Inject;

import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.bus.client.api.messaging.MessageBus;
import org.jboss.errai.bus.server.annotations.Command;
import org.jboss.errai.bus.server.annotations.Service;

public class ClassWithServiceAndCommandMethod {

  @Inject
  private MessageBus bus;
  
  @Service("ClassWithServiceAndCommandMethod")
  @Command("serviceAndCommandMethod")
  private void respond(Message message) {
    MessageBuilder.createConversation(message).subjectProvided().noErrorHandling().sendNowWith(bus);
  }
  
}
