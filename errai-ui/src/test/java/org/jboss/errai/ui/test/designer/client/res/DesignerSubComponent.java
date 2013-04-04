package org.jboss.errai.ui.test.designer.client.res;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;

import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;

@Dependent
@Templated("DesignerTemplate.html#subTemplate")
public class DesignerSubComponent extends Composite {

  @DataField
  private Label h2 = new Label();

  @PostConstruct
  public void init() {
    h2.getElement().setAttribute("id", "h2");
  }

  public Label getContent2() {
    return h2;
  }

}
