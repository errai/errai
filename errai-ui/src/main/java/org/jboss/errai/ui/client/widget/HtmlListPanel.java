package org.jboss.errai.ui.client.widget;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.ComplexPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * HtmlList panel creates an ordered (ol) or unordered (ul) element for lists
 * @author edewit@redhat.com
 */
public class HtmlListPanel extends ComplexPanel {
  public HtmlListPanel(boolean ordered) {
    if (ordered) {
      setElement(DOM.createElement("ol"));
    } else {
      setElement(DOM.createElement("ul"));
    }
  }

  @Override
  public void add(Widget w) {
    add(w, getElement());
  }
}
