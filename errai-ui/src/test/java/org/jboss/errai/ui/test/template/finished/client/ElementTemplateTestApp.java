package org.jboss.errai.ui.test.template.finished.client;

import com.google.gwt.user.client.ui.RootPanel;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import org.jboss.errai.ui.test.template.finished.client.res.ElementFormComponent;

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

  @PreDestroy
  public void preDe() {
    System.out.println("PREDESTROY");
  }
}
