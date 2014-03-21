package org.jboss.errai.security.client.local.interceptors;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.common.client.api.ErrorCallback;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.common.client.api.interceptor.FeatureInterceptor;
import org.jboss.errai.common.client.api.interceptor.RemoteCallContext;
import org.jboss.errai.common.client.api.interceptor.RemoteCallInterceptor;
import org.jboss.errai.security.client.local.context.SecurityContext;
import org.jboss.errai.security.shared.api.annotation.RestrictedAccess;
import org.jboss.errai.security.shared.exception.UnauthenticatedException;
import org.jboss.errai.security.shared.exception.UnauthorizedException;
import org.jboss.errai.security.shared.interceptor.SecurityInterceptor;
import org.jboss.errai.security.shared.util.AnnotationUtils;

/**
 * Intercepts RPC calls to resources marked with {@link RestrictedAccess}. This
 * interceptor throws an {@link UnauthenticatedException} if the user is not
 * logged in, and a {@link UnauthorizedException} if the user does not have the
 * required roles.
 * 
 * @author edewit@redhat.com
 * @author Max Barkley <mbarkley@redhat.com>
 */
@FeatureInterceptor(RestrictedAccess.class)
@Dependent
public class ClientSecurityRoleInterceptor extends SecurityInterceptor implements
        RemoteCallInterceptor<RemoteCallContext> {
  
  private final SecurityContext securityContext;
  
  @Inject
  public ClientSecurityRoleInterceptor(final SecurityContext securityContext) {
    this.securityContext = securityContext;
  }

  @Override
  public void aroundInvoke(final RemoteCallContext callContext) {
    securityCheck(
            AnnotationUtils.mergeRoles(
                    getRestrictedAccessAnnotation(callContext.getTypeAnnotations()),
                    getRestrictedAccessAnnotation(callContext.getAnnotations())),
            callContext);
  }

  private void securityCheck(final String[] values, final RemoteCallContext callContext) {
    if (securityContext.isValid()) {
      if (securityContext.hasUser()) {
        if (hasAllRoles(securityContext.getUser().getRoles(), values)) {
          callContext.proceed(new RemoteCallback<Object>() {

            @Override
            public void callback(final Object response) {
              callContext.setResult(response);
            }
          }, new ErrorCallback<Object>() {

            @Override
            public boolean error(Object message, Throwable throwable) {
              if (throwable instanceof UnauthenticatedException) {
                securityContext.invalidateCache();
              }

              return true;
            }
          });
        }
        else {
          throw new UnauthorizedException();
        }
      }
      else {
        throw new UnauthenticatedException();
      }
    }
    else {
      callContext.proceed();
    }
  }

}
