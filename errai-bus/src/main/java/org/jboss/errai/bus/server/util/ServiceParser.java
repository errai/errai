package org.jboss.errai.bus.server.util;

import java.lang.reflect.Method;
import java.util.Map;

import org.jboss.errai.bus.client.api.Local;
import org.jboss.errai.bus.client.api.messaging.MessageCallback;
import org.jboss.errai.bus.server.annotations.Command;
import org.jboss.errai.bus.server.annotations.Service;

/**
 * Parses and stores {@link Service} and {@link Command} meta-data for registering a
 * {@link MessageCallback}.
 * 
 * @author Max Barkley <mbarkley@redhat.com>
 */
public abstract class ServiceParser {

  protected boolean local;
  protected String svcName;
  protected Map<String, Method> commandPoints;

  /**
   * Get all (if any) {@link Command} endpoints for this service.
   * 
   * @return A map of command names to corresponding methods.
   */
  public Map<String, Method> getCommandPoints() {
    return commandPoints;
  }

  /**
   * @return True iff this service has any {@link Command} endpoints.
   */
  public boolean hasCommandPoints() {
    return getCommandPoints().size() != 0;
  }

  /**
   * @return The subject name of this service used for registering a {@link MessageCallback}.
   */
  public String getServiceName() {
    return svcName;
  }

  /**
   * @return True iff this is a {@link Local} service.
   */
  public boolean isLocal() {
    return local;
  }

  /**
   * @return The {@link Class} of the delegate instance for this service.<br/>
   * <br/>
   *         For a {@link Service} annotation on a type, this will be that type.<br/>
   *         For a {@link Service} annotation on a method, this will be the enclosing type.
   */
  public abstract Class<?> getDelegateClass();

  /**
   * @return True iff this is a type service with no command points, and this type is assignable to
   *         a {@link MessageCallback}.
   */
  public abstract boolean isCallback();

  public abstract MessageCallback getCallback(Object delegateInstance);

}