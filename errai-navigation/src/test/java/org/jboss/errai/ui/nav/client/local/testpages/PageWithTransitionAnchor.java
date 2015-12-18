package org.jboss.errai.ui.nav.client.local.testpages;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.ui.nav.client.local.Page;
import org.jboss.errai.ui.nav.client.local.TransitionAnchor;
import org.jboss.errai.ui.nav.client.local.TransitionAnchorFactory;

import com.google.common.collect.HashMultimap;
import com.google.gwt.user.client.ui.FlowPanel;

@Dependent
@Page
public class PageWithTransitionAnchor extends FlowPanel {

  @Inject
  public TransitionAnchor<PageB> linkToB;

  @Inject
  public TransitionAnchorFactory<PageBWithState> linkFactory;

  /**
   * Constructor.
   */
  public PageWithTransitionAnchor() {
  }

  @PostConstruct
  protected void postCtor() {
    add(linkToB);
    add(linkFactory.get());
    add(linkFactory.get("uuid", "12345"));
    HashMultimap<String, String> state = HashMultimap.create();
    state.put("uuid", "54321");
    add(linkFactory.get(state));
  }

}
