package org.jboss.errai.ioc.support.bus.tests.client.res;

import javax.inject.Singleton;

import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.bus.client.api.messaging.MessageCallback;
import org.jboss.errai.bus.server.annotations.ShadowService;

@Singleton
@ShadowService("Greeting")
public class OfflineMessageCallback implements MessageCallback {

  private String greeting;
  
  @Override
  public void callback(Message message) {
    greeting = (String) message.getParts().get("greeting");
  }
  
  public String getGreeting() {
    return greeting;
  }

}
