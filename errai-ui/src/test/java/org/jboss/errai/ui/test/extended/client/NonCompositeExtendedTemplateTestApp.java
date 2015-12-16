package org.jboss.errai.ui.test.extended.client;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.jboss.errai.ioc.client.api.EntryPoint;
import org.jboss.errai.ui.test.extended.client.res.Extension;
import org.jboss.errai.ui.test.extended.client.res.NonCompositeSecondLevelExtensionComponent;

import com.google.gwt.user.client.ui.RootPanel;

@EntryPoint
public class NonCompositeExtendedTemplateTestApp implements ElementTemplateTestApp {

  @Inject
  private RootPanel root;

  @Inject
  private NonCompositeSecondLevelExtensionComponent extComponent;

  @Inject
  private NonCompositeSecondLevelExtensionComponent secondExtComponent;

  @PostConstruct
  public void setup() {
    root.getElement().appendChild(extComponent.getElement());
  }

  @Override
  public Extension getExtComponent() {
    return extComponent;
  }

  @Override
  public Extension getSecondExtComponent() {
    return secondExtComponent;
  }
}
