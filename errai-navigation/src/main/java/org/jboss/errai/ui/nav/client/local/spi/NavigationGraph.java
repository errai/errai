package org.jboss.errai.ui.nav.client.local.spi;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;

import org.jboss.errai.common.client.util.CreationalCallback;
import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.ioc.client.container.async.AsyncBeanManager;
import org.jboss.errai.ui.nav.client.local.PageRole;
import org.jboss.errai.ui.nav.client.local.TransitionTo;
import org.jboss.errai.ui.nav.client.local.UniquePageRole;
import org.jboss.errai.ui.nav.client.local.api.MissingPageRoleException;
import org.jboss.errai.ui.nav.client.local.api.PageNotFoundException;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.gwt.user.client.ui.IsWidget;

/**
 * The NavigationGraph is responsible for creating or retrieving instances of Page and PageTransition objects. It is
 * also the central repository for structural information about the interpage navigation in the app (this information is
 * defined in a decentralized way, by classes that implement {@link PageNode} and contain injected {@link TransitionTo}
 * fields.
 * <p>
 * The concrete implementation of this class is usually generated at compile-time by scanning for page classes. It is
 * expected to fill in the {@link #pagesByName} map in its constructor.
 * 
 * @author Jonathan Fuerth <jfuerth@gmail.com>
 */
public abstract class NavigationGraph {

  protected final AsyncBeanManager bm = IOC.getAsyncBeanManager();

  /**
   * Maps page names to the classes that implement them. The subclass's constructor is responsible for populating this
   * map.
   */
  protected final Map<String, PageNode<? extends IsWidget>> pagesByName = new HashMap<String, PageNode<? extends IsWidget>>();
  protected final Multimap<Class<? extends PageRole>, PageNode<? extends IsWidget>> pagesByRole = ArrayListMultimap
          .create();

  /**
   * Returns an instance of the given page type. If the page is an ApplicationScoped bean, the singleton instance of the
   * page will be returned; otherwise (for Dependent-scoped beans) a new instance will be returned.
   * 
   * @param name
   *          The page name, as defined by the implementation of page.
   * @return The appropriate instance of the page.
   */
  public <W extends IsWidget> PageNode<W> getPage(String name) {
    @SuppressWarnings("unchecked")
    PageNode<W> page = (PageNode<W>) pagesByName.get(name);
    if (page == null) {
      throw new PageNotFoundException("Page not found: \"" + name + "\"");
    }
    return page;
  }

  /**
   * Returns an instance of the given page type. If the page is an ApplicationScoped bean, the singleton instance of the
   * page will be returned; otherwise (for Dependent-scoped beans) a new instance will be returned.
   * 
   * @param type
   *          The Class object for the bean that implements the page.
   * @return The appropriate instance of the page.
   */
  public <W extends IsWidget> PageNode<W> getPage(Class<W> type) {
    // TODO this could be made more efficient if we had a pagesByWidgetType map
    for (Entry<String, PageNode<? extends IsWidget>> e : pagesByName.entrySet()) {
      if (e.getValue().contentType().equals(type)) {
        @SuppressWarnings({ "unchecked" })
        PageNode<W> page = (PageNode<W>) e.getValue();
        return page;
      }
    }
    throw new PageNotFoundException("No page with a widget type of " + type.getName() + " exists");
  }

  /**
   * Returns all pages that have the specified role. In the add page annotation one can specify multiple roles for a
   * page. {@link #getPage(Class)} {@link PageRole}
   * 
   * @param role
   *          the role used to lookup the pages
   * @return all pages that have the role set.
   */
  public Collection<PageNode<? extends IsWidget>> getPagesByRole(Class<? extends PageRole> role) {
    return pagesByRole.get(role);
  }

  public PageNode getPageByRole(Class<? extends UniquePageRole> role) {
    final Collection<PageNode<? extends IsWidget>> pageNodes = pagesByRole.get(role);
    if (pageNodes.size() == 1) {
      return pageNodes.iterator().next();
    }
    else if (pageNodes.size() < 1) {
      throw new MissingPageRoleException(role);
    }
    else {
      throw new IllegalStateException("Role '" + role + "' is not unique multiple pages: " + pageNodes + " found");
    }
  }

  /**
   * Returns true if and only if there are no pages in this nagivation graph.
   */
  public boolean isEmpty() {
    return pagesByName.isEmpty();
  }

  protected static final class PageNodeCreationalCallback<W extends IsWidget> implements
          CreationalCallback<PageNode<W>> {

    @Override
    public void callback(PageNode<W> beanInstance) {

    }

  }
  
  /**
   * @return Returns a collection of all {@link PageNode PageNodes} in the navigation graph.
   */
  public Collection<PageNode<? extends IsWidget>> getAllPages() {
    Collection<PageNode<? extends IsWidget>> values = pagesByName.values();
    return Collections.unmodifiableCollection(new HashSet<PageNode<? extends IsWidget>>(values));
  }
  
}
