package org.jboss.errai.security.client.local;

import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.common.client.api.interceptor.RemoteCallContext;
import org.jboss.errai.common.client.api.interceptor.RemoteCallInterceptor;
import org.jboss.errai.security.shared.RequireRoles;
import org.jboss.errai.security.shared.Role;
import org.jboss.errai.security.shared.SecurityManager;

import java.lang.annotation.Annotation;
import java.util.List;

/**
 * @author edewit@redhat.com
 */
public class SecurityRoleInterceptor extends SecurityInterceptor{
  @Override
  public void aroundInvoke(final RemoteCallContext context) {
    final SecurityManager securityManager = MessageBuilder.createCall(new RemoteCallback<List<Role>>() {
      @Override
      public void callback(final List<Role> roles) {
        final RequireRoles annotation = getRequiredRoleAnnotation(context.getAnnotations());
        final String[] roleNames = annotation.value();
        for (String roleName : roleNames) {
          final Role role = new Role(roleName);
          if (!roles.contains(role)) {
            navigateToLoginPage();
            return;
          }
        }
        proceed(context);
      }
    }, SecurityManager.class);

    securityManager.getRoles();
  }

  private RequireRoles getRequiredRoleAnnotation(Annotation[] annotations) {
    for (Annotation annotation : annotations) {
      if (annotation instanceof RequireRoles) {
        return (RequireRoles) annotation;
      }
    }
    return null;
  }
}
