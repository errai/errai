package org.jboss.errai.ui.test.nestedcyclic.client;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.jboss.errai.ioc.client.api.EntryPoint;
import org.jboss.errai.ui.test.nestedcyclic.client.res.ParentComponent;

import com.google.gwt.user.client.ui.RootPanel;

@EntryPoint
public class NestedTemplateTestApp {

  @Inject
  private RootPanel root;

  @Inject
  private ParentComponent component;

  @PostConstruct
  public void setup() {
    root.add(component);
  }

  public ParentComponent getComponent() {
    return component;
  }
}
