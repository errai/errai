package org.jboss.errai.ui.client.widget;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.ComplexPanel;
import com.google.gwt.user.client.ui.Widget;

/**
* @author edewit@redhat.com
*/
public class OrderedListPanel extends ComplexPanel {
  public OrderedListPanel() {
    setElement(DOM.createElement("ul"));
  }

  @Override
  public void add(Widget w) {
    add(w, getElement());
  }
}
