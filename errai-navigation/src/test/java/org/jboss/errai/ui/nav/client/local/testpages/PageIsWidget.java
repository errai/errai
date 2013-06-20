package org.jboss.errai.ui.nav.client.local.testpages;

import org.jboss.errai.ui.nav.client.local.Page;

import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

import javax.enterprise.context.ApplicationScoped;

/**
 * Simple page that implements {@link IsWidget}
 *
 * @author Johannes Barop <jb@barop.de>
 */
@ApplicationScoped
@Page
public class PageIsWidget implements IsWidget {

  private final HTML widget = new HTML("<p>Hello World</p>");

  @Override
  public Widget asWidget() {
    return widget;
  }

}
