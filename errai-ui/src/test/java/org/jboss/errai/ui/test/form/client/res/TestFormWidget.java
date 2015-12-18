package org.jboss.errai.ui.test.form.client.res;

import javax.enterprise.context.Dependent;

import org.jboss.errai.ui.client.widget.AbstractForm;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.FormElement;
import com.google.gwt.dom.client.InputElement;

@Templated
@Dependent
public class TestFormWidget extends AbstractForm {

  @DataField
  private FormElement form = Document.get().createFormElement();

  @DataField
  private InputElement username = Document.get().createTextInputElement();

  @DataField
  private InputElement password = Document.get().createPasswordInputElement();

  @Override
  public FormElement getFormElement() {
    return form;
  }
}
