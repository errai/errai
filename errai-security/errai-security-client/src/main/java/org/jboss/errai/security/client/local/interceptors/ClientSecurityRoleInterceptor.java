package org.jboss.errai.security.client.local.interceptors;

import org.jboss.errai.common.client.api.ErrorCallback;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.common.client.api.interceptor.FeatureInterceptor;
import org.jboss.errai.common.client.api.interceptor.RemoteCallContext;
import org.jboss.errai.common.client.api.interceptor.RemoteCallInterceptor;
import org.jboss.errai.common.client.util.CreationalCallback;
import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.security.client.local.identity.ActiveUserProvider;
import org.jboss.errai.security.shared.RequireRoles;
import org.jboss.errai.security.shared.SecurityInterceptor;
import org.jboss.errai.security.shared.exception.SecurityException;
import org.jboss.errai.security.shared.exception.UnauthenticatedException;
import org.jboss.errai.security.shared.exception.UnauthorizedException;
import org.jboss.errai.security.shared.util.AnnotationUtils;

/**
 * Intercepts RPC calls to resources marked with {@link RequireRoles}. This
 * interceptor throws an {@link UnauthenticatedException} if the user is not
 * logged in, and a {@link UnauthorizedException} if the user does not have the
 * required roles.
 * 
 * @author edewit@redhat.com
 * @author Max Barkley <mbarkley@redhat.com>
 */
@FeatureInterceptor(RequireRoles.class)
public class ClientSecurityRoleInterceptor extends SecurityInterceptor implements
        RemoteCallInterceptor<RemoteCallContext> {
  @Override
  public void aroundInvoke(final RemoteCallContext context) {
    securityCheck(
            AnnotationUtils.mergeRoles(
                    getRequiredRoleAnnotation(context.getTypeAnnotations()),
                    getRequiredRoleAnnotation(context.getAnnotations())),
            context);
  }

  private void securityCheck(final String[] values, final RemoteCallContext context) {
    IOC.getAsyncBeanManager().lookupBean(ActiveUserProvider.class)
            .getInstance(new CreationalCallback<ActiveUserProvider>() {

              @Override
              public void callback(final ActiveUserProvider provider) {
                if (provider.isCacheValid()) {
                  if (provider.hasActiveUser()) {
                    if (hasAllRoles(provider.getActiveUser().getRoles(), values)) {
                      context.proceed(new RemoteCallback<Object>() {

                        @Override
                        public void callback(final Object response) {
                          context.setResult(response);
                        }
                      }, new ErrorCallback<Object>() {

                        @Override
                        public boolean error(Object message, Throwable throwable) {
                          if (throwable instanceof SecurityException) {
                            provider.invalidateCache();
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
                  context.proceed();
                }
              }
            });
  }

}
