package org.jboss.errai.ui.test.client;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.jboss.errai.ioc.client.api.EntryPoint;
import org.jboss.errai.ui.shared.Insert;

import com.google.gwt.resources.client.TextResource;
import com.google.gwt.user.client.ui.RootPanel;

@EntryPoint
public class App {

  @Insert
  private PageX page;

  @Inject
  private RootPanel root;

  private TextResource template;

  @PostConstruct
  public void setup() {
    page = new PageX();
    template = TemplateResource.INSTANCE.getTemplate();
  }
  
  public TextResource getTemplate() {
    return template;
  }
}
