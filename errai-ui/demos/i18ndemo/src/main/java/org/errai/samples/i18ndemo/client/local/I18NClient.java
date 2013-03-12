package org.errai.samples.i18ndemo.client.local;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.jboss.errai.ioc.client.api.EntryPoint;

import com.google.gwt.user.client.ui.RootPanel;

@EntryPoint
public class I18NClient {

  @Inject
  private RootPanel root;
  @Inject
  protected TemplatedWidget tw;

  @PostConstruct
  public void post() {
    root.add(tw);
  }

}
