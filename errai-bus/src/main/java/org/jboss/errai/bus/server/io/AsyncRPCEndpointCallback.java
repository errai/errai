package org.jboss.errai.bus.server.io;

import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.bus.client.api.messaging.MessageBus;

import java.lang.reflect.Method;

/**
 * @author Mike Brock
 */
public class AsyncRPCEndpointCallback extends AbstractRPCMethodCallback {
  public AsyncRPCEndpointCallback(ServiceInstanceProvider genericSvc, Method method, MessageBus bus) {
    super(genericSvc, method, bus);
  }

  @Override
  public void callback(final Message message) {
    invokeMethodFromMessage(message);
  }
}
