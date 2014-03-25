/**
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.errai.security.client.local.identity;

import java.io.Serializable;

import javax.enterprise.context.Dependent;

import org.jboss.errai.bus.client.api.BusErrorCallback;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.databinding.client.api.Bindable;
import org.jboss.errai.security.shared.api.identity.Role;
import org.jboss.errai.security.shared.api.identity.User;
import org.jboss.errai.security.shared.service.AuthenticationService;

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
