package org.jboss.errai.cdi.event.server;

import javax.inject.Inject;

import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.bus.client.api.messaging.MessageBus;
import org.jboss.errai.bus.client.api.messaging.MessageCallback;
import org.jboss.errai.bus.server.annotations.Service;

@Service
public class ClassWithService implements MessageCallback {
  
  @Inject
  MessageBus bus;

  @Override
  public void callback(Message message) {
    MessageBuilder.createConversation(message).subjectProvided().done().sendNowWith(bus);
  }

}
