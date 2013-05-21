package org.jboss.errai.security.client.local;

import com.google.gwt.user.client.Cookies;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.common.client.api.interceptor.RemoteCallContext;
import org.jboss.errai.common.client.api.interceptor.RemoteCallInterceptor;
import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.security.shared.LoginPage;
import org.jboss.errai.ui.nav.client.local.Navigation;

import static org.jboss.errai.security.shared.LoginPage.CURRENT_PAGE_COOKIE;

/**
 * @author edewit@redhat.com
 */
public abstract class SecurityInterceptor implements RemoteCallInterceptor<RemoteCallContext> {

  protected static void proceed(final RemoteCallContext context) {
    context.proceed(new RemoteCallback<Object>() {
      @Override
      public void callback(Object response) {
        context.setResult(response);
      }
    });
  }

  protected void navigateToLoginPage() {
    Navigation navigation = IOC.getBeanManager().lookupBean(Navigation.class).getInstance();
    Cookies.setCookie(CURRENT_PAGE_COOKIE, navigation.getCurrentPage().name());
    navigation.goToWithRole(LoginPage.class);
  }
}
