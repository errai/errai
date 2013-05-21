package org.jboss.errai.security.client.local;

import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.common.client.api.interceptor.RemoteCallContext;
import org.jboss.errai.security.shared.SecurityManager;

/**
 * @author edewit@redhat.com
 */
public class SecurityUserInterceptor extends SecurityInterceptor {

  @Override
  public void aroundInvoke(final RemoteCallContext context) {
    final SecurityManager securityManager = MessageBuilder.createCall(new RemoteCallback<Boolean>() {
      @Override
      public void callback(final Boolean loggedIn) {
        if (loggedIn) {
          proceed(context);
        } else {
          navigateToLoginPage();
        }
      }
    }, SecurityManager.class);

    securityManager.isLoggedIn();
  }
}
