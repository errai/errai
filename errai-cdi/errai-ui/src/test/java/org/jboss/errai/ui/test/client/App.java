package org.jboss.errai.ui.test.client;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.jboss.errai.ioc.client.api.EntryPoint;

import com.google.gwt.user.client.ui.RootPanel;

@EntryPoint
public class App {

  @Inject
  private RootPanel root;

  @Inject
  private TemplateComponent component;

  @PostConstruct
  public void setup() {
    root.add(component);
    System.out.println(root.getElement().getInnerHTML());
  }
  
  public TemplateComponent getComponent() {
    return component;
  }
}
