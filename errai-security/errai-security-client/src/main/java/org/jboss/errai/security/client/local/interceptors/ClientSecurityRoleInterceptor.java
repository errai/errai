package org.jboss.errai.security.client.local.interceptors;

import org.jboss.errai.common.client.api.interceptor.FeatureInterceptor;
import org.jboss.errai.common.client.api.interceptor.RemoteCallContext;
import org.jboss.errai.common.client.api.interceptor.RemoteCallInterceptor;
import org.jboss.errai.common.client.util.CreationalCallback;
import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.security.client.local.identity.ActiveUserProvider;
import org.jboss.errai.security.shared.RequireRoles;
import org.jboss.errai.security.shared.exception.UnauthenticatedException;
import org.jboss.errai.security.shared.exception.UnauthorizedException;
import org.jboss.errai.security.shared.util.AnnotationUtils;

/**
 * Will 'redirect' users that try to make use of services annotated with
 * {@link RequireRoles} that are not logged in to the
 * {@link org.jboss.errai.ui.nav.client.local.api.LoginPage} or do not have
 * specified role go to the
 * {@link org.jboss.errai.ui.nav.client.local.api.SecurityError}. In other cases
 * the service call will proceed
 * 
 * @see org.jboss.errai.ui.nav.client.local.api.LoginPage
 * @author edewit@redhat.com
 */
@FeatureInterceptor(RequireRoles.class)
public class ClientSecurityRoleInterceptor extends ClientSecurityInterceptor implements
        RemoteCallInterceptor<RemoteCallContext> {
  @Override
  public void aroundInvoke(final RemoteCallContext context) {
    securityCheck(
            AnnotationUtils.mergeRoles(
                    getRequiredRoleAnnotation(context.getTypeAnnotations()),
                    getRequiredRoleAnnotation(context.getAnnotations())),
            new Command() {
      @Override
      public void action() {
        proceed(context);
      }
    });
  }

  public void securityCheck(final String[] values, final Command command) {
    IOC.getAsyncBeanManager().lookupBean(ActiveUserProvider.class)
            .getInstance(new CreationalCallback<ActiveUserProvider>() {

              @Override
              public void callback(final ActiveUserProvider provider) {
                if (provider.hasActiveUser()) {
                  if (hasAllRoles(provider.getActiveUser().getRoles(), values)) {
                    if (command != null)
                      command.action();
                  }
                  else {
                    throw new UnauthorizedException();
                  }
                }
                else {
                  throw new UnauthenticatedException();
                }
              }
            });
  }

}
