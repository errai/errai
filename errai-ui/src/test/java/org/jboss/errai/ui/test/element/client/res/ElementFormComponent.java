package org.jboss.errai.ui.test.element.client.res;

import javax.inject.Inject;

import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.jboss.errai.ui.test.common.client.dom.Document;

import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.PasswordTextBox;

import elemental.client.Browser;
import elemental.html.ButtonElement;

@Templated
public class ElementFormComponent extends Composite {

  private int numberOfTimesPressed = 0;

  @DataField
  private Element form = DOM.createForm();

  @DataField
  private org.jboss.errai.ui.test.common.client.dom.Element username = Document.getDocument().createElement("input");

  @Inject
  @DataField
  private PasswordTextBox password;

  @Inject
  @DataField("remember")
  private CheckBox rememberMe;

  @Inject
  @DataField
  private Button submit;

  @DataField
  private ButtonElement cancel = Browser.getDocument().createButtonElement();

  public Element getForm() {
    return form;
  }

  public org.jboss.errai.ui.test.common.client.dom.Element getUsername() {
    return username;
  }

  public PasswordTextBox getPassword() {
    return password;
  }

  public CheckBox getRememberMe() {
    return rememberMe;
  }

  public Button getSubmit() {
    return submit;
  }

  public ButtonElement getCancel() {
    return cancel;
  }

  @EventHandler("cancel")
  private void onClick(ClickEvent event) {
    numberOfTimesPressed++;
    /*
     * DO NOT REMOVE
     * HTMLUnit crashes when firing a click event without this.
     */
    event.preventDefault();
  }

  public int getNumberOfTimesPressed() {
    return numberOfTimesPressed;
  }
}
