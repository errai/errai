package org.jboss.errai.ui.nav.test.client.local.testpages;

import javax.enterprise.context.ApplicationScoped;

import org.jboss.errai.ui.nav.client.local.Page;

import com.google.gwt.user.client.ui.SimplePanel;

@ApplicationScoped
@Page(path="{stringThing}/{intThing}")
public class PageWithExtraState extends SimplePanel {

  // TODO encapsulate
  public String stringThing;

  // TODO encapsulate and make it an int
  public String intThing;

}
