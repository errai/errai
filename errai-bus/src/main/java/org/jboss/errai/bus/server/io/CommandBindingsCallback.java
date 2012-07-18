/*
 * Copyright 2011 JBoss, by Red Hat, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.bus.server.io;

import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.api.MessageCallback;
import org.jboss.errai.bus.client.framework.MessageBus;
import org.jboss.errai.bus.client.util.ErrorHelper;
import org.jboss.errai.common.client.framework.Assert;
import org.slf4j.Logger;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import static org.slf4j.LoggerFactory.getLogger;

public class CommandBindingsCallback implements MessageCallback {
  private static final Logger log = getLogger(CommandBindingsCallback.class);
  
  private final Map<String, MethodDispatcher> methodDispatchers;
  private final MessageCallback defaultCallback;
  private final boolean defaultAction;
  private final MessageBus bus;

  public CommandBindingsCallback(final Map<String, Method> commandBindings, final Object delegate, final MessageBus bus) {
    this.methodDispatchers = new HashMap<String, MethodDispatcher>(commandBindings.size() * 2);
    this.defaultAction = delegate instanceof MessageCallback;
    this.defaultCallback = defaultAction ? (MessageCallback) delegate : null;
    this.bus = Assert.notNull(bus);

    for (final Map.Entry<String, Method> entry : commandBindings.entrySet()) {
      final Class[] parmTypes = entry.getValue().getParameterTypes();

      if (parmTypes.length > 1 ||
          (parmTypes.length == 1 && !Message.class.isAssignableFrom(parmTypes[0]))) {
        throw new IllegalStateException("method does not implement signature: " + entry.getValue().getName()
            + "(" + Message.class.getName() + ")");
      }

      methodDispatchers.put(entry.getKey(),
          parmTypes.length == 0 ?
              new NoParamMethodDispatcher(delegate, entry.getValue()) :
              new DefaultMethodDispatcher(delegate, entry.getValue()));
    }
  }

  public void callback(final Message message) {
    final MethodDispatcher method = methodDispatchers.get(message.getCommandType());

    if (method == null) {
      if (defaultAction) {
        defaultCallback.callback(message);
      }
      else {
        ErrorHelper.sendClientError(bus, message, "no such command: " + message.getCommandType(), "");
      }
    }
    else {
      try {
        method.dispatch(message);
      }
      catch (Exception e) {
        // see ERRAI-290: we may want to hand this exception over to a user-provided server-side callback.
        log.error("Command method threw an exception. This exception is not propagated to the client.", e);
      }
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
