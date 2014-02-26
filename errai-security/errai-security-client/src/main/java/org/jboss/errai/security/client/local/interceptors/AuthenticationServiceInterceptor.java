package org.jboss.errai.security.client.local.interceptors;

import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.common.client.api.interceptor.InterceptsRemoteCall;
import org.jboss.errai.common.client.api.interceptor.RemoteCallContext;
import org.jboss.errai.common.client.api.interceptor.RemoteCallInterceptor;
import org.jboss.errai.common.client.util.CreationalCallback;
import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.security.client.local.identity.ActiveUserProvider;
import org.jboss.errai.security.client.local.identity.ActiveUserProviderImpl;
import org.jboss.errai.security.client.local.identity.UserEventModule;
import org.jboss.errai.security.shared.AuthenticationService;
import org.jboss.errai.security.shared.User;

/**
 * Intercepts RPC logins through {@link AuthenticationService} for populating
 * and removing the current logged in user via {@link ActiveUserProvider}.
 * 
 * @author Max Barkley <mbarkley@redhat.com>
 */
@InterceptsRemoteCall({ AuthenticationService.class })
public class AuthenticationServiceInterceptor implements RemoteCallInterceptor<RemoteCallContext> {

  @Override
  public void aroundInvoke(final RemoteCallContext context) {
    if (context.getMethodName().equals("login")) {
      login(context);
    }
    else if (context.getMethodName().equals("logout")) {
      logout(context);
    }
    else if (context.getMethodName().equals("getUser")) {
      getUser(context);
    }
    else if (context.getMethodName().equals("isLoggedIn")) {
      isLoggedIn(context);
    }
    else {
      context.proceed();
    }
  }

  private void isLoggedIn(final RemoteCallContext context) {
    final ActiveUserProvider provider = ActiveUserProviderImpl.getInstance();
    if (provider.isCacheValid()) {
      context.setResult(provider.hasActiveUser());
    }
    else {
      context.proceed();
    }
  }

  private void login(final RemoteCallContext context) {
    context.proceed(new RemoteCallback<User>() {

      @Override
      public void callback(final User response) {
        if (response != null) {
          IOC.getAsyncBeanManager().lookupBean(ActiveUserProvider.class)
                  .getInstance(new CreationalCallback<ActiveUserProvider>() {

                    @Override
                    public void callback(final ActiveUserProvider provider) {
                      provider.setActiveUser(response);
                      IOC.getAsyncBeanManager().lookupBean(UserEventModule.class)
                              .getInstance(new CreationalCallback<UserEventModule>() {

                                @Override
                                public void callback(final UserEventModule module) {
                                  module.fireLoggedInEvent(response);
                                }
                              });
                    }
                  });
        }
      }
    });
  }

  private void logout(final RemoteCallContext context) {
    IOC.getAsyncBeanManager().lookupBean(ActiveUserProvider.class)
            .getInstance(new CreationalCallback<ActiveUserProvider>() {

              @Override
              public void callback(final ActiveUserProvider provider) {
                provider.setActiveUser(null);
                IOC.getAsyncBeanManager().lookupBean(UserEventModule.class)
                        .getInstance(new CreationalCallback<UserEventModule>() {

                          @Override
                          public void callback(final UserEventModule module) {
                            module.fireLoggedOutEvent();
                          }
                        });
              }
            });
    context.proceed();
  }

  private void getUser(final RemoteCallContext context) {
    final ActiveUserProvider provider = ActiveUserProviderImpl.getInstance();
    if (provider.isCacheValid()) {
      context.setResult(provider.getActiveUser());
    }
    else {
      context.proceed(new RemoteCallback<User>() {

        @Override
        public void callback(final User response) {
          provider.setActiveUser(response);
        }
      });
    }
  }

}
