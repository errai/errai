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

import static org.jboss.errai.security.shared.api.identity.User.StandardUserProperties.FIRST_NAME;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.enterprise.client.jaxrs.api.RestErrorCallback;
import org.jboss.errai.security.client.local.callback.DefaultBusSecurityErrorCallback;
import org.jboss.errai.security.client.local.callback.DefaultRestSecurityErrorCallback;
import org.jboss.errai.security.demo.client.shared.AdminService;
import org.jboss.errai.security.demo.client.shared.MessageService;
import org.jboss.errai.security.shared.api.annotation.RestrictedAccess;
import org.jboss.errai.security.shared.api.identity.User;
import org.jboss.errai.security.shared.service.AuthenticationService;
import org.jboss.errai.ui.nav.client.local.Page;
import org.jboss.errai.ui.nav.client.local.api.LoginPage;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.slf4j.Logger;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.http.client.Request;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;

/**
 * <p>
 * This {@link Page} demonstrates RPC service secured with
 * {@link RestrictedAccess}.
 *
 * <p>
 * {@link MessageService} is an Errai Bus RPC service. If the service is called
 * without an authenticated user, a {@link DefaultBusSecurityErrorCallback}
 * navigates to the {@link LoginPage}.
 *
 * <p>
 * {@link AdminService} is a JAX-RS endpoint. There is no global error-handling
 * for JAX-RS RPCs so {@link DefaultRestSecurityErrorCallback} is passed in
 * manually. This error handler will also navigate to the {@link LoginPage} or
 * {@link SecurityErrorPage}.
 */
@Dependent
@Templated("#main")
@Page
public class Messages {

  @Inject
  private Caller<AuthenticationService> authCaller;

  @Inject
  private Caller<MessageService> messageServiceCaller;

  @Inject
  private Caller<AdminService> adminServiceCaller;

  @Inject
  @DataField("newItemForm")
  private Label label;

  @Inject
  @DataField
  private Button hello;

  @Inject
  @DataField
  private Button ping;

  @Inject
  private Instance<DefaultRestSecurityErrorCallback> defaultCallbackInstance;

  @Inject
  private Logger logger;

  @EventHandler("hello")
  private void onHelloClicked(ClickEvent event) {
    logger.info("Messages.onHelloClicked");
    authCaller.call(new RemoteCallback<User>() {

      @Override
      public void callback(User response) {
        messageServiceCaller.call(
                new RemoteCallback<String>() {
                  @Override
                  public void callback(String o) {
                    label.setText(o);
                  }
                }).hello();
      }

    }).getUser();
  }

  @EventHandler("ping")
  private void onPingClicked(ClickEvent event) {
    adminServiceCaller.call(new RemoteCallback<String>() {
      @Override
      public void callback(String o) {
        label.setText(o);
      }
    }, defaultCallbackInstance.get().setWrappedErrorCallback(new RestErrorCallback() {
      @Override
      public boolean error(Request message, Throwable throwable) {
        authCaller.call(new RemoteCallback<User>() {

          @Override
          public void callback(User user) {
            final String name = (user.getProperty(FIRST_NAME) != null) ? user.getProperty(FIRST_NAME) : "Anonymous";
            logger.warn(name + " has attempted to access a protected resource!");
          }

        }).getUser();

        // By returning true here, the default security redirection logic will
        // occur.
        return true;
      }
    })).ping();
  }
}
