package org.jboss.errai.ui.test.handler.client;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.ui.shared.TemplateWidgetMapper;
import org.jboss.errai.ui.test.handler.client.res.HandledComponent;
import org.jboss.errai.ui.test.handler.client.res.NonCompositeHandledComponent;

import com.google.gwt.user.client.ui.RootPanel;

@Dependent
public class NonCompositeHandlerTemplateTestApp implements HandlerTemplateTestApp {

  @Inject
  private RootPanel root;

  @Inject
  private NonCompositeHandledComponent component;

  @PostConstruct
  public void setup() {
    root.add(TemplateWidgetMapper.get(component));
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
