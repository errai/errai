package org.jboss.errai.ui.test.designer.client;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.jboss.errai.ioc.client.api.EntryPoint;
import org.jboss.errai.ui.test.designer.client.res.DesignerBreadcrumbsComponent;

import com.google.gwt.user.client.ui.RootPanel;

@EntryPoint
public class DesignerTemplateTestApp {

  @Inject
  private RootPanel root;

  @Inject
  private DesignerBreadcrumbsComponent component;

  @PostConstruct
  public void setup() {
    root.add(component);
  }

  public DesignerBreadcrumbsComponent getComponent() {
    return component;
  }

  public RootPanel getRoot() {
    return root;
  }
}
