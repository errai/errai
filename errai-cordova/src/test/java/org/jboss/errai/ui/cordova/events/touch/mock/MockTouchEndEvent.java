package org.jboss.errai.ui.cordova.events.touch.mock;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.dom.client.Touch;
import com.google.gwt.event.dom.client.TouchEndEvent;

import static org.mockito.Mockito.mock;

public class MockTouchEndEvent extends TouchEndEvent {

  private JsArray<Touch> touches;

  public MockTouchEndEvent() {
    touches = mock(JsArray.class);
  }

  @Override
  public JsArray<Touch> getTouches() {
    return touches;
  }

}
