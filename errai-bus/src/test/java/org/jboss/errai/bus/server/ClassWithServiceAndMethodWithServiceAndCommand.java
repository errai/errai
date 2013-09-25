package org.jboss.errai.bus.server;

import javax.inject.Inject;

import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.bus.client.api.messaging.MessageBus;
import org.jboss.errai.bus.client.api.messaging.MessageCallback;
import org.jboss.errai.bus.server.annotations.Command;
import org.jboss.errai.bus.server.annotations.Service;

@Service
public class ClassWithServiceAndMethodWithServiceAndCommand implements MessageCallback {

  @Inject
  private MessageBus bus;

  @Service("TheMethodsService")
  @Command
  private void command(Message message) {
    MessageBuilder.createConversation(message).subjectProvided().noErrorHandling().sendNowWith(bus);
  }
  
  @Service("TheMethodsService")
  @Command
  private void badCommand(Message message) {
    throw new RuntimeException("This should never be called!");
  }

  @Override
  public void callback(Message message) {
    MessageBuilder.createConversation(message).subjectProvided().withValue("callback").noErrorHandling()
            .sendNowWith(bus);
  }

}
