package org.jboss.errai.ui.test.designer.client;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.ui.test.designer.client.res.DesignerComponent;
import org.jboss.errai.ui.test.designer.client.res.NonCompositeDesignerBreadcrumbsComponent;

import com.google.gwt.user.client.ui.RootPanel;

@Dependent
public class DesignerTemplateTestAppUsingNonCompositeComponent implements DesignerTemplateTestApp {

  @Inject
  private RootPanel root;

  @Inject
  private NonCompositeDesignerBreadcrumbsComponent component;

  @PostConstruct
  public void setup() {
    root.getElement().appendChild(component.getRoot());
  }

  @PreDestroy
  public void tearDown() {
    root.getElement().removeAllChildren();
  }

  @Override
  public DesignerComponent getComponent() {
    return component;
  }

  @Override
  public RootPanel getRoot() {
    return root;
  }
}
