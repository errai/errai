package org.jboss.errai.bus.server;

import org.jboss.errai.bus.client.api.Local;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.bus.client.tests.ServiceAnnotationTests;
import org.jboss.errai.bus.server.annotations.Service;

public class MethodLocalService extends BaseServiceTester {

  @Service
  public void localServiceRelay(Message message) {
    MessageBuilder.createMessage("localService").defaultErrorHandling().sendNowWith(bus);
  }

  @Service
  @Local
  public void localService(Message message) {
    MessageBuilder.createMessage(ServiceAnnotationTests.REPLY_TO).defaultErrorHandling().sendNowWith(bus);
  }

}
