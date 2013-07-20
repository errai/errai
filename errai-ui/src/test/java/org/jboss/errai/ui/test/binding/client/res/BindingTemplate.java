package org.jboss.errai.ui.test.binding.client.res;

import javax.inject.Inject;

import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.ui.shared.api.annotations.AutoBound;
import org.jboss.errai.ui.shared.api.annotations.Bound;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.jboss.errai.ui.test.common.client.TestModel;

import com.google.gwt.dom.client.DivElement;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;

@Templated
public class BindingTemplate extends Composite {

  @Bound(property = "id")
  @DataField
  private final DivElement idDiv = DOM.createElement("div").cast();

  @Inject
  @Bound
  @DataField
  private Label id;

  @Inject
  @Bound(property = "child.name")
  @DataField
  private TextBox name;

  @Inject
  @Bound(property = "lastChanged", converter = BindingDateConverter.class)
  @DataField("dateField")
  private TextBox date;

  @Inject
  @Bound
  @DataField("phone")
  private TextBox phoneNumber;

  @Inject
  @Bound
  @DataField
  private BindingListWidget children;
  
  private final TestModel model;

  @Inject
  public BindingTemplate(@AutoBound DataBinder<TestModel> binder) {
    model = binder.getModel();
  }

  public DivElement getIdDiv() {
    return idDiv;
  }

  public Label getIdLabel() {
    return id;
  }

  public TextBox getNameTextBox() {
    return name;
  }

  public TextBox getDateTextBox() {
    return date;
  }

  public TextBox getPhoneNumberBox() {
    return phoneNumber;
  }
  
  public BindingListWidget getListWidget() {
    return children;
  }

  public TestModel getModel() {
    return model;
  }
}