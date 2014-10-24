package org.jboss.errai.ui.nav.client.local.testpages;

import com.google.gwt.user.client.ui.VerticalPanel;
import org.jboss.errai.ui.nav.client.local.Page;
import org.jboss.errai.ui.nav.client.local.PageShowing;

import javax.enterprise.context.ApplicationScoped;

/**
 * Simple test page which throws an exception in {@link PageShowing}.
 *
 * @author Johannes Barop <jb@barop.de>
 */
@ApplicationScoped
@Page
public class PageWithException extends VerticalPanel {

  @PageShowing
  protected void beforeShow() {
    throw new NullPointerException("");
  }

}
