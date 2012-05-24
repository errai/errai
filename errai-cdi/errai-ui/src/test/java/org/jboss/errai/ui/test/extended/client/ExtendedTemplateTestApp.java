package org.jboss.errai.ui.test.extended.client;

import javax.inject.Inject;

import org.jboss.errai.ioc.client.api.AfterInitialization;
import org.jboss.errai.ioc.client.api.EntryPoint;
import org.jboss.errai.ui.test.extended.client.res.ExtensionComponent;

import com.google.gwt.user.client.ui.RootPanel;

@EntryPoint
public class ExtendedTemplateTestApp {

  @Inject
  private RootPanel root;

  @Inject
  private ExtensionComponent component;

  @AfterInitialization
  public void setup() {
    root.add(component);
  }

  public ExtensionComponent getComponent() {
    return component;
  }
}
