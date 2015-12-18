package org.jboss.errai.bus.client.tests.support;

import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.bus.client.api.messaging.MessageCallback;
import org.jboss.errai.bus.server.annotations.Service;

@Service
public class FunAnnotatedClientClass2 implements MessageCallback {

  @Override
  public void callback(Message message) {
    System.out.println("FunAnnotatedClientClass2 got a call");
  }

}
