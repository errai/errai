package org.jboss.errai.ui.test.client.demo.component;

import javax.annotation.PostConstruct;

import org.jboss.errai.ui.shared.api.annotations.Replace;

import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;

public class HeaderFragment extends Composite {

  @Replace
  private Anchor homeLink;

  @PostConstruct
  public void init() {
    homeLink.setTarget("/home");
  }
}
