package org.jboss.errai.bus.server.io;

import org.jboss.errai.bus.client.api.CallableFuture;
import org.jboss.errai.bus.client.api.messaging.MessageBus;
import org.jboss.errai.bus.client.api.messaging.MessageCallback;

import java.lang.reflect.Method;

/**
 * @author Mike Brock
 */
public class RPCEndpointFactory {
  private static final RPCEndpointFactory ENDPOINT_FACTORY = new RPCEndpointFactory();

  private RPCEndpointFactory() {
  }

  public static RPCEndpointFactory get() {
    return ENDPOINT_FACTORY;
  }

  public static MessageCallback createEndpointFor(final ServiceInstanceProvider provider,
                                                  final Method method,
                                                  final MessageBus messageBus) {
    if (method.getReturnType().equals(void.class)) {
      return new VoidRPCEndpointCallback(provider, method, messageBus);
    }
    else if (CallableFuture.class.isAssignableFrom(method.getReturnType())) {
      return new AsyncRPCEndpointCallback(provider, method, messageBus);
    }
    else {
      return new ValueReplyRPCEndpointCallback(provider, method, messageBus);
    }
  }
}
