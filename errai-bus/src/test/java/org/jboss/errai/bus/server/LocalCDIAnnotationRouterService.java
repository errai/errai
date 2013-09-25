package org.jboss.errai.bus.server;

import javax.inject.Inject;

import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.bus.client.api.messaging.MessageBus;
import org.jboss.errai.bus.client.api.messaging.MessageCallback;
import org.jboss.errai.bus.server.annotations.Service;
import org.jboss.errai.common.client.protocols.MessageParts;

@Service
public class LocalCDIAnnotationRouterService implements MessageCallback {

  @Inject
  private MessageBus bus;
  
  @Override
  public void callback(Message message) {
    final String SUBJECT = message.getValue(String.class);
    final String REPLY_TO = message.get(String.class, MessageParts.ReplyTo);
    
    MessageBuilder.createMessage(SUBJECT).with(MessageParts.ReplyTo, REPLY_TO).noErrorHandling().sendNowWith(bus);
  }
}
