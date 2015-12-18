package org.jboss.errai.ioc.support.bus.tests.server;

import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.bus.client.api.messaging.MessageCallback;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.server.annotations.Service;

import java.io.File;

/**
 * A service that replies to TestCompleterService with an empty message.
 *
 * @author Jonathan Fuerth <jfuerth@gmail.com>
 */
@Service
public class EmptyReplyService implements MessageCallback {
  public EmptyReplyService() {
    System.out.println("Working dir: " + new File("").getAbsoluteFile().getAbsolutePath());
  }

  @Override
  public void callback(Message message) {
    MessageBuilder.createConversation(message)
    .toSubject("TestCompleterService")
    .done().reply();
  }
}
