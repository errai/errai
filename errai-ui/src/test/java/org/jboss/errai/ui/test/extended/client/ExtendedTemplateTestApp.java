package org.jboss.errai.ui.test.extended.client;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.jboss.errai.ioc.client.api.EntryPoint;
import org.jboss.errai.ui.test.extended.client.res.ExtensionComponent;

import com.google.gwt.user.client.ui.RootPanel;

@EntryPoint
public class ExtendedTemplateTestApp {

  @Inject
  private RootPanel root;

  @Inject
  private ExtensionComponent component;

  @PostConstruct
  public void setup() {
    System.out.println("Adding component to RootPanel");
    root.add(component);
  }

  public ExtensionComponent getComponent() {
    return component;
  }
}
