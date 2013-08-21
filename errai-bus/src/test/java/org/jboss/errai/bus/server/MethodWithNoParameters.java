package org.jboss.errai.bus.server;

import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.tests.ServiceAnnotationTests;
import org.jboss.errai.bus.server.annotations.Service;


public class MethodWithNoParameters extends BaseServiceTester {
  
  @Service
  public void noParams() {
    MessageBuilder.createMessage(ServiceAnnotationTests.REPLY_TO).done().sendNowWith(bus);
  }
}
