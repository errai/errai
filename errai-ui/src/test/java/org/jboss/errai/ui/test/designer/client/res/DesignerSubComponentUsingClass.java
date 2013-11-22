package org.jboss.errai.ui.test.designer.client.res;

import javax.annotation.PostConstruct;

import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;

@Templated("DesignerTemplateUsingIdsAndClasses.html#subTemplate")
public class DesignerSubComponentUsingClass extends Composite {

  @DataField
  private final Label h2 = new Label();

  @PostConstruct
  public void init() {
    h2.getElement().setAttribute("id", "h2");
  }

  public Label getContent2() {
    return h2;
  }

}
