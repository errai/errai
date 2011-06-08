/*
* Copyright 2010 JBoss, a divison Red Hat, Inc
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

package org.jboss.errai.workspaces.client.modules.auth;

import com.google.gwt.event.dom.client.*;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.HasCloseHandlers;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.*;
import org.gwt.mosaic.ui.client.Caption;
import org.gwt.mosaic.ui.client.MessageBox;
import org.gwt.mosaic.ui.client.WindowPanel;
import org.gwt.mosaic.ui.client.layout.BoxLayout;
import org.gwt.mosaic.ui.client.layout.LayoutPanel;

/**
 * A mosaic based based login box
 */
public class AuthenticationDisplay extends LayoutPanel
    implements AuthenticationModule.Display

{
  private TextBox userNameInput;
  private PasswordTextBox passwordInput;
  private Button loginButton;

  private WindowPanel windowPanel;

  public AuthenticationDisplay() {
    super();

    userNameInput = new TextBox();
    passwordInput = new PasswordTextBox();

    loginButton = new Button("Submit");

    createLayoutWindowPanel();

    userNameInput.setFocus(true);
  }

  private void createLayoutWindowPanel() {
    windowPanel = new WindowPanel("Authentication required");
    Widget closeBtn = windowPanel.getHeader().getWidget(0, Caption.CaptionRegion.RIGHT);
    closeBtn.setVisible(false);
    windowPanel.setAnimationEnabled(false);
    LayoutPanel panel = new LayoutPanel();
    //panel.addStyleName("WSLogin");
    windowPanel.setWidget(panel);


    // create contents
    panel.setLayout(new BoxLayout(BoxLayout.Orientation.VERTICAL));
    Grid grid = new Grid(3, 2);
    grid.setWidget(0, 0, new Label("Username:"));
    grid.setWidget(0, 1, userNameInput);

    grid.setWidget(1, 0, new Label("Password:"));
    grid.setWidget(1, 1, passwordInput);

    grid.setWidget(2, 0, new HTML(""));
    grid.setWidget(2, 1, loginButton);

    /**
     * Create a handler so that striking enter automatically
     * submits the login.
     */
    KeyDownHandler clickOnEnter = new KeyDownHandler() {

      public void onKeyDown(KeyDownEvent event) {
        if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
          loginButton.click();
        }
      }
    };

    userNameInput.addKeyDownHandler(clickOnEnter);
    passwordInput.addKeyDownHandler(clickOnEnter);

    /**
     * Close the window immediately upon submission.
     */
    loginButton.addClickHandler(new ClickHandler() {

      public void onClick(ClickEvent event) {
        windowPanel.hide();
      }
    });

    panel.add(grid);

    windowPanel.getHeader().add(Caption.IMAGES.window().createImage());

    windowPanel.addCloseHandler(new CloseHandler<PopupPanel>() {
      public void onClose(CloseEvent<PopupPanel> event) {
        windowPanel = null;
      }
    });
  }


  public void showLoginPanel() {
    if (null == windowPanel)
      createLayoutWindowPanel();

    clearPanel();
    windowPanel.pack();
    windowPanel.center();
  }


  public void clearPanel() {
    userNameInput.setText("");
    passwordInput.setText("");
  }


  public void hideLoginPanel() {
    if (windowPanel != null)
      windowPanel.hide();
  }


  public HasText getUsernameInput() {
    return userNameInput;
  }


  public HasText getPasswordInput() {
    return passwordInput;
  }


  public HasClickHandlers getSubmitButton() {
    return loginButton;
  }


  public HasCloseHandlers getWindowPanel() {
    return windowPanel;
  }


  public void showWelcomeMessage(final String messageText) {
    Timer t = new Timer() {

      public void run() {
        MessageBox.info("Welcome", messageText);
      }
    };

    t.schedule(500);
  }
}
