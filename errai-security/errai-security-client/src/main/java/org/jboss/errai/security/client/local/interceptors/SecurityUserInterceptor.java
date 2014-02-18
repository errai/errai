package org.jboss.errai.security.client.local.interceptors;

import org.jboss.errai.common.client.api.interceptor.FeatureInterceptor;
import org.jboss.errai.common.client.api.interceptor.RemoteCallContext;
import org.jboss.errai.common.client.api.interceptor.RemoteCallInterceptor;
import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.ioc.client.container.async.CreationalCallback;
import org.jboss.errai.security.client.local.identity.ActiveUserProvider;
import org.jboss.errai.security.shared.RequireAuthentication;
import org.jboss.errai.security.shared.SecurityException;

/**
 * SecurityUserInterceptor will intercept calls annotated with
 * {@link org.jboss.errai.security.shared.RequireAuthentication} and check
 * cached credentials to see if the user is authenticated. If not, this
 * interceptor throws an error.
 * 
 * @author edewit@redhat.com
 */
@FeatureInterceptor(RequireAuthentication.class)
public class SecurityUserInterceptor extends ClientSecurityInterceptor implements
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
                  throw new SecurityException();
                }
              }
            });
  }

  public void securityCheck() {
    securityCheck(null);
  }
}
