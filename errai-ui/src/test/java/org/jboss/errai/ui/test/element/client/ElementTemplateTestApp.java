package org.jboss.errai.ui.test.element.client;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.jboss.errai.ioc.client.api.EntryPoint;
import org.jboss.errai.ui.test.element.client.res.ElementFormComponent;

import com.google.gwt.user.client.ui.RootPanel;

@EntryPoint
public class ElementTemplateTestApp {

  @Inject
  private RootPanel root;

  @Inject
  private ElementFormComponent form;

  @PostConstruct
  public void setup() {
    root.add(form);
    System.out.println(root.getElement().getInnerHTML());
  }

  public ElementFormComponent getForm() {
    return form;
  }
}
