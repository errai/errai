package org.jboss.errai.bus.server.io;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.jboss.errai.bus.client.api.base.MessageCallbackFailure;
import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.bus.client.api.messaging.MessageCallback;

abstract class MethodBindingCallback implements MessageCallback {

  protected void maybeUnwrapAndThrowError(Throwable throwable) throws RuntimeException {
    while (throwable instanceof InvocationTargetException && throwable.getCause() != null) {
      throwable = throwable.getCause();
    }

    throw (throwable instanceof RuntimeException) ? (RuntimeException) throwable
            : new MessageCallbackFailure(throwable);
  }

  protected void verifyMethodSignature(final Method method) {
    final Class<?>[] parmTypes = method.getParameterTypes();

    if (parmTypes.length > 1 || (parmTypes.length == 1 && !Message.class.isAssignableFrom(parmTypes[0]))) {
      throw new IllegalStateException("method does not implement signature: " + method.getName() + "("
              + Message.class.getName() + ")");
    }
  }

}
