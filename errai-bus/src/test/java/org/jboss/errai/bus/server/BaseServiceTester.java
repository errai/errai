package org.jboss.errai.bus.server;

import javax.inject.Inject;

import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.bus.client.api.messaging.MessageBus;

/**
 * @author mbarkley <mbarkley@redhat.com>
 */
public class BaseServiceTester {
  
  @Inject
  protected MessageBus bus;
  
  public BaseServiceTester() {
    System.out.println(getClass().getSimpleName() + " instance created");
  }
  
  protected void sendResponse(Message message) {
    MessageBuilder.createConversation(message).subjectProvided().noErrorHandling().sendNowWith(bus);
  }

}
