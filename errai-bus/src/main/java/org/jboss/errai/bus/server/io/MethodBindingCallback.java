/*
 * Copyright (C) 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
