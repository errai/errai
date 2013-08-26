package org.jboss.errai.ui.cordova.events.touch.mock;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.dom.client.Touch;
import com.google.gwt.event.dom.client.TouchStartEvent;

import static org.mockito.Mockito.*;

public class MockTouchStartEvent extends TouchStartEvent {

  private JsArray<Touch> touches;

  private Touch touch;

  public MockTouchStartEvent(int id, int x, int y) {
    touches = mock(JsArray.class);
    touch = mock(Touch.class);
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
