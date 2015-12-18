package org.jboss.errai.ui.nav.client.local.testpages;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.jboss.errai.ui.nav.client.local.Page;
import org.jboss.errai.ui.nav.client.local.TransitionAnchor;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;

@ApplicationScoped
@Page
public class OtherPageWithTransitionAnchor extends Composite {

  @Inject
  private TransitionAnchor<PageA> aLink;
  
  public OtherPageWithTransitionAnchor() {
    initWidget(new FlowPanel());
  }
  
  public TransitionAnchor<PageA> getAnchor() {
    return aLink;
  }

}
