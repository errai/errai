package org.jboss.errai.bus.server.io;

import java.lang.reflect.Method;

import org.jboss.errai.bus.client.api.base.MessageDeliveryFailure;
import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.bus.client.api.messaging.MessageCallback;

/**
 * A callback implementation for methods annotated with
 * {@link org.jboss.errai.bus.server.annotations.Service Service}.
 * 
 * @author Max Barkley <mbarkley@redhat.com>
 */
public class ServiceMethodCallback implements MessageCallback {

  private Object delegate;
  private Method service;
  private boolean noArgs;

  /**
   * Create a callback to the given service method.
   * 
   * @param delegate The instance on which the service method should be invoked.
   * @param service The service method to be invoked.
   */
  public ServiceMethodCallback(Object delegate, Method service) {
    this.delegate = delegate;
    this.service = service;
    if (service.getParameterTypes().length == 0) {
      noArgs = true;
    }
    else if (service.getParameterTypes().length != 1 || !service.getParameterTypes()[0].equals(Message.class)) {
      throw new RuntimeException(delegate.getClass().getName() + "#" + service.getName() + " has incorrect arguments");
    }
  }

  @Override
  public void callback(Message message) {
    if (noArgs) {
      try {
        service.invoke(delegate);
      } catch (Exception e) {
        throw new MessageDeliveryFailure(e);
      }
    }
    else {
      try {
        service.invoke(delegate, message);
      } catch (Exception e) {
        throw new MessageDeliveryFailure(e);
      }
    }
  }
}
