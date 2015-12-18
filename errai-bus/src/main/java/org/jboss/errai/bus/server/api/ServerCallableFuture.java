package org.jboss.errai.bus.server.api;

import static org.jboss.errai.bus.client.api.base.MessageBuilder.createConversation;

import org.jboss.errai.bus.client.api.CallableFuture;
import org.jboss.errai.bus.client.api.messaging.Message;

/**
 * @author Mike Brock
 */
public class ServerCallableFuture<T> implements CallableFuture<T> {
  private Message incomingMessage;

  public ServerCallableFuture() {
    incomingMessage = RpcContext.getMessage();
  }

  @Override
  public void setValue(final T responseValue) {
    createConversation(incomingMessage)
        .subjectProvided()
        .with("MethodReply", responseValue)
        .noErrorHandling().reply();
  }
}
