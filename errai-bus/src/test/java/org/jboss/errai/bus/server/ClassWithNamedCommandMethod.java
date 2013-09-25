package org.jboss.errai.bus.server;

import javax.inject.Inject;

import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.bus.client.api.messaging.MessageBus;
import org.jboss.errai.bus.server.annotations.Command;
import org.jboss.errai.bus.server.annotations.Service;

@Service
public class ClassWithNamedCommandMethod {

  @Inject
  private MessageBus bus;

  @Command("ANamedCommandMethod")
  public void command(Message message) {
    MessageBuilder.createConversation(message).subjectProvided().done().sendNowWith(bus);
  }
}
