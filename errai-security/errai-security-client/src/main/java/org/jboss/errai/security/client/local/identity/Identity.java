package org.jboss.errai.security.client.local.identity;

import java.io.Serializable;

import javax.enterprise.context.Dependent;

import org.jboss.errai.bus.client.api.BusErrorCallback;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.databinding.client.api.Bindable;
import org.jboss.errai.security.shared.AuthenticationService;
import org.jboss.errai.security.shared.Role;
import org.jboss.errai.security.shared.User;

/**
 * Identity holds the username and password and performs the authentication
 * tasks.
 * 
 * @author edewit@redhat.com
 * @author Max Barkley <mbarkley@redhat.com>
 */
@Bindable
@Dependent
public class Identity implements Serializable {

  private class CallbackWrapper<T> implements RemoteCallback<T> {
    private final RemoteCallback<T> callback;

    private CallbackWrapper(final RemoteCallback<T> callback) {
      this.callback = callback;
    }

    @Override
    public void callback(final T response) {
      if (callback != null)
        callback.callback(response);
    }

  }

  private static final long serialVersionUID = 1L;
  private String username;
  private String password;

  public void login(final RemoteCallback<User> callback, final BusErrorCallback errorCallback) {
    final RemoteCallback<User> callbackWrapper = new CallbackWrapper<User>(callback);

    if (errorCallback != null)
      MessageBuilder.createCall(callbackWrapper, errorCallback, AuthenticationService.class).login(username, password);
    else
      MessageBuilder.createCall(callbackWrapper, AuthenticationService.class).login(username, password);
  }

  public void login(final RemoteCallback<User> callback) {
    login(callback, null);
  }

  public void logout() {
    MessageBuilder.createCall(new VoidRemoteCallback(), AuthenticationService.class).logout();
  }

  public void getUser(final RemoteCallback<User> callback) {
    MessageBuilder.createCall(new CallbackWrapper<User>(callback), AuthenticationService.class).getUser();
  }
  
  public void getUser(final RemoteCallback<User> callback, final BusErrorCallback errorCallback) {
    MessageBuilder.createCall(callback, errorCallback, AuthenticationService.class).getUser();
  }

  public void hasPermission(final RemoteCallback<Boolean> callback, final BusErrorCallback errorCallback,
          final String... roleNames) {
    final RemoteCallback<Boolean> callbackWrapper = new CallbackWrapper<Boolean>(callback);
    final RemoteCallback<User> permissionCallback = new RemoteCallback<User>() {
      @Override
      public void callback(final User user) {
        if (user == null) {
          callbackWrapper.callback(false);
          return;
        }
        for (String roleName : roleNames) {
          final Role role = new Role(roleName);
          if (!user.getRoles().contains(role)) {
            callbackWrapper.callback(false);
            return;
          }
        }
        callbackWrapper.callback(true);
      }
    };
    
    if (errorCallback == null)
      MessageBuilder.createCall(permissionCallback, AuthenticationService.class).getUser();
    else
      MessageBuilder.createCall(permissionCallback, errorCallback, AuthenticationService.class).getUser();
  }
  
  public void hasPermission(final RemoteCallback<Boolean> callback) {
    hasPermission(callback, null);
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
