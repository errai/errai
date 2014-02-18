package org.jboss.errai.security.client.local.interceptors;

import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.common.client.api.interceptor.InterceptsRemoteCall;
import org.jboss.errai.common.client.api.interceptor.RemoteCallContext;
import org.jboss.errai.common.client.api.interceptor.RemoteCallInterceptor;
import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.ioc.client.container.async.CreationalCallback;
import org.jboss.errai.security.client.local.identity.ActiveUserProvider;
import org.jboss.errai.security.shared.AuthenticationService;
import org.jboss.errai.security.shared.User;

@InterceptsRemoteCall({ AuthenticationService.class })
public class LoginInterceptor implements RemoteCallInterceptor<RemoteCallContext> {

  @Override
  public void aroundInvoke(final RemoteCallContext context) {
    if (context.getMethodName().equals("login")) {
      context.proceed(new RemoteCallback<User>() {

        @Override
        public void callback(final User response) {
          if (response != null) {
            IOC.getAsyncBeanManager().lookupBean(ActiveUserProvider.class)
                    .getInstance(new CreationalCallback<ActiveUserProvider>() {

                      @Override
                      public void callback(final ActiveUserProvider provider) {
                        provider.setActiveUser(response);
                      }
                    });
          }
        }
      });
    }
    else {
      context.proceed();
    }
  }

}
