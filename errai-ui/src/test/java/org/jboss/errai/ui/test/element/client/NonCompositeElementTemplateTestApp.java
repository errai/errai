package org.jboss.errai.ui.test.element.client;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.ui.test.element.client.res.ElementFormComponent;
import org.jboss.errai.ui.test.element.client.res.NonCompositeElementFormComponent;

import com.google.gwt.user.client.ui.RootPanel;

@Dependent
public class NonCompositeElementTemplateTestApp implements ElementTemplateTestApp {

  @Inject
  private RootPanel root;

  @Inject
  private NonCompositeElementFormComponent form;

  @PostConstruct
  public void setup() {
    root.getElement().appendChild(form.getElement());
  }

  @Override
  public ElementFormComponent getForm() {
    return form;
  }
}
