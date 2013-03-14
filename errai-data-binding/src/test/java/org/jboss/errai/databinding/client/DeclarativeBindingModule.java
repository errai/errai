package org.jboss.errai.databinding.client;

import java.util.Date;

import javax.inject.Inject;

import org.jboss.errai.databinding.client.api.Converter;
import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.ioc.client.api.EntryPoint;
import org.jboss.errai.ui.shared.api.annotations.AutoBound;
import org.jboss.errai.ui.shared.api.annotations.Bound;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;

@EntryPoint
public class DeclarativeBindingModule extends DeclarativeBindingSuperType {
  public static final Date TEST_DATE = DateTimeFormat.getFormat("yyyy/MM/dd").parse("1980/22/06");

  @Bound
  private final Label id = new Label("id");

  @Bound(property="child.name")
  private final TextBox name = new TextBox();

  //tests automatic initialization
  @Bound  
  private TextBox age;
  
  private final TestModel model;
  
  @Inject
  public DeclarativeBindingModule(@AutoBound DataBinder<TestModel> binder) {
    model = binder.getModel();
  }

  public Label getLabel() {
    return id;
  }

  public TextBox getNameTextBox() {
    return name;
  }
  
  public TextBox getAge() {
    return age;
  }
  
  public TestModel getModel() {
    return model;
  }
  
  public static class BindingDateConverter implements Converter<Date, String> {

    @Override
    public Date toModelValue(String widgetValue) {
      return TEST_DATE;
    }

    @Override
    public String toWidgetValue(Date modelValue) {
      return "testdate";
    }
  }
}