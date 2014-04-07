package org.jboss.errai.ui.test.template.finished.client.res;

import javax.inject.Inject;

import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.TextBox;

@Templated
public class ElementFormComponent extends Composite {

  @DataField
  private Element form = DOM.createForm();

  @Inject
  @DataField
  @AddClassNameAnnotation(classname = "testing-classname")
  private TextBox username;

  @Inject
  @DataField
  private PasswordTextBox password;

  public Element getForm() {
    return form;
  }

  public TextBox getUsername() {
    return username;
  }

  public PasswordTextBox getPassword() {
    return password;
  }
}
