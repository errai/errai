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

import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.security.shared.api.annotation.RestrictedAccess;
import org.jboss.errai.security.shared.service.AuthenticationService;
import org.jboss.errai.ui.nav.client.local.Navigation;
import org.jboss.errai.ui.nav.client.local.TransitionTo;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import com.google.gwt.event.dom.client.ClickEvent;

import elemental.client.Browser;
import elemental.html.AnchorElement;
import elemental.js.html.JsAnchorElement;

/**
 * <p>
 * A navigation bar that lives outside the {@link Navigation#getContentPanel()
 * navigation content panel} so that it is always displayed on the page.
 *
 * <p>
 * {@link RestrictedAccess} annotated on a field applies a
 * {@link RestrictedAccess#CSS_CLASS_NAME CSS class} to the element when a user
 * is not logged in or lacks the specified roles.
 *
 * <p>
 * {@link #admin}, and {{@link #logout} become hidden when a user lacks roles.
 * {@link #login} uses additional CSS rules so that it is hidden when a user
 * <b>does</b> have proper roles (see application.css in src/main/webapp/css).
 */
@Templated
@Dependent
public class NavBar {

  /*
   * Calling Browser.getDocument().createAnchorElement() makes an <anchor> tag instead of an <a> tag.
   */
  @DataField AnchorElement messages = (JsAnchorElement) Browser.getDocument().createElement("a");
  @DataField @RestrictedAccess AnchorElement login = (JsAnchorElement) Browser.getDocument().createElement("a");
  @DataField @RestrictedAccess(roles = "admin") AnchorElement admin = (JsAnchorElement) Browser.getDocument().createElement("a");
  @DataField @RestrictedAccess AnchorElement logout = (JsAnchorElement) Browser.getDocument().createElement("a");

  @Inject TransitionTo<WelcomePage> welcomePage;
  @Inject TransitionTo<Messages> messagesTab;
  @Inject TransitionTo<LoginForm> loginTab;
  @Inject TransitionTo<AdminPage> adminTab;

  @Inject Caller<AuthenticationService> authServiceCaller;

  @EventHandler("messages")
  public void onHomeButtonClick(ClickEvent e) {
    messagesTab.go();
  }

  @EventHandler("login")
  public void onLoginButtonClicked(ClickEvent event) {
    loginTab.go();
  }

  @EventHandler("admin")
  public void onAdminTabClicked(ClickEvent event) {
    adminTab.go();
  }

  @EventHandler("logout")
  public void logoutClicked(final ClickEvent event) {
    authServiceCaller.call(new RemoteCallback<Void>() {

      @Override
      public void callback(Void response) {
        welcomePage.go();
      }

    }).logout();
  }
}
