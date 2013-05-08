package org.jboss.errai.security.client.local;

import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.common.client.api.interceptor.RemoteCallContext;
import org.jboss.errai.common.client.api.interceptor.RemoteCallInterceptor;
import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.security.shared.SecurityManager;
import org.jboss.errai.ui.nav.client.local.Navigation;

/**
 * @author edewit@redhat.com
 */
public class SecurityInterceptor implements RemoteCallInterceptor<RemoteCallContext> {

  @Override
  public void aroundInvoke(final RemoteCallContext context) {
    final Navigation navigation = IOC.getBeanManager().lookupBean(Navigation.class).getInstance();
    final SecurityManager securityManager = MessageBuilder.createCall(new RemoteCallback<Boolean>() {
      @Override
      public void callback(final Boolean loggedIn) {
        if (loggedIn) {
          context.proceed(new RemoteCallback<String>() {
            @Override
            public void callback(String response) {
              context.setResult(response);
            }
          });
        } else {
          navigation.goToLogin();
        }
      }
    }, SecurityManager.class);

    securityManager.isLoggedIn();
  }
}
