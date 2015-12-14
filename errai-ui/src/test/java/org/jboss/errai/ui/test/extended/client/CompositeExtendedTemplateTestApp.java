package org.jboss.errai.ui.test.extended.client;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.jboss.errai.ioc.client.api.EntryPoint;
import org.jboss.errai.ui.test.extended.client.res.CompositeSecondLevelExtensionComponent;
import org.jboss.errai.ui.test.extended.client.res.Extension;

import com.google.gwt.user.client.ui.RootPanel;

@EntryPoint
public class CompositeExtendedTemplateTestApp implements ElementTemplateTestApp {

  @Inject
  private RootPanel root;

  @Inject
  private CompositeSecondLevelExtensionComponent extComponent;

  @Inject
  private CompositeSecondLevelExtensionComponent secondExtComponent;

  @PostConstruct
  public void setup() {
    System.out.println("Adding component to RootPanel");
    root.add(extComponent);
  }

  @Override
  public Extension getExtComponent() {
    return extComponent;
  }

  @Override
  public CompositeSecondLevelExtensionComponent getSecondExtComponent() {
    return secondExtComponent;
  }
}
