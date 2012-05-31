package org.jboss.errai.ui.test.handler.client.res;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Widget;

public class VocalWidget extends Widget {

  public VocalWidget() {
    super();
    setElement(DOM.createDiv());
  }

  @Override
  protected void onAttach() {
    System.out.println("Vocalwidget attached!");
    super.onAttach();
  }

}
