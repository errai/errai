package org.jboss.errai.ui.shared;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasHTML;
import com.google.gwt.user.client.ui.Widget;

/**
 * Used to merge a {@link Template} onto a {@link Composite} component.
 * 
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public class ElementWrapperWidget extends Widget implements HasHTML {

  public ElementWrapperWidget(Element wrapped) {
    if (wrapped == null) {
      throw new IllegalArgumentException(
              "Element to be wrapped must not be null - Did you forget to initialize or @Inject a @DataField?");
    }
    this.setElement(wrapped);
    DOM.setEventListener(this.getElement(), this);
  }

  @Override
  public String getText() {
    return getElement().getInnerText();
  }

  @Override
  public void setText(String text) {
    getElement().setInnerText(text);
  }

  @Override
  public String getHTML() {
    return getElement().getInnerHTML();
  }

  @Override
  public void setHTML(String html) {
    getElement().setInnerHTML(html);
  }

}
