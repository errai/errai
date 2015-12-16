package org.jboss.errai.ui.test.designer.client.res;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Button;

@Templated("DesignerTemplate.html#breadcrumbs")
public class NonCompositeDesignerBreadcrumbsComponent implements DesignerComponent {

  @DataField
  private Element breadcrumbs = DOM.createElement("h1");

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

  public Element getRoot() {
    return breadcrumbs;
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
