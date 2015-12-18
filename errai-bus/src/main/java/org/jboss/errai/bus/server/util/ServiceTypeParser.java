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
import org.jboss.errai.bus.server.annotations.Remote;
import org.jboss.errai.bus.server.annotations.Service;
import org.jboss.errai.bus.server.io.CommandBindingsCallback;

/**
 * A {@link ServiceParser} implementation for types annotated with {@link Service}.
 * 
 * @author Max Barkley <mbarkley@redhat.com>
 */
public class ServiceTypeParser extends ServiceParser {
  
  private final Class<?> clazz;

  /**
   * Create a {@link ServiceParser} for classes annotated with {@link Service}.
   * 
   * @param clazz A class annotated with {@link Service}.
   * @throws NotAService Thrown if {@code clazz} does not have a {@link Service} annotation.
   */
  public ServiceTypeParser(Class<?> clazz) throws NotAService {
    this.clazz = clazz;

    Service svcAnnotation = clazz.getAnnotation(Service.class);
    if (null == svcAnnotation) {
      throw new NotAService("The class " + clazz.getName() + " is not a service");
    }

    local = clazz.isAnnotationPresent(Local.class);
    svcName = resolveServiceName(clazz);

    this.commandPoints = Collections.unmodifiableMap(getCommandPoints(clazz));
  }
  
  /**
   * @return The {@link Remote} interface associated with this type service or {@code null} if none exists.
   */
  public Class<?> getRemoteImplementation() {
    return getRemoteImplementation(clazz);
  }
  
  private static Class<?> getRemoteImplementation(Class<?> type) {
    for (Class<?> iface : type.getInterfaces()) {
      if (iface.isAnnotationPresent(Remote.class)) {
        return iface;
      }
      else if (iface.getInterfaces().length != 0 && ((iface = getRemoteImplementation(iface)) != null)) {
        return iface;
      }
    }
    return null;
  }

  /**
   * Get the subject name of a service type.
   */
  private static String resolveServiceName(final Class<?> type) {
    String subjectName = type.getAnnotation(Service.class).value();
  
    if (subjectName.equals(""))
      subjectName = type.getSimpleName();
  
    return subjectName;
  }
  
  private static Map<String, Method> getCommandPoints(Class<?> clazz) {
    Map<String, Method> commandPoints = new HashMap<String, Method>();
    for (final Method method : clazz.getDeclaredMethods()) {
      if (method.isAnnotationPresent(Command.class) && !method.isAnnotationPresent(Service.class)) {
        Command command = method.getAnnotation(Command.class);
        for (String cmdName : command.value()) {
          if (cmdName.equals(""))
            cmdName = method.getName();
          commandPoints.put(cmdName, method);
        }
      }
    }
    return commandPoints;
  }

  @Override
  public Class<?> getDelegateClass() {
    return clazz;
  }

  @Override
  public boolean isCallback() {
    return MessageCallback.class.isAssignableFrom(clazz) && !hasCommandPoints();
  }

  @Override
  public String toString() {
    return clazz.toString();
  }

  @Override
  public MessageCallback getCallback(Object delegateInstance) {
    if (isCallback()) {
      return (MessageCallback) delegateInstance;
    }
    else if (hasCommandPoints()) {
      return new CommandBindingsCallback(getCommandPoints(), delegateInstance);
    }
    else {
      return null;
    }
  }
  
}
