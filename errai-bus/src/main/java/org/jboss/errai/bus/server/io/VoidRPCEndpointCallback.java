package org.jboss.errai.bus.server.io;

import static org.jboss.errai.bus.client.api.base.MessageBuilder.createConversation;

import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.bus.client.api.messaging.MessageBus;

import java.lang.reflect.Method;

/**
 * @author Mike Brock
 */
public class VoidRPCEndpointCallback extends AbstractRPCMethodCallback {
  public VoidRPCEndpointCallback(ServiceInstanceProvider genericSvc, Method method, MessageBus bus) {
    super(genericSvc, method, bus);
  }

  @Override
  public void callback(final Message message) {
    invokeMethodFromMessage(message);
    createConversation(message)
        .subjectProvided()
        .noErrorHandling().sendNowWith(bus);
  }
}
