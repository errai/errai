package org.jboss.errai.security.client.local.interceptors;

import org.jboss.errai.common.client.api.interceptor.FeatureInterceptor;
import org.jboss.errai.common.client.api.interceptor.RemoteCallContext;
import org.jboss.errai.common.client.api.interceptor.RemoteCallInterceptor;
import org.jboss.errai.common.client.util.CreationalCallback;
import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.security.client.local.identity.ActiveUserProvider;
import org.jboss.errai.security.shared.RequireAuthentication;
import org.jboss.errai.security.shared.SecurityInterceptor;
import org.jboss.errai.security.shared.exception.UnauthenticatedException;

/**
 * Intercepts RPC calls to resources marked with {@link RequireAuthentication}.
 * This interceptor throws an {@link UnauthenticatedException} if the user is
 * not logged in.
 * 
 * @author edewit@redhat.com
 * @author Max Barkley <mbarkley@redhat.com>
 */
@FeatureInterceptor(RequireAuthentication.class)
public class SecurityUserInterceptor extends SecurityInterceptor implements
        RemoteCallInterceptor<RemoteCallContext> {

  @Override
  public void aroundInvoke(final RemoteCallContext context) {
    securityCheck(new Command() {
      @Override
      public void action() {
        proceed(context);
      }
    });
  }

  private void securityCheck(final Command command) {
    IOC.getAsyncBeanManager().lookupBean(ActiveUserProvider.class)
            .getInstance(new CreationalCallback<ActiveUserProvider>() {
              @Override
              public void callback(ActiveUserProvider provider) {
                if (provider.hasActiveUser()) {
                  if (command != null)
                    command.action();
                }
                else {
                  throw new UnauthenticatedException();
                }
              }
            });
  }

  public void securityCheck() {
    securityCheck(null);
  }
}
