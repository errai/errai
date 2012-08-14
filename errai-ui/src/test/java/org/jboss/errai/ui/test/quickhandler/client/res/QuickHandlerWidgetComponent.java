package org.jboss.errai.ui.test.quickhandler.client.res;

import javax.enterprise.context.Dependent;

import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;

@Dependent
@Templated
public class QuickHandlerWidgetComponent extends Composite {

  @DataField("b1")
  private Button button = new Button();

  @EventHandler("b1")
  public void doSomethingC1(ClickEvent e) {
    // do something
  }
}
