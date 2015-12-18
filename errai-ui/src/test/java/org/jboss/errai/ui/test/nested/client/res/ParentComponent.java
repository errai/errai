package org.jboss.errai.ui.test.nested.client.res;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;

@Templated
public class ParentComponent extends Composite {

  @Inject
  @DataField
  private ChildComponent c1;

  private Button button;

  @PostConstruct
  public void init() {
    c1.getElement().setAttribute("id", "c1");
    button.getElement().setAttribute("id", "c2");
  }

  public Button getButton() {
    return button;
  }

  @Inject
  public void setButton(@DataField("c2") Button button) {
    this.button = button;
  }

}
