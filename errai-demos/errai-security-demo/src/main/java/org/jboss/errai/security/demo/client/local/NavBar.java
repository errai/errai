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
import org.jboss.errai.common.client.dom.Anchor;
import org.jboss.errai.common.client.dom.MouseEvent;
import org.jboss.errai.security.shared.api.annotation.RestrictedAccess;
import org.jboss.errai.security.shared.service.AuthenticationService;
import org.jboss.errai.ui.nav.client.local.Navigation;
import org.jboss.errai.ui.nav.client.local.api.LoginPage;
import org.jboss.errai.ui.nav.client.local.api.TransitionTo;
import org.jboss.errai.ui.nav.client.local.api.TransitionToRole;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.ForEvent;
import org.jboss.errai.ui.shared.api.annotations.Templated;

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

  @Inject
  @DataField
  @TransitionTo(Messages.class)
  Anchor messages;

  @Inject
  @DataField
  @TransitionToRole(LoginPage.class)
  @RestrictedAccess
  Anchor login;

  @Inject
  @DataField
  @TransitionTo(AdminPage.class)
  @RestrictedAccess(roles = "admin")
  Anchor admin;

  @Inject
  @DataField
  @RestrictedAccess
  Anchor logout;

  @Inject org.jboss.errai.ui.nav.client.local.TransitionTo<WelcomePage> welcomePage;

  @Inject Caller<AuthenticationService> authServiceCaller;

  @EventHandler("logout")
  public void logoutClicked(final @ForEvent("click") MouseEvent event) {
    authServiceCaller.call(response -> welcomePage.go()).logout();
  }
}
