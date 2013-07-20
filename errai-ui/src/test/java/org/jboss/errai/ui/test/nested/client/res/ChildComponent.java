package org.jboss.errai.ui.test.nested.client.res;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;

@Templated
public class ChildComponent extends Composite {

  @Inject
  @DataField
  private Label c1;

  @Inject
  @DataField("c2")
  private Button content2;

  @PostConstruct
  public void init() {
    c1.getElement().setAttribute("id", "c1a");
    c1.setText("Added by component");
    content2.getElement().setAttribute("id", "c1b");
  }

  public Label getC1() {
    return c1;
  }

  public Button getC2() {
    return content2;
  }

}
