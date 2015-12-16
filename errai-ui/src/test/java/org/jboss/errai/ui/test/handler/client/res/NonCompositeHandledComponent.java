package org.jboss.errai.ui.test.handler.client.res;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.jboss.errai.ui.test.common.client.TestModel;

import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Button;

@Templated("HandledComponent.html")
public class NonCompositeHandledComponent implements HandledComponent {

  @DataField
  private Element root = DOM.createDiv();

  private final Button b1;

  @DataField
  private final Button b2 = new Button("Will be rendered inside button from GWT");

  private final VocalWidget b3;

  @Inject
  public NonCompositeHandledComponent(@DataField Button b1, @DataField VocalWidget b3, DataBinder<TestModel> binder) {
    this.b1 = b1;
    this.b3 = b3;
  }

  @PostConstruct
  public void init() {
    b1.getElement().setAttribute("id", "b1");
    b2.getElement().setAttribute("id", "b2");
    b3.getElement().setAttribute("id", "b3");

    b1.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        b1.removeFromParent();
      }
    });
  }

  public Element getRoot() {
    return root;
  }

  @Override
  public Button getB1() {
    return b1;
  }

  @Override
  public Button getB2() {
    return b2;
  }

}
