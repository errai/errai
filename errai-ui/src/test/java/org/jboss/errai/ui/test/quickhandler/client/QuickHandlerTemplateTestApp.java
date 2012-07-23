package org.jboss.errai.ui.test.quickhandler.client;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.jboss.errai.ioc.client.api.EntryPoint;
import org.jboss.errai.ui.test.quickhandler.client.res.QuickHandlerComponent;

import com.google.gwt.user.client.ui.RootPanel;

@EntryPoint
public class QuickHandlerTemplateTestApp {

  @Inject
  private RootPanel root;

  @Inject
  private QuickHandlerComponent component;

  @PostConstruct
  public void setup() {
    root.add(component);
    System.out.println(root.getElement().getInnerHTML());
  }

  public QuickHandlerComponent getComponent() {
    return component;
  }
}
