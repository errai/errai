package org.jboss.errai.security.client.local;

import com.google.gwt.user.client.Cookies;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.common.client.api.interceptor.RemoteCallContext;
import org.jboss.errai.common.client.api.interceptor.RemoteCallInterceptor;
import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.security.shared.LoginPage;
import org.jboss.errai.security.shared.SecurityManager;
import org.jboss.errai.ui.nav.client.local.Navigation;

import static org.jboss.errai.security.shared.LoginPage.CURRENT_PAGE_COOKIE;

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
          Cookies.setCookie(CURRENT_PAGE_COOKIE, navigation.getCurrentPage().name());
          navigation.goToWithRole(LoginPage.class);
        }
      }
    }, SecurityManager.class);

    securityManager.isLoggedIn();
  }
}
