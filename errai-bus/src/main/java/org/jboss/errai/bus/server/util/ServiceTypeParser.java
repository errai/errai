package org.jboss.errai.bus.server.util;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.jboss.errai.bus.client.api.Local;
import org.jboss.errai.bus.client.api.messaging.MessageBus;
import org.jboss.errai.bus.client.api.messaging.MessageCallback;
import org.jboss.errai.bus.server.annotations.Command;
import org.jboss.errai.bus.server.annotations.Remote;
import org.jboss.errai.bus.server.annotations.Service;
import org.jboss.errai.bus.server.annotations.security.RequireAuthentication;
import org.jboss.errai.bus.server.annotations.security.RequireRoles;
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
  public boolean hasRule() {
    return clazz.isAnnotationPresent(RequireRoles.class);
  }

  @Override
  public boolean hasAuthentication() {
    return clazz.isAnnotationPresent(RequireAuthentication.class);
  }

  @Override
  public MessageCallback getCallback(Object delegateInstance, MessageBus bus) {
    if (isCallback()) {
      return (MessageCallback) delegateInstance;
    }
    else if (hasCommandPoints()) {
      return new CommandBindingsCallback(getCommandPoints(), delegateInstance, bus);
    }
    else {
      return null;
    }
  }
  
}
