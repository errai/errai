package org.jboss.errai.security.shared.interceptor;

import java.lang.annotation.Annotation;
import java.util.List;

import org.jboss.errai.security.shared.api.annotation.RequireRoles;
import org.jboss.errai.security.shared.api.identity.Role;

/**
 * Base class for the security interceptors
 * @author edewit@redhat.com
 */
public abstract class SecurityInterceptor {

  protected boolean hasAllRoles(List<Role> roles, String[] roleNames) {
    for (String roleName : roleNames) {
      final Role role = new Role(roleName);
      if (!roles.contains(role)) {
        return false;
      }
    }

    return true;
  }

  protected RequireRoles getRequiredRoleAnnotation(Annotation[] annotations) {
    for (Annotation annotation : annotations) {
      if (annotation instanceof RequireRoles) {
        return (RequireRoles) annotation;
      }
    }
    return null;
  }
}
