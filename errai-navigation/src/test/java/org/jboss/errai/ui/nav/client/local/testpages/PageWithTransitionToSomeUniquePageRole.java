package org.jboss.errai.ui.nav.client.local.testpages;

import javax.inject.Inject;

import org.jboss.errai.ui.nav.client.local.Page;
import org.jboss.errai.ui.nav.client.local.TransitionToRole;

import com.google.gwt.user.client.ui.SimplePanel;

@Page
public class PageWithTransitionToSomeUniquePageRole extends SimplePanel {
  
  @Inject
  public TransitionToRole<SomeUniquePageRole> transition;

}
