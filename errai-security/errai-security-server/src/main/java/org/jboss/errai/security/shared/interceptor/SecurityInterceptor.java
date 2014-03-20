package org.jboss.errai.security.shared.interceptor;

import java.lang.annotation.Annotation;
import java.util.Collection;

import org.jboss.errai.security.shared.api.annotation.RestrictAccess;
import org.jboss.errai.security.shared.api.identity.Role;

/**
 * Base class for the security interceptors
 * @author edewit@redhat.com
 */
public abstract class SecurityInterceptor {

  protected boolean hasAllRoles(Collection<Role> roles, String[] roleNames) {
    for (String roleName : roleNames) {
      final Role role = new Role(roleName);
      if (!roles.contains(role)) {
        return false;
      }
    }

    return true;
  }

  protected RestrictAccess getRequiredRoleAnnotation(Annotation[] annotations) {
    for (Annotation annotation : annotations) {
      if (annotation instanceof RestrictAccess) {
        return (RestrictAccess) annotation;
      }
    }
    return null;
  }
}
