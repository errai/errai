package org.jboss.errai.ui.nav.client.local;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.jboss.errai.ioc.client.api.EntryPoint;
import org.jboss.errai.ui.nav.client.local.testpages.PageWithTransitionAnchor;

import com.google.gwt.user.client.ui.RootPanel;

/**
 * Entry point for the transition anchor test.  Needed so that the transition
 * anchor gets attached and thus its href is set.
 *
 * @author eric.wittmann@redhat.com
 */
@EntryPoint
public class TransitionAnchorTestApp {

  @Inject
  private RootPanel root;

  @Inject
  private PageWithTransitionAnchor page;

  @PostConstruct
  public void setup() {
    root.add(page);
  }

  public PageWithTransitionAnchor getPage() {
    return page;
  }

}
