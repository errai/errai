package org.jboss.errai.ui.test.quickhandler.client;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.ui.test.quickhandler.client.res.CompositeQuickHandlerComponent;
import org.jboss.errai.ui.test.quickhandler.client.res.QuickHandlerComponent;

import com.google.gwt.user.client.ui.RootPanel;

@Dependent
public class CompositeQuickHandlerTemplateTestApp implements QuickHandlerTemplateTestApp {

  @Inject
  private RootPanel root;

  @Inject
  private CompositeQuickHandlerComponent component;

  @PostConstruct
  public void setup() {
    root.add(component);
  }

  @PreDestroy
  public void tearDown() {
    root.clear();
  }

  @Override
  public QuickHandlerComponent getComponent() {
    return component;
  }
}
