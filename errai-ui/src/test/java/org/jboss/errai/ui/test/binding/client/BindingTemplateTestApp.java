package org.jboss.errai.ui.test.binding.client;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.jboss.errai.ioc.client.api.EntryPoint;
import org.jboss.errai.ui.test.binding.client.res.BindingListWidget;
import org.jboss.errai.ui.test.binding.client.res.BindingTemplate;

import com.google.gwt.user.client.ui.RootPanel;

@EntryPoint
public class BindingTemplateTestApp {

  @Inject
  private RootPanel root;

  @Inject
  private BindingTemplate template;
  
  @Inject
  private BindingListWidget listWidget;

  @PostConstruct
  public void setup() {
    root.add(template);
  }

  public BindingTemplate getTemplate() {
    return template;
  }
  
  public BindingListWidget getListWidget() {
    return listWidget;
  }
}
