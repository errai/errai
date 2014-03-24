package org.jboss.errai.processor.testcase;

import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.SinkNative;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.TextBox;

@Templated("empty-template.html")
public class EventHandlerNonVoidReturnType extends Composite {

  @DataField TextBox myTextBox;

  @EventHandler("myTextBox")
  Object onKeyPressed(KeyPressEvent e) {
    // no op
  }
  
  @SinkNative(Event.ONMOUSEOVER)
  @EventHandler("image-with-sinknative")
  boolean onMouseOver() {
    // no op
  }
}
