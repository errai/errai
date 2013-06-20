package org.jboss.errai.ui.nav.client.local.testpages;

import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import org.jboss.errai.ui.nav.client.local.Page;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
@Page
public class PageC implements IsWidget {

  private HTML widget = new HTML("<p>Hello World</p>");

  @Override
  public Widget asWidget() {
    return widget;
  }

}
