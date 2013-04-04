package org.jboss.errai.ui.test.i18n.client;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.jboss.errai.ioc.client.api.EntryPoint;
import org.jboss.errai.ui.shared.api.annotations.Bundle;
import org.jboss.errai.ui.test.i18n.client.res.I18nComponent;

import com.google.gwt.user.client.ui.RootPanel;

@EntryPoint
@Bundle("I18nTemplateTest.json")
public class I18nTemplateTestApp {

  @Inject
  private RootPanel root;

  @Inject
  private I18nComponent component;

  @PostConstruct
  public void setup() {
    root.add(component);
  }

  public I18nComponent getComponent() {
    return component;
  }
}
