/*
 * Copyright (C) 2013 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.demo.mobile.client.local;

import org.jboss.errai.common.client.api.Assert;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class WelcomeDialog extends Composite {

  private static WelcomeDialogUiBinder uiBinder = GWT
        .create(WelcomeDialogUiBinder.class);

  interface WelcomeDialogUiBinder extends UiBinder<Widget, WelcomeDialog> {}

  @UiField
  TextBox nameBox;

  @UiField
  Button goButton;

  private final Runnable afterNameGivenAction;

  private String name = "Anonymous";

  public WelcomeDialog(Runnable afterNameGivenAction) {
    this.afterNameGivenAction = Assert.notNull(afterNameGivenAction);
    initWidget(uiBinder.createAndBindUi(this));
  }

  /**
   * Aliases typing Enter in the name box to the same as pressing the "Go" button.
   * 
   * @param event
   *          The key event. The value {@code event.getNativeKeyCode()} is compared against
   *          {@code KeyCodes.KEY_ENTER}.
   */
  @UiHandler("nameBox")
  void onNameBoxKeypress(KeyUpEvent event) {
    if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
      onGoButtonClick(null);
    }
  }

  /**
   * Runs the {@code afterNameGivenAction}.
   * 
   * @param event
   *          Ignored. Can be null.
   */
  @UiHandler("goButton")
  void onGoButtonClick(ClickEvent event) {
    afterNameGivenAction.run();
    name = nameBox.getText();
  }

  /**
   * Returns the text that is currently entered in the name textbox.
   */
  public String getName() {
    return name;
  }
}
