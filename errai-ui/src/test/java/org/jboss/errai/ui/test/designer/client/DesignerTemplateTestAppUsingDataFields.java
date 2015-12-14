package org.jboss.errai.ui.test.designer.client;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.ui.test.designer.client.res.DesignerBreadcrumbsComponentUsingDataFields;
import org.jboss.errai.ui.test.designer.client.res.DesignerComponent;

import com.google.gwt.user.client.ui.RootPanel;

@Dependent
public class DesignerTemplateTestAppUsingDataFields implements DesignerTemplateTestApp {

  @Inject
  private RootPanel root;

  @Inject
  private DesignerBreadcrumbsComponentUsingDataFields component;

  @PostConstruct
  public void setup() {
    root.add(component);
  }

  @PreDestroy
  public void tearDown() {
    root.clear();
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
