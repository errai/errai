package org.jboss.errai.ui.test.designer.client.res;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;

@Dependent
@Templated("DesignerTemplate.html#breadcrumbs")
public class DesignerBreadcrumbsComponent extends Composite {

  @Inject
  @DataField
  private BasicComponent c1;

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

  public DesignerSubComponent getSubComponent() {
    return something;
  }

  public Button getButton() {
    return c2;
  }
}
