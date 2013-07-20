package org.jboss.errai.ui.test.designer.client.res;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;

@Templated
public class BasicComponent1 extends Composite {

  @Inject
  @DataField
  private Label c1;

  @Inject
  @DataField
  private Button c2;

  @PostConstruct
  public void init() {
    c1.getElement().setAttribute("id", "lbl");
    c1.setText("Added by component");
    c2.getElement().setAttribute("id", "btn");
  }

  public Label getC1() {
    return c1;
  }

  public Button getC2() {
    return c2;
  }

}
