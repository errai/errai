package org.jboss.errai.ui.test.designer.client.res;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;

import org.jboss.errai.ui.shared.api.annotations.Replace;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;

@Dependent
@Templated("DesignerTemplate.html#designerContent")
public class DesignerSubComponent extends Composite {

  @Replace
  private Label h2;

  @PostConstruct
  public void init() {
    h2.getElement().setAttribute("id", "h2");
  }

  public Label getContent2() {
    return h2;
  }

}
