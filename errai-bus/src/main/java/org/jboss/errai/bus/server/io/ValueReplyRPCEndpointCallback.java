package org.jboss.errai.bus.server.io;

import static org.jboss.errai.bus.client.api.base.MessageBuilder.createConversation;

import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.bus.client.api.messaging.MessageBus;

import java.lang.reflect.Method;

/**
 * @author Mike Brock
 */
public class ValueReplyRPCEndpointCallback extends AbstractRPCMethodCallback {
  public ValueReplyRPCEndpointCallback(final ServiceInstanceProvider genericSvc,
                                       final Method method,
                                       final MessageBus bus) {
    super(genericSvc, method, bus);
  }

  @Override
  public void callback(final Message message) {
    createConversation(message)
        .subjectProvided()
        .with("MethodReply", invokeMethodFromMessage(message))
        .noErrorHandling().sendNowWith(bus);
  }
}
