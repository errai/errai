package org.jboss.errai.ui.nav.client.local.testpages;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.ui.nav.client.local.Page;
import org.jboss.errai.ui.nav.client.local.TransitionAnchor;

import com.google.gwt.user.client.ui.SimplePanel;

@Dependent
@Page
public class PageWithTransitionAnchor extends SimplePanel {

  @Inject
  public TransitionAnchor<PageB> linkToB;

  /**
   * Constructor.
   */
  public PageWithTransitionAnchor() {
  }

  @PostConstruct
  protected void postCtor() {
    add(linkToB);
  }

}
