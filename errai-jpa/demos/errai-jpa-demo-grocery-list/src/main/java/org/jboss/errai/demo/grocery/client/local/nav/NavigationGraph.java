package org.jboss.errai.demo.grocery.client.local.nav;

/**
 * The NavigationGraph is responsible for creating or retrieving instances of
 * Page and PageTransition objects. It is also the central repository for
 * structural information about the interpage navigation in the app (this
 * information is defined in a decentralized way, by classes that implement
 * {@link Page} and contain injected {@link PageTransition} fields.
 * <p>
 * The implementation of this interface is usually generated at compile-time by
 * scanning for page classes.
 *
 * @author Jonathan Fuerth <jfuerth@gmail.com>
 */
public interface NavigationGraph {

  /**
   * Returns an instance of the given page type. If the page is an
   * ApplicationScoped bean, the singleton instance of the page will be
   * returned; otherwise (for Dependent-scoped beans) a new instance will be
   * returned.
   *
   * @param name The page name, as defined by the implementation of page.
   * @return The appropriate instance of the page.
   */
  public Page getPage(String name);

  // XXX not sure I want this
  Page getPage(Class<? extends Page> type);

}
