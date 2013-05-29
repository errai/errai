package org.jboss.errai.bus.client.api;

import static org.jboss.errai.bus.client.api.base.MessageBuilder.createConversation;

import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.bus.server.api.RpcContext;

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
