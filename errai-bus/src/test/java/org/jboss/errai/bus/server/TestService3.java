package org.jboss.errai.bus.server;

import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.bus.client.api.messaging.MessageCallback;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.server.annotations.Service;

/**
 * @author Mike Brock
 */
@Service
public class TestService3 implements MessageCallback {
  @Override
  public void callback(final Message message) {
    MessageBuilder.createConversation(message)
        .subjectProvided().signalling().noErrorHandling().reply();
  }
}
