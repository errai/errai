package org.jboss.errai.ui.test.extended.client;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.jboss.errai.ioc.client.api.EntryPoint;
import org.jboss.errai.ui.test.extended.client.res.ExtensionComponent;
import org.jboss.errai.ui.test.extended.client.res.SecondLevelExtensionComponent;
import com.google.gwt.user.client.ui.RootPanel;

@EntryPoint
public class ExtendedTemplateTestApp {

  @Inject
  private RootPanel root;

  @Inject
  private SecondLevelExtensionComponent extComponent;

  @Inject
  private SecondLevelExtensionComponent secondExtComponent;

  @PostConstruct
  public void setup() {
    System.out.println("Adding component to RootPanel");
    root.add(extComponent);
  }

  public ExtensionComponent getExtComponent() {
    return extComponent;
  }

  public SecondLevelExtensionComponent getSecondExtComponent() {
    return secondExtComponent;
  }
}
