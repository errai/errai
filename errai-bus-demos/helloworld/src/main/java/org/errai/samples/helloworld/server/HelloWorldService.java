package org.errai.samples.helloworld.server;

import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.api.MessageCallback;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.server.annotations.Service;

@Service
public class HelloWorldService implements MessageCallback {
  public void callback(Message message) {
    MessageBuilder.createConversation(message)
        .subjectProvided()
        .withValue("Hello, World!")
        .done().reply();
  }
}
