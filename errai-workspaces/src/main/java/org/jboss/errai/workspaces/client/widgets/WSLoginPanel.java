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

import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.user.client.ui.*;
import org.jboss.errai.common.client.framework.WSComponent;
import org.jboss.errai.common.client.framework.WidgetCallback;

public class WSLoginPanel implements WSComponent {

  private TextBox userNameInput = new TextBox();
  private TextBox passwordInput = new PasswordTextBox();
  private Button loginButton;

  private Composite widget;

  public WSLoginPanel()
  {
    this.widget = new Composite() {
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

        userNameInput = new TextBox();
        passwordInput = new PasswordTextBox();

        login.add(userNameLabel);
        login.add(userNameInput);
        login.setWidth("100%");
        login.setCellHorizontalAlignment(userNameInput, HasAlignment.ALIGN_RIGHT);
        password.add(passwordLabel);

        password.add(passwordInput);
        password.setWidth("100%");
        password.setCellHorizontalAlignment(passwordInput, HasAlignment.ALIGN_RIGHT);

        loginButton = new Button("Login");
        buttons.add(loginButton);
        buttons.setWidth("100%");
        buttons.setCellHorizontalAlignment(loginButton, HasAlignment.ALIGN_RIGHT);

        // clickhandler comes form presenter

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

  public Button getLoginButton()
  {
    return loginButton;
  }

  public TextBox getUserNameInput()
  {
    return userNameInput;
  }

  public TextBox getPasswordInput()
  {
    return passwordInput;
  }

  public void getWidget(WidgetCallback callback) {
    callback.onSuccess(this.widget);
  }

}
