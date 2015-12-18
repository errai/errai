package org.jboss.errai.processor.testcase;

import java.util.List;

import javax.inject.Inject;

import org.jboss.errai.ui.shared.api.annotations.Bound;
import org.jboss.errai.ui.shared.api.annotations.Model;

import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class BoundNotAWidget {
  @Inject @Model
  private BoundModelClass model;

  @Bound
  private final String property1 = "oops";

  @Inject @Bound
  private Object property2;

  private final List<Object> constructorInjectedWidget;
  private final Integer getterWidget = 7;
  private Exception setterWidget;

  @Inject
  public BoundNotAWidget(@Bound List<Object> property5) {
    this.constructorInjectedWidget = property5;
  }

  @Inject
  public void setProperty3(@Bound Exception boundWidget) {
    this.setterWidget = boundWidget;
  }

  @Bound
  public Integer getProperty4() {
    return getterWidget;
  }
}