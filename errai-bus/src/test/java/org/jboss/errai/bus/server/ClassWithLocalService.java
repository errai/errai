package org.jboss.errai.bus.server;

import javax.inject.Inject;

import org.jboss.errai.bus.client.api.Local;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.bus.client.api.messaging.MessageBus;
import org.jboss.errai.bus.client.api.messaging.MessageCallback;
import org.jboss.errai.bus.server.annotations.Service;
import org.jboss.errai.common.client.protocols.MessageParts;

@Service
@Local
public class ClassWithLocalService implements MessageCallback {

  @Inject
  private MessageBus bus;
  
  @Override
  public void callback(Message message) {
    MessageBuilder.createMessage(message.get(String.class, MessageParts.ReplyTo)).noErrorHandling().sendNowWith(bus);
  }

}
