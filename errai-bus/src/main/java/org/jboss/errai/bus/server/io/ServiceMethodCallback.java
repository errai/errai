package org.jboss.errai.bus.server.io;

import java.lang.reflect.Method;

import org.jboss.errai.bus.client.api.messaging.Message;

/**
 * A callback implementation for methods annotated with
 * {@link org.jboss.errai.bus.server.annotations.Service Service}.
 * 
 * @author Max Barkley <mbarkley@redhat.com>
 */
public class ServiceMethodCallback extends MethodBindingCallback {

  private Object delegate;
  private Method service;
  private boolean noArgs;

  /**
   * Create a callback to the given service method.
   * 
   * @param delegate
   *          The instance on which the service method should be invoked.
   * @param service
   *          The service method to be invoked.
   */
  public ServiceMethodCallback(Object delegate, Method service) {
    this.delegate = delegate;
    this.service = service;
    this.service.setAccessible(true);

    noArgs = (service.getParameterTypes().length == 0);
    verifyMethodSignature(service);
  }

  @Override
  public void callback(Message message) {
    try {
      if (noArgs) {
        service.invoke(delegate);
      }
      else {
        service.invoke(delegate, message);
      }
    }
    catch (Exception e) {
      maybeUnwrapAndThrowError(e);
    }
  }
}
