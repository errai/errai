package org.jboss.errai.ui.test.handler.client.res;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;

import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;

@Dependent
@Templated
public class HandledComponent extends Composite {

  @DataField
  private Button b1;

  @DataField
  private Button b2;

  @DataField
  private VocalWidget b3;

  @PostConstruct
  public void init() {
    b1.getElement().setAttribute("id", "b1");
    b2.getElement().setAttribute("id", "b2");
    b3.getElement().setAttribute("id", "b3");

    b1.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        System.out.println("Handled click event on b1.");
        b1.removeFromParent();
      }
    });
  }

  public Button getB1() {
    return b1;
  }

  public Button getB2() {
    return b2;
  }

}
