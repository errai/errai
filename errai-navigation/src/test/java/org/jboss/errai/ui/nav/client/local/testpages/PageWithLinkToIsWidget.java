package org.jboss.errai.ui.nav.client.local.testpages;

import javax.inject.Inject;

import org.jboss.errai.ui.nav.client.local.Page;
import org.jboss.errai.ui.nav.client.local.TransitionAnchor;
import org.jboss.errai.ui.nav.client.local.TransitionTo;

import com.google.gwt.user.client.ui.SimplePanel;

@Page
public class PageWithLinkToIsWidget extends SimplePanel {

  @Inject private TransitionTo<PageIsWidget> transitionToIsWidget;
  @Inject private TransitionAnchor<PageIsWidget> linkToIsWidget;

  public TransitionTo<PageIsWidget> getTransitionToIsWidget() {
    return transitionToIsWidget;
  }

  public TransitionAnchor<PageIsWidget> getLinkToIsWidget() {
    return linkToIsWidget;
  }
}
