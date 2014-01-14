package org.jboss.errai.security.shared;

import static org.jboss.errai.ui.nav.client.local.api.LoginPage.CURRENT_PAGE_COOKIE;

import java.lang.annotation.Annotation;
import java.util.List;

import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.common.client.api.interceptor.RemoteCallContext;
import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.ui.nav.client.local.Navigation;
import org.jboss.errai.ui.nav.client.local.UniquePageRole;
import org.jboss.errai.ui.nav.client.local.api.LoginPage;

import com.google.gwt.user.client.Cookies;

/**
 * Base class for the client side security interceptors
 * @author edewit@redhat.com
 */
public abstract class SecurityInterceptor {

  protected static void proceed(final RemoteCallContext context) {
    context.proceed(new RemoteCallback<Object>() {
      @Override
      public void callback(Object response) {
        context.setResult(response);
      }
    });
  }

  protected void navigateToLoginPage() {
    navigateToPage(LoginPage.class);
  }

  protected void navigateToPage(Class<? extends UniquePageRole> roleClass) {
    Navigation navigation = IOC.getBeanManager().lookupBean(Navigation.class).getInstance();
    Cookies.setCookie(CURRENT_PAGE_COOKIE, navigation.getCurrentPage().name());
    navigation.goToWithRole(roleClass);
  }

  protected boolean hasAllRoles(List<Role> roles, String[] roleNames) {
    for (String roleName : roleNames) {
      final Role role = new Role(roleName);
      if (!roles.contains(role)) {
        return false;
      }
    }

    return true;
  }

  protected RequireRoles getRequiredRoleAnnotation(Annotation[] annotations) {
    for (Annotation annotation : annotations) {
      if (annotation instanceof RequireRoles) {
        return (RequireRoles) annotation;
      }
    }
    return null;
  }
  
  /* Poor mans closures */
  public static interface Command {
    void action();
  }
}
