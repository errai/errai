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
package org.jboss.errai.security.demo.client.local;

import static org.jboss.errai.security.shared.api.identity.User.StandardUserProperties.FIRST_NAME;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.jboss.errai.bus.client.api.BusErrorCallback;
import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.ioc.client.api.AfterInitialization;
import org.jboss.errai.security.shared.api.identity.User;
import org.jboss.errai.security.shared.event.LoggedInEvent;
import org.jboss.errai.security.shared.event.LoggedOutEvent;
import org.jboss.errai.security.shared.service.AuthenticationService;
import org.jboss.errai.ui.nav.client.local.DefaultPage;
import org.jboss.errai.ui.nav.client.local.Page;
import org.jboss.errai.ui.nav.client.local.TransitionTo;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;

@ApplicationScoped
@Templated("#root")
@Page(role = DefaultPage.class)
public class WelcomePage extends Composite {

  static final String ANONYMOUS = "anonymous";

  @Inject
  public
  @DataField
  Button startButton;

  @Inject
  @DataField
  private Label userLabel;
  
  @Inject
  private Caller<AuthenticationService> authCaller;

  @Inject
  TransitionTo<Messages> startButtonClicked;

  @EventHandler("startButton")
  public void onStartButtonPress(ClickEvent e) {
    startButtonClicked.go();
  }

  @AfterInitialization
  private void setupUserLabel() {
    authCaller.call(new RemoteCallback<User>() {

      @Override
      public void callback(final User user) {
        userLabel.setText(user.getProperty(FIRST_NAME) != null ? user.getProperty(FIRST_NAME) : ANONYMOUS);
      }
    }, new BusErrorCallback() {

      @Override
      public boolean error(Message message, Throwable throwable) {
        userLabel.setText(ANONYMOUS);
        return true;
      }
    }).getUser();
  }

  @SuppressWarnings("unused")
  private void onLoggedIn(@Observes LoggedInEvent loggedInEvent) {
    userLabel.setText(loggedInEvent.getUser().getProperty(FIRST_NAME));
  }

  @SuppressWarnings("unused")
  private void onLoggedOut(@Observes LoggedOutEvent loggedOutEvent) {
    userLabel.setText(ANONYMOUS);
  }
}
