package org.jboss.errai.security.server;

import javax.inject.Inject;

import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.bus.client.api.messaging.MessageBus;

abstract class BaseResponseService {

  @Inject
  protected MessageBus bus;
  
  protected void respondToMessage(final Message message) {
    MessageBuilder.createConversation(message)
    .subjectProvided()
    .signalling()
    .noErrorHandling()
    .sendNowWith(bus);
  }

}
