package org.jboss.errai.bus.server;

import javax.inject.Inject;

import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.api.messaging.MessageBus;
import org.jboss.errai.bus.client.tests.ServiceAnnotationTests;
import org.jboss.errai.bus.server.annotations.Service;


public class MethodWithNoParameters {
  
  @Inject
  private MessageBus bus;
  
  @Service
  public void noParams() {
    MessageBuilder.createMessage(ServiceAnnotationTests.REPLY_TO).done().sendNowWith(bus);
  }
}
