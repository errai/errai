package org.jboss.errai.security.client.local;

import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.databinding.client.api.Bindable;
import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.security.shared.Role;
import org.jboss.errai.security.shared.AuthenticationService;
import org.jboss.errai.security.shared.User;
import org.jboss.errai.ui.nav.client.local.Navigation;
import org.jboss.errai.ui.shared.api.style.StyleBindingsRegistry;

import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.util.List;

import static org.jboss.errai.security.shared.LoginPage.CURRENT_PAGE_COOKIE;

/**
 * @author edewit@redhat.com
 */
@Bindable
@SessionScoped
public class Identity implements Serializable {
  private String username;
  private String password;

  public void login() {
    MessageBuilder.createCall(new VoidRemoteCallback(), AuthenticationService.class).login(username, password);
    StyleBindingsRegistry.get().updateStyles();
    final String page = Cookies.getCookie(CURRENT_PAGE_COOKIE);
    if (page != null) {
      Cookies.removeCookie(CURRENT_PAGE_COOKIE);
      IOC.getBeanManager().lookupBean(Navigation.class).getInstance().goTo(page);
    }
  }

  public void logout() {
    MessageBuilder.createCall(new VoidRemoteCallback(), AuthenticationService.class).logout();
    StyleBindingsRegistry.get().updateStyles();
  }

  public void getUser(final AsyncCallback<User> callback) {
    MessageBuilder.createCall(new RemoteCallback<User>() {
      @Override
      public void callback(User response) {
        callback.onSuccess(response);
      }
    }, AuthenticationService.class).getUser();
  }

  public void hasPermission(final AsyncCallback<Boolean> callback, final String... roleNames) {
    MessageBuilder.createCall(new RemoteCallback<List<Role>>() {
      @Override
      public void callback(List<Role> roles) {
        for (String roleName : roleNames) {
          final Role role = new Role(roleName);
          if (!roles.contains(role)) {
            callback.onSuccess(false);
            return;
          }
        }
        callback.onSuccess(true);
      }
    }, AuthenticationService.class).getRoles();
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  private static class VoidRemoteCallback implements RemoteCallback<Void> {
    @Override
    public void callback(final Void response) {
    }
  }
}
