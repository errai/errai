/*
 * Copyright 2009 JBoss, a divison Red Hat, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.workspaces.client.widgets;

import com.google.gwt.event.dom.client.*;
import com.google.gwt.user.client.ui.*;
import org.jboss.errai.bus.client.security.AuthenticationHandler;
import org.jboss.errai.bus.client.security.Credential;
import org.jboss.errai.bus.client.security.impl.NameCredential;
import org.jboss.errai.bus.client.security.impl.PasswordCredential;
import org.jboss.errai.common.client.framework.WSComponent;
import org.jboss.errai.workspaces.client.Workspace;

public class WSLoginPanel implements WSComponent {
    public Widget getWidget() {
        return new Composite() {
            {
                VerticalPanel mainPanel = new VerticalPanel();
                HorizontalPanel login = new HorizontalPanel();
                HorizontalPanel password = new HorizontalPanel();
                HorizontalPanel buttons = new HorizontalPanel();

                mainPanel.add(login);
                mainPanel.add(password);
                mainPanel.add(buttons);

                Label userNameLabel = new Label("User:");
                Label passwordLabel = new Label("Password:");

                final TextBox userNameInput = new TextBox();
                final TextBox passwordInput = new PasswordTextBox();

                login.add(userNameLabel);
                login.add(userNameInput);
                login.setWidth("100%");
                login.setCellHorizontalAlignment(userNameInput, HasAlignment.ALIGN_RIGHT);

                password.add(passwordLabel);
                password.add(passwordInput);
                password.setWidth("100%");
                password.setCellHorizontalAlignment(passwordInput, HasAlignment.ALIGN_RIGHT);

                final Button loginButton = new Button("Login");
                buttons.add(loginButton);
                buttons.setWidth("100%");
                buttons.setCellHorizontalAlignment(loginButton, HasAlignment.ALIGN_RIGHT);

                loginButton.addClickHandler(new ClickHandler() {
                    public void onClick(ClickEvent event) {
                        Workspace.getSecurityService().doAuthentication(
                                new AuthenticationHandler() {
                                    public void doLogin(Credential[] credentials) {
                                        for (Credential c : credentials) {
                                            if (c instanceof NameCredential) {
                                                ((NameCredential) c).setName(userNameInput.getText());
                                            }
                                            else if (c instanceof PasswordCredential) {
                                                ((PasswordCredential) c).setPassword(passwordInput.getText());
                                            }
                                        }
                                    }
                                });
                    }
                });

                KeyDownHandler formSubmit = new KeyDownHandler() {
                    public void onKeyDown(KeyDownEvent event) {
                        if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
                            loginButton.click();
                        }
                    }
                };

                userNameInput.addKeyDownHandler(formSubmit);
                passwordInput.addKeyDownHandler(formSubmit);

                initWidget(mainPanel);
            }
        };
    }

}
