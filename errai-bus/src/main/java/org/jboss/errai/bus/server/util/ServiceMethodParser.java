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

package org.jboss.errai.bus.server.util;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.jboss.errai.bus.client.api.Local;
import org.jboss.errai.bus.client.api.messaging.MessageCallback;
import org.jboss.errai.bus.server.annotations.Command;
import org.jboss.errai.bus.server.annotations.Service;
import org.jboss.errai.bus.server.io.CommandBindingsCallback;
import org.jboss.errai.bus.server.io.ServiceMethodCallback;

/**
 * A {@link ServiceParser} implementation for methods annotated with {@link Service}.
 * 
 * @author Max Barkley <mbarkley@redhat.com>
 */
public class ServiceMethodParser extends ServiceParser {

  private final Method method;

  /**
   * Create a {@link ServiceParser} for methods annotated with {@link Service}.
   * 
   * @param method A method annotated with {@link Service}.
   * @throws NotAService Thrown if {@code method} does not have a {@link Service} annotation.
   */
  public ServiceMethodParser(Method method) throws NotAService {
    if (!method.isAnnotationPresent(Service.class)) {
      throw new NotAService("The method " + method.getName() + " is not a service.");
    }
    this.method = method;
    svcName = ("".equals(method.getAnnotation(Service.class).value())) ? method.getName() : method.getAnnotation(
            Service.class).value();
    local = method.isAnnotationPresent(Local.class);
    commandPoints = Collections.unmodifiableMap(getCommandPoints(method));
  }
  
  /**
   * Generate a map for any command points on this method.
   */
  private static Map<String, Method> getCommandPoints(Method method) {
    Map<String, Method> commandPoints = new HashMap<String, Method>();
    if (method.isAnnotationPresent(Command.class)) {
      Command command = method.getAnnotation(Command.class);
      for (String cmdName : command.value()) {
        if (cmdName.equals(""))
          cmdName = method.getName();
        commandPoints.put(cmdName, method);
      }
    }
    return commandPoints;
  }

  @Override
  public Class<?> getDelegateClass() {
    return method.getDeclaringClass();
  }

  @Override
  public boolean isCallback() {
    return false;
  }

  @Override
  public MessageCallback getCallback(Object delegate) {
    if (hasCommandPoints()) {
      return new CommandBindingsCallback(getCommandPoints(), delegate);
    }
    else {
      return new ServiceMethodCallback(delegate, method);
    }
  }

  @Override
  public String toString() {
    return method.toString();
  }
  
}
