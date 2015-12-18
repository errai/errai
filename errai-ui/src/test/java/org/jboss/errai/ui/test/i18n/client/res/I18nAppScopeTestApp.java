package org.jboss.errai.ui.test.i18n.client.res;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.jboss.errai.ui.shared.api.annotations.Bundle;

import com.google.gwt.user.client.ui.RootPanel;

@Bundle("I18nAppScopeTest.json")
@ApplicationScoped
public class I18nAppScopeTestApp {

  @Inject
  private RootPanel root;
  
  @Inject
  private DepScopedWidget asWidget;
  
  @PostConstruct
  private void setup() {
    root.add(asWidget);
  }
  
  public DepScopedWidget getWidget() {
    return asWidget;
  }
  
}
