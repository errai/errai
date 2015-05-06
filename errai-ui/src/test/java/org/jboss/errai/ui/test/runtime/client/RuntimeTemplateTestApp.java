package org.jboss.errai.ui.test.runtime.client;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.ui.shared.TemplateInitializedEvent;
import org.jboss.errai.ui.test.runtime.client.res.RuntimeParentComponent;

import com.google.gwt.user.client.ui.RootPanel;

@Dependent
public class RuntimeTemplateTestApp {

  @Inject
  private RootPanel root;

  @Inject
  private RuntimeParentComponent component;

  @PostConstruct
  public void init() {
    TemplateInitializedEvent.Handler handler = new TemplateInitializedEvent.Handler() {
      @Override
      public void onInitialized() {
        root.add(component); 
      }
    };
    component.addHandler(handler, TemplateInitializedEvent.TYPE);
  }
  
  public RootPanel getRoot() {
    return root;
  }
  
  public RuntimeParentComponent getComponent() {
    return component;
  }
}
