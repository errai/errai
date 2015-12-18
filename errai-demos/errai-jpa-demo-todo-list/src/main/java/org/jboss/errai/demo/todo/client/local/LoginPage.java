/*
 * Copyright (C) 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.demo.todo.client.local;

import javax.inject.Inject;

import org.jboss.errai.bus.client.api.BusErrorCallback;
import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.security.shared.api.identity.User;
import org.jboss.errai.security.shared.service.AuthenticationService;
import org.jboss.errai.ui.nav.client.local.Page;
import org.jboss.errai.ui.nav.client.local.PageShowing;
import org.jboss.errai.ui.nav.client.local.TransitionAnchor;
import org.jboss.errai.ui.nav.client.local.TransitionTo;
import org.jboss.errai.ui.nav.client.local.api.SecurityError;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import com.google.gwt.dom.client.Document;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.DomEvent;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.TextBox;

@Templated("#main")
@Page(path = "login", role = { org.jboss.errai.ui.nav.client.local.api.LoginPage.class, SecurityError.class })
public class LoginPage extends Composite {
  
  @Inject
  private Caller<AuthenticationService> authCaller;

  @Inject
  private @DataField
  Label loginError;

  @Inject
  private
  @DataField
  TextBox username;
  @Inject
  private
  @DataField
  PasswordTextBox password;

  @Inject
  private @DataField
  Button loginButton;
  @Inject
  private @DataField
  TransitionAnchor<SignupPage> signupLink;

  @Inject
  private TransitionTo<TodoListPage> toListPage;

  @PageShowing
  private void onPageShowing() {
    loginError.setVisible(false);
  }

  @EventHandler
  private void hideErrorLabel(FocusEvent e) {
    loginError.setVisible(false);
  }

  @EventHandler("loginButton")
  private void login(ClickEvent e) {
    authCaller.call(new RemoteCallback<User>() {
      @Override
      public void callback(User user) {
        toListPage.go();
      }
    }, new BusErrorCallback() {
      @Override
      public boolean error(Message message, Throwable throwable) {
        loginError.setVisible(true);

        return false;
      }
    }).login(username.getText(), password.getText());
  }

  @EventHandler({ "username", "password" })
  private void onKeyDown(KeyDownEvent event) {
    // fire change events to update the binder properties
    DomEvent.fireNativeEvent(Document.get().createChangeEvent(), password);
    DomEvent.fireNativeEvent(Document.get().createChangeEvent(), username);
    // auto-submit login form when user presses enter
    if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER &&
            !username.getText().trim().equals("") &&
            !password.getText().trim().equals("")) {
      login(null);
    }
  }
}
