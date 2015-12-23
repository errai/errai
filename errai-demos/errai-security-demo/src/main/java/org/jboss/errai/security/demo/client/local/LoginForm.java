/*
 * Copyright (C) 2014 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.security.demo.client.local;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.bus.client.api.BusErrorCallback;
import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.security.client.local.api.SecurityContext;
import org.jboss.errai.security.shared.api.identity.User;
import org.jboss.errai.security.shared.service.AuthenticationService;
import org.jboss.errai.ui.client.widget.AbstractForm;
import org.jboss.errai.ui.nav.client.local.Page;
import org.jboss.errai.ui.nav.client.local.PageShowing;
import org.jboss.errai.ui.nav.client.local.api.LoginPage;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.slf4j.Logger;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.FormElement;
import com.google.gwt.dom.client.Style.Visibility;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.TextBox;

/**
 * A {@link LoginPage} that uses the {@link AuthenticationService}.
 *
 * The primary of method of logging in users in Errai Security is the injecting
 * a {@link Caller}, used to build an RPC to {@link AuthenticationService}.
 * Caching is automatically handled so that after logging in or out, subsequent
 * calls to {@link AuthenticationService#getUser()} do not require the network.
 */
@Page(role = LoginPage.class)
@Templated("#root")
@Dependent
public class LoginForm extends AbstractForm {

  @Inject
  private Logger logger;

  @Inject
  private Caller<AuthenticationService> authServiceCaller;

  @Inject
  private SecurityContext securityContext;

  @Inject
  @DataField
  private TextBox username;

  @Inject
  @DataField
  private PasswordTextBox password;

  @DataField
  private final FormElement form = Document.get().createFormElement();

  @Inject
  @DataField
  private Button login;

  @DataField
  private Element alert = DOM.createDiv();

  @Inject
  @DataField
  @Keycloak
  private Anchor keycloakAnchor;

  @Override
  protected FormElement getFormElement() {
    return form;
  }

  @EventHandler("login")
  private void loginClicked(ClickEvent event) {
    authServiceCaller.call(new RemoteCallback<User>() {

      @Override
      public void callback(final User user) {
        /*
         * This triggers most browsers to prompt users to remember their
         * credentials.
         */
        submit();
        securityContext.navigateBackOrHome();
      }
    }, new BusErrorCallback() {
      @Override
      public boolean error(Message message, Throwable throwable) {
        logger.error("Login failure reason: ", throwable);
        alert.getStyle().setVisibility(Visibility.VISIBLE);
        return false;
      }
    }).login(username.getText(), password.getText());
  }

  @PageShowing
  private void isLoggedIn() {
    authServiceCaller.call(new RemoteCallback<User>() {

      @Override
      public void callback(final User user) {
        if (!User.ANONYMOUS.equals(user)) {
          login.setEnabled(false);
        }
      }
    }).getUser();
  }

}
