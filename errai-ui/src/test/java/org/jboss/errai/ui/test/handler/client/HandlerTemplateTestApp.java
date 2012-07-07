package org.jboss.errai.ui.test.handler.client;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.jboss.errai.ioc.client.api.EntryPoint;
import org.jboss.errai.ui.test.handler.client.res.HandledComponent;

import com.google.gwt.user.client.ui.RootPanel;

@EntryPoint
public class HandlerTemplateTestApp {

  @Inject
  private RootPanel root;

  @Inject
  private HandledComponent component;

  @PostConstruct
  public void setup() {
    root.add(component);
    System.out.println(root.getElement().getInnerHTML());
  }

  public HandledComponent getComponent() {
    return component;
  }
}
