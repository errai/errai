package org.jboss.errai.ui.cordova.events.touch.mock;

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

  public MockTouchMoveEvent(int id1, int x1, int y1, int id2, int x2, int y2) {
    this(id1, x1, y1);

    final Touch touch1 = mock(Touch.class);
    when(touches.get(1)).thenReturn(touch1);
    when(touch1.getIdentifier()).thenReturn(id2);
    when(touch1.getPageX()).thenReturn(x2);
    when(touch1.getPageY()).thenReturn(y2);
  }


  @Override
  public JsArray<Touch> getTouches() {
    return touches;
  }
}
