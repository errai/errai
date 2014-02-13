package org.jboss.errai.security.client.local;

import static org.jboss.errai.ui.nav.client.local.api.LoginPage.CURRENT_PAGE_COOKIE;

import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.security.shared.SecurityInterceptor;
import org.jboss.errai.ui.nav.client.local.Navigation;
import org.jboss.errai.ui.nav.client.local.UniquePageRole;
import org.jboss.errai.ui.nav.client.local.api.LoginPage;

import com.google.gwt.user.client.Cookies;

public class ClientSecurityInterceptor extends SecurityInterceptor {

  protected void navigateToLoginPage() {
    navigateToPage(LoginPage.class);
  }

  protected void navigateToPage(Class<? extends UniquePageRole> roleClass) {
    Navigation navigation = IOC.getBeanManager().lookupBean(Navigation.class).getInstance();
    Cookies.setCookie(CURRENT_PAGE_COOKIE, navigation.getCurrentPage().name());
    navigation.goToWithRole(roleClass);
  }

}
