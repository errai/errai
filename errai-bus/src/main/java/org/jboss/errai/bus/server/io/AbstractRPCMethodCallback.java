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

import static org.slf4j.LoggerFactory.getLogger;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import org.jboss.errai.bus.client.api.base.MessageDeliveryFailure;
import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.bus.client.api.messaging.MessageBus;
import org.jboss.errai.bus.client.api.messaging.MessageCallback;
import org.jboss.errai.bus.server.QueueUnavailableException;
import org.jboss.errai.bus.server.api.RpcContext;
import org.slf4j.Logger;

/**
 * @author Mike Brock
 */
public abstract class AbstractRPCMethodCallback implements MessageCallback {
  private static final Logger log = getLogger(AbstractRPCMethodCallback.class);

  protected final ServiceInstanceProvider serviceProvider;
  protected final Class[] targetTypes;
  protected final Method method;
  protected final MessageBus bus;

  protected AbstractRPCMethodCallback(final ServiceInstanceProvider genericSvc,
                                      final Method method,
                                      final MessageBus bus) {
    this.serviceProvider = genericSvc;
    this.targetTypes = (this.method = method).getParameterTypes();
    this.bus = bus;
  }

  public Object invokeMethodFromMessage(Message message) {
    final List<Object> parms = message.get(List.class, "MethodParms");

    if ((parms == null && targetTypes.length != 0) || (parms.size() != targetTypes.length)) {
      throw new MessageDeliveryFailure(
          "wrong number of arguments sent to endpoint. (received: "
              + (parms == null ? 0 : parms.size())
              + "; required: " + targetTypes.length + ")");
    }

    try {
      RpcContext.set(message);
      return method.invoke(serviceProvider.get(message), parms.toArray(new Object[parms.size()]));
    }
    catch (QueueUnavailableException e) {
      throw e;
    }
    catch (MessageDeliveryFailure e) {
      throw e;
    }
    catch (InvocationTargetException e) {
      log.debug("RPC endpoint threw exception:", e.getCause());
      throw new MessageDeliveryFailure("error invoking RPC endpoint " + method, e.getCause(), true);
    }
    catch (Exception e) {
      throw new MessageDeliveryFailure("error invoking endpoint", e);
    }
    finally {
      RpcContext.remove();
    }
  }
}
