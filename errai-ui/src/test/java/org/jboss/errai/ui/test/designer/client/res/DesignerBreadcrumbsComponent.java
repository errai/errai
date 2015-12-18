package org.jboss.errai.ui.test.designer.client.res;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.jboss.errai.ui.shared.api.annotations.DataField;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;

public abstract class DesignerBreadcrumbsComponent extends Composite implements DesignerComponent {

  @Inject
  @DataField
  private BasicComponent1 c1;

  @Inject
  @DataField("subTemplate")
  private DesignerSubComponent something;

  @Inject
  @DataField
  private Button c2;

  @PostConstruct
  public void init() {
    c1.getElement().setAttribute("id", "basic");
    something.getElement().setAttribute("id", "somethingNew");
    c2.getElement().setAttribute("id", "btn");
  }

  @Override
  public DesignerSubComponent getSubComponent() {
    return something;
  }

  @Override
  public Button getButton() {
    return c2;
  }
}
