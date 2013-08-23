package org.jboss.errai.ui.cordova.events.touch.longtap;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.dom.client.Touch;
import com.google.gwt.event.dom.client.TouchMoveEvent;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MockTouchMoveEvent extends TouchMoveEvent {

  private JsArray<Touch> touches;

  private Touch touch;

  public MockTouchMoveEvent(int id, int x, int y) {
    touches = mock(JsArray.class);
    touch = mock(Touch.class);
    when(touches.length()).thenReturn(1);
    when(touches.get(0)).thenReturn(touch);
    when(touch.getIdentifier()).thenReturn(id);
    when(touch.getPageX()).thenReturn(x);
    when(touch.getPageY()).thenReturn(y);
  }

  @Override
  public JsArray<Touch> getTouches() {
    return touches;
  }
}
