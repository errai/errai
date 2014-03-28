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

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.enterprise.client.jaxrs.api.RestErrorCallback;
import org.jboss.errai.security.client.local.api.SecurityContext;
import org.jboss.errai.security.client.local.callback.DefaultRestSecurityErrorCallback;
import org.jboss.errai.security.demo.client.shared.MessageService;
import org.jboss.errai.security.shared.api.identity.User;
import org.jboss.errai.security.shared.service.AuthenticationService;
import org.jboss.errai.ui.nav.client.local.Page;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.slf4j.Logger;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.http.client.Request;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;

@Dependent
@Templated("#main")
@Page
public class Messages extends Composite {
  
  @Inject
  private Caller<AuthenticationService> authCaller;

  @Inject
  private Caller<MessageService> messageServiceCaller;

  @Inject
  @DataField("newItemForm")
  private Label label;

  @Inject
  private SecurityContext securityContext;

  @Inject
  @DataField
  private Button hello;

  @Inject
  @DataField
  private Button ping;

  @Inject
  private Logger logger;

  @EventHandler("hello")
  private void onHelloClicked(ClickEvent event) {
    System.out.println("Messages.onHelloClicked");
    authCaller.call(new RemoteCallback<User>() {

      @Override
      public void callback(User response) {
        messageServiceCaller.call(
                new RemoteCallback<String>() {
                  @Override
                  public void callback(String o) {
                    label.setText(o);
                  }
                }, new DefaultRestSecurityErrorCallback(securityContext)).hello();
      }

    }).getUser();
  }

  @EventHandler("ping")
  private void onPingClicked(ClickEvent event) {
    messageServiceCaller.call(new RemoteCallback<String>() {
      @Override
      public void callback(String o) {
        label.setText(o);
      }
    }, new DefaultRestSecurityErrorCallback(new RestErrorCallback() {
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
    }, securityContext)).ping();
  }
}
