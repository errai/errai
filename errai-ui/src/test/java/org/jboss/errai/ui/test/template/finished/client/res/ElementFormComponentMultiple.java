package org.jboss.errai.ui.test.template.finished.client.res;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.TextBox;
import javax.inject.Inject;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

/**
 * ClassDescription for ElementFormComponentMultiple
 * 
 * @author Dennis Schumann <dennis.schumann@devbliss.com>
 */
@Templated(value = "ElementFormComponent.html")
public class ElementFormComponentMultiple extends Composite {

  @DataField
  private Element form = DOM.createForm();

  @Inject
  @DataField
  @AddClassNameAnnotation(classname = "testing-classname")
  @PermissionAnnotation("CreateUser")
  private TextBox username;

  @Inject
  @DataField
  private PasswordTextBox password;

  @Inject
  @DataField("remember")
  private CheckBox rememberMe;

  @Inject
  @DataField
  private Button submit;

  public Element getForm() {
    return form;
  }

  public TextBox getUsername() {
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
}
