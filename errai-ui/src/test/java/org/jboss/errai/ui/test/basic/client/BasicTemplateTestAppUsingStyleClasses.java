package org.jboss.errai.ui.test.basic.client;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.jboss.errai.ioc.client.api.EntryPoint;
import org.jboss.errai.ui.test.basic.client.res.BasicComponent;
import org.jboss.errai.ui.test.basic.client.res.BasicComponentUsingStyleClasses;

import com.google.gwt.user.client.ui.RootPanel;

@EntryPoint
public class BasicTemplateTestAppUsingStyleClasses implements BasicTemplateTestApp {

  @Inject
  private RootPanel root;

  @Inject
  private BasicComponentUsingStyleClasses component;

  @PostConstruct
  public void setup() {
    root.add(component);
    System.out.println(root.getElement().getInnerHTML());
  }

  @Override
  public BasicComponent getComponent() {
    return component;
  }
}
