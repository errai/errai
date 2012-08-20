package org.jboss.errai.demo.grocery.client.local.nav;

import org.jboss.errai.ui.shared.api.annotations.Templated;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

/**
 * A subclassable GWT Composite widget which is also a {@link Page}. A subclass
 * of this class that is annotated with {@link Templated @Templated} is an
 * ErraiUI templated widget which is also a page that can be navigated to and
 * away from.
 *
 * @author Jonathan Fuerth <jfuerth@gmail.com>
 */
public class CompositePage extends Composite implements Page {

  /**
   * Returns the simple name of this class (override this method if its name
   * should be different from the class' simple name).
   */
  @Override
  public String name() {
    return getClass().getName().substring(getClass().getName().lastIndexOf('.') + 1);
  }

  /**
   * Returns this CompositePage (because CompositePage is both a Page
   * implementation and a Widget that provides the page's content).
   */
  @Override
  public Widget content() {
    return this;
  }

}
