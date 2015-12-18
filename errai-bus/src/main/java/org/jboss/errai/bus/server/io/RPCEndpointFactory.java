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
