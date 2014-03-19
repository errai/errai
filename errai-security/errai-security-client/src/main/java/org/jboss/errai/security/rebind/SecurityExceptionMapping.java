package org.jboss.errai.security.rebind;

import org.jboss.errai.marshalling.rebind.api.CustomMapping;
import org.jboss.errai.marshalling.rebind.api.InheritedMappings;
import org.jboss.errai.marshalling.rebind.api.model.MappingDefinition;
import org.jboss.errai.marshalling.rebind.api.model.impl.AccessorMapping;
import org.jboss.errai.marshalling.rebind.api.model.impl.ReadMapping;
import org.jboss.errai.marshalling.rebind.api.model.impl.SimpleConstructorMapping;
import org.jboss.errai.marshalling.rebind.api.model.impl.WriteMapping;
import org.jboss.errai.security.shared.exception.AuthenticationException;
import org.jboss.errai.security.shared.exception.SecurityException;
import org.jboss.errai.security.shared.exception.UnauthenticatedException;
import org.jboss.errai.security.shared.exception.UnauthorizedException;

/**
 * This custom mapping prevents marshalling issues with the JDK 7. Without this,
 * under some scenarios the marshaller will create a mapping for the
 * {@code suppressedExceptions} field in {@link Throwable} which is not part of
 * the JRE emulation library in GWT 2.5.1.
 * 
 * @author Max Barkley <mbarkley@redhat.com>
 */
@CustomMapping(value = SecurityException.class)
@InheritedMappings(
        value = { UnauthenticatedException.class, UnauthorizedException.class, AuthenticationException.class })
public class SecurityExceptionMapping extends MappingDefinition {

  public SecurityExceptionMapping() {
    super(SecurityException.class);

    SimpleConstructorMapping constructorMapping = new SimpleConstructorMapping();
    constructorMapping.mapParmToIndex("message", 0, String.class);
    setInstantiationMapping(constructorMapping);
    addMemberMapping(new WriteMapping("cause", Throwable.class, "initCause"));

    addMemberMapping(new AccessorMapping("stackTrace", StackTraceElement[].class, "setStackTrace", "getStackTrace"));

    addMemberMapping(new ReadMapping("message", String.class, "getMessage"));
    addMemberMapping(new ReadMapping("cause", Throwable.class, "getCause"));
  }

}
