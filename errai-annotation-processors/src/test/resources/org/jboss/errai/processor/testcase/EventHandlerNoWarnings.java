package org.jboss.errai.processor.testcase;

import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.SinkNative;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.TextBox;

@Templated
public class EventHandlerNoWarnings extends Composite {

  @DataField TextBox myTextBox;

  /** Correct usage for events on a child widget: event handler refers to existing field in this class. */
  @EventHandler("myTextBox")
  void onKeyPressed(KeyPressEvent e) {
    // no op
  }

  /** Correct usage for events on self: event handler defaults to this templated widget. */
  @EventHandler
  void onKeyPressedOnSelf(KeyPressEvent e) {
    // no op
  }

  /** Correct usage of SinkNative: event handler refers to existing element in template. */
  @SinkNative(Event.ONMOUSEOVER)
  @EventHandler("image-with-sinknative")
  void onMouseOver(Event e) {
    // no op
  }
}
