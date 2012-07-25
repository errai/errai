package org.jboss.errai.ui.test.binding.client.res;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.jboss.errai.ui.test.common.client.Model;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;

@Dependent
@Templated
public class BindingTemplate extends Composite {

  @Inject
  @DataField
  private Label id;

  @Inject
  @DataField
  private TextBox name;

  @Inject
  private DataBinder<Model> binder;

  public Label getLabel() {
    return id;
  }

  public TextBox getTextBox() {
    return name;
  }

  public Model getModel() {
    return binder.getModel();
  }
}