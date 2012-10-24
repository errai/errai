package org.jboss.errai.ui.test.binding.client.res;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.ui.shared.api.annotations.AutoBound;
import org.jboss.errai.ui.shared.api.annotations.Bound;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.jboss.errai.ui.test.common.client.TestModel;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;

@Dependent
@Templated
public class BindingTemplate extends Composite {

  @Inject
  @Bound
  @DataField
  private Label id;

  @Inject
  @Bound(property="child.name")
  @DataField
  private TextBox name;

  @Inject
  @Bound(property = "lastChanged", converter = BindingDateConverter.class)
  @DataField
  private TextBox date;

  private final TestModel model;
  
  @Inject
  public BindingTemplate(@AutoBound DataBinder<TestModel> binder) {
    model = binder.getModel();
  }

  public Label getLabel() {
    return id;
  }

  public TextBox getNameTextBox() {
    return name;
  }

  public TextBox getDateTextBox() {
    return date;
  }
  
  public TestModel getModel() {
    return model;
  }
}