/*
 * Copyright (C) 2012 Red Hat, Inc. and/or its affiliates.
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
import java.util.HashMap;
import java.util.Map;

import org.jboss.errai.bus.client.api.base.MessageDeliveryFailure;
import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.bus.client.api.messaging.MessageCallback;

public class CommandBindingsCallback extends MethodBindingCallback {
  private final Map<String, MethodDispatcher> methodDispatchers;
  private final MessageCallback defaultCallback;
  private final boolean defaultAction;

  public CommandBindingsCallback(final Map<String, Method> commandBindings, final Object delegate) {
    this.methodDispatchers = new HashMap<String, MethodDispatcher>(commandBindings.size() * 2);
    this.defaultAction = delegate instanceof MessageCallback;
    this.defaultCallback = defaultAction ? (MessageCallback) delegate : null;

    for (final Map.Entry<String, Method> entry : commandBindings.entrySet()) {
      final Method method = entry.getValue();
      final Class<?>[] parmTypes = method.getParameterTypes();

      verifyMethodSignature(method);

      method.setAccessible(true);

      methodDispatchers.put(entry.getKey(), parmTypes.length == 0 ? new NoParamMethodDispatcher(delegate, method)
              : new DefaultMethodDispatcher(delegate, method));
    }
  }

  public void callback(final Message message) {
    final MethodDispatcher method = methodDispatchers.get(message.getCommandType());

    try {
      if (method == null) {
        if (defaultAction) {
          defaultCallback.callback(message);
        }
        else {
          throw new MessageDeliveryFailure(String.format("Unrecognized command, %s, in service %s",
                  message.getCommandType(), message.getSubject()));
        }
      }
      else {
        method.dispatch(message);
      }
    }
    catch (MessageDeliveryFailure e) {
      throw e;
    }
    catch (Exception e) {
      maybeUnwrapAndThrowError(e);
    }
  }

  private abstract class MethodDispatcher {
    protected Object delegate;
    protected Method method;

    protected MethodDispatcher(final Object delegate, final Method method) {
      this.delegate = delegate;
      this.method = method;
    }

    abstract void dispatch(Message m) throws Exception;
  }

  private class NoParamMethodDispatcher extends MethodDispatcher {
    NoParamMethodDispatcher(final Object delegate, final Method method) {
      super(delegate, method);
    }

    @Override
    void dispatch(final Message m) throws Exception {
      method.invoke(delegate);
    }
  }

  private class DefaultMethodDispatcher extends MethodDispatcher {
    DefaultMethodDispatcher(final Object delegate, final Method method) {
      super(delegate, method);
    }

    @Override
    void dispatch(final Message m) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
      method.invoke(delegate, m);
    }
  }
}
