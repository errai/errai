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
