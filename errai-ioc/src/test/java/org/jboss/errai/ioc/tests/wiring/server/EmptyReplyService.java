package org.jboss.errai.ioc.tests.wiring.server;

import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.api.MessageCallback;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.server.annotations.Service;

/**
 * A service that replies to TestCompleterService with an empty message.
 *
 * @author Jonathan Fuerth <jfuerth@gmail.com>
 */
@Service
public class EmptyReplyService implements MessageCallback {

  @Override
  public void callback(Message message) {
    MessageBuilder.createConversation(message)
    .toSubject("TestCompleterService")
    .done().reply();
  }
}
