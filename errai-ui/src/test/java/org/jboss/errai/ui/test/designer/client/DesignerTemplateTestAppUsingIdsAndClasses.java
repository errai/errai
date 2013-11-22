package org.jboss.errai.ui.test.designer.client;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.jboss.errai.ioc.client.api.EntryPoint;
import org.jboss.errai.ui.test.designer.client.res.DesignerBreadcrumbsComponentUsingId;

import com.google.gwt.user.client.ui.RootPanel;

@EntryPoint
public class DesignerTemplateTestAppUsingIdsAndClasses {

  @Inject
  private RootPanel root;

  @Inject
  private DesignerBreadcrumbsComponentUsingId component;

  @PostConstruct
  public void setup() {
    root.add(component);
  }

  public DesignerBreadcrumbsComponentUsingId getComponent() {
    return component;
  }

  public RootPanel getRoot() {
    return root;
  }
}
