package org.jboss.errai.ui.test.binding.client.res;

import static org.jboss.errai.ui.test.common.client.dom.Document.getDocument;

import javax.inject.Inject;

import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.ui.shared.api.annotations.AutoBound;
import org.jboss.errai.ui.shared.api.annotations.Bound;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.jboss.errai.ui.test.common.client.TestModel;
import org.jboss.errai.ui.test.common.client.dom.Element;
import org.jboss.errai.ui.test.common.client.dom.TextInputElement;

import com.google.gwt.dom.client.DivElement;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;

@Templated("BindingTemplate.html")
public class CompositeBindingTemplate extends Composite implements BindingTemplate<BindingItemWidget> {

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

  @Bound(property = "title")
  @DataField
  private Element titleField = getDocument().createElement("div");

  @Bound
  @DataField
  private TextInputElement age = getDocument().createTextInputElement();

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
  private BindingListWidget<BindingItemWidget> children;

  private final TestModel model;

  @Inject
  public CompositeBindingTemplate(@AutoBound DataBinder<TestModel> binder) {
    model = binder.getModel();
  }

  @Override
  public Element getRoot() {
    return (Element) getElement();
  }

  @Override
  public DivElement getIdDiv() {
    return idDiv;
  }

  @Override
  public Label getIdLabel() {
    return id;
  }

  @Override
  public TextBox getNameTextBox() {
    return name;
  }

  @Override
  public TextBox getDateTextBox() {
    return date;
  }

  @Override
  public TextBox getPhoneNumberBox() {
    return phoneNumber;
  }

  @Override
  public Element getTitleField() {
    return titleField;
  }

  @Override
  public TextInputElement getAge() {
    return age;
  }

  @Override
  public BindingListWidget<BindingItemWidget> getListWidget() {
    return children;
  }

  @Override
  public TestModel getModel() {
    return model;
  }
}