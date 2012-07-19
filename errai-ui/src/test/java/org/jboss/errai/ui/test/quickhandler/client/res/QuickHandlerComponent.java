package org.jboss.errai.ui.test.quickhandler.client.res;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;

@Dependent
@Templated
public class QuickHandlerComponent extends Composite {

  @Inject
  @DataField("c1")
  private Label content;

  @Inject
  @DataField
  private Button c2;

  private boolean clicked = false;

  public Label getContent() {
    return content;
  }

  public Button getC2() {
    return c2;
  }
  
  @EventHandler("c2")
  public void doSomething(ClickEvent e)
  {
    clicked = true;
  }

  public boolean isClicked() {
    return clicked;
  }

}
