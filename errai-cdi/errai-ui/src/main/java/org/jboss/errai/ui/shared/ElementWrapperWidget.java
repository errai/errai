package org.jboss.errai.ui.shared;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

/**
 * Used to merge a {@link Template} onto a {@link Composite} component.
 * 
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public class ElementWrapperWidget extends Widget {

  public ElementWrapperWidget(Element wrapped) {
    this.setElement(getElement());
    DOM.setEventListener(this.getElement(), this);
  }
  
}
