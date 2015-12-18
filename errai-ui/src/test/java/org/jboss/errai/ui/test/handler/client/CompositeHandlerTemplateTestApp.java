package org.jboss.errai.ui.test.handler.client;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.ui.test.handler.client.res.CompositeHandledComponent;
import org.jboss.errai.ui.test.handler.client.res.HandledComponent;

import com.google.gwt.user.client.ui.RootPanel;

@Dependent
public class CompositeHandlerTemplateTestApp implements HandlerTemplateTestApp {

  @Inject
  private RootPanel root;

  @Inject
  private CompositeHandledComponent component;

  @PostConstruct
  public void setup() {
    root.add(component);
  }

  @PreDestroy
  public void tearDown() {
    root.clear();
  }

  @Override
  public HandledComponent getComponent() {
    return component;
  }
}
