package org.jboss.errai.demo.grocery.client.local.nav;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * An interface whose implementation is usually generated at compile-time by scanning for page classes.
 *
 * @author Jonathan Fuerth <jfuerth@gmail.com>
 */
public interface NavigationGraph {

  public Page getPage(String name);

  // a page and all its outbound transitions. this type is probably only needed at rebind time (not on the client)
  static class PageNode {
    private final Page page;
    private final List<PageTransition<?>> transitions = new ArrayList<PageTransition<?>>();

    public PageNode(Page page) {
      super();
      this.page = page;
    }

    /**
     * Adds a transition from this page to another.
     *
     * @param toPage
     *          The target page of the transition.
     * @param reason
     *          The label that should be given to this transition; the reason the
     *          user is moving from the {@link #fromPage()} to the
     *          {@link #toPage()}. Not null.
     * @return This PageNode, so calls can be chained.
     */
    public PageNode addTransition(Page toPage, String reason) {
      throw new UnsupportedOperationException();
      //transitions.add(new PageTransition<Page>(page, toPage, reason));
//      return this;
    }

    /**
     * Returns the available transitions away from this page.
     *
     * @return Outbound transitions from this page. Never null, but may be empty. Do not attempt to modify the returned list.
     */
    public List<PageTransition<?>> pageTransitions() {
      return Collections.unmodifiableList(transitions);
    }
  }

}
