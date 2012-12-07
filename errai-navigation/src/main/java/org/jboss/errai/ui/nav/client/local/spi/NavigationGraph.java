package org.jboss.errai.ui.nav.client.local.spi;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.ioc.client.container.IOCBeanManager;
import org.jboss.errai.ui.nav.client.local.TransitionTo;

import com.google.gwt.user.client.ui.Widget;

/**
 * The NavigationGraph is responsible for creating or retrieving instances of
 * Page and PageTransition objects. It is also the central repository for
 * structural information about the interpage navigation in the app (this
 * information is defined in a decentralized way, by classes that implement
 * {@link PageNode} and contain injected {@link TransitionTo} fields.
 * <p>
 * The concrete implementation of this class is usually generated at
 * compile-time by scanning for page classes. It is expected to fill in the
 * {@link #pagesByName} map in its constructor.
 *
 * @author Jonathan Fuerth <jfuerth@gmail.com>
 */
public abstract class NavigationGraph {

  protected final IOCBeanManager bm = IOC.getBeanManager();

  /**
   * Maps page names to the classes that implement them. The subclass's
   * constructor is responsible for populating this map.
   */
  protected final Map<String, PageNode<? extends Widget>> pagesByName = new HashMap<String, PageNode<? extends Widget>>();

  /**
   * Returns an instance of the given page type. If the page is an
   * ApplicationScoped bean, the singleton instance of the page will be
   * returned; otherwise (for Dependent-scoped beans) a new instance will be
   * returned.
   *
   * @param name The page name, as defined by the implementation of page.
   * @return The appropriate instance of the page.
   */
  public <W extends Widget> PageNode<W> getPage(String name) {
    @SuppressWarnings("unchecked")
    PageNode<W> page = (PageNode<W>) pagesByName.get(name);
    if (page == null) {
      throw new IllegalArgumentException("Page not found: \"" + name + "\"");
    }
    return page;
  }

  /**
   * Returns an instance of the given page type. If the page is an
   * ApplicationScoped bean, the singleton instance of the page will be
   * returned; otherwise (for Dependent-scoped beans) a new instance will be
   * returned.
   *
   * @param type The Class object for the bean that implements the page.
   * @return The appropriate instance of the page.
   */
  public <W extends Widget> PageNode<W> getPage(Class<W> type) {
    // TODO this could be made more efficient if we had a pagesByWidgetType map
    for (Entry<String, PageNode<? extends Widget>> e : pagesByName.entrySet()) {
      if (e.getValue().contentType().equals(type)) {
        @SuppressWarnings("unchecked")
        PageNode<W> page = (PageNode<W>) e.getValue();
        return page;
      }
    }
    throw new IllegalArgumentException("No page with a widget type of " + type.getName() + " exists");
  }

  /**
   * Returns true if and only if there are no pages in this nagivation graph.
   */
  public boolean isEmpty() {
    return pagesByName.isEmpty();
  }
}
