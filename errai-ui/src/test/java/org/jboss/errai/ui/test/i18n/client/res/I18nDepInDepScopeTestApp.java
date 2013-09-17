package org.jboss.errai.ui.test.i18n.client.res;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.ui.shared.api.annotations.Bundle;

import com.google.gwt.user.client.ui.RootPanel;

@Dependent
@Bundle("I18nAppScopeTest.json")
public class I18nDepInDepScopeTestApp {
  
  @Inject
  private RootPanel root;
  
  @Inject
  private DepScopedWidget depWidget;
  
  @PostConstruct
  private void setup() {
    root.add(depWidget);
  }
  
  public DepScopedWidget getWidget() {
    return depWidget;
  }
}
