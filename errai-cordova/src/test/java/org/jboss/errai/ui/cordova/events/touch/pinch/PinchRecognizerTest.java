package org.jboss.errai.ui.cordova.events.touch.pinch;

import com.google.gwt.event.shared.GwtEvent;
import com.google.gwtmockito.GwtMockitoTestRunner;
import org.jboss.errai.ui.cordova.events.touch.mock.MockHasHandlers;
import org.jboss.errai.ui.cordova.events.touch.mock.MockTouchMoveEvent;
import org.jboss.errai.ui.cordova.events.touch.mock.MockTouchStartEvent;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

/**
 * @author edewit@redhat.com
 */
@RunWith(GwtMockitoTestRunner.class)
public class PinchRecognizerTest {

  private MockHasHandlers hasHandlers = new MockHasHandlers();
  private PinchRecognizer pinchRecognizer = new PinchRecognizer(hasHandlers, new OffsetProvider() {
    @Override
    public int getLeft() {
      return 0;
    }

    @Override
    public int getTop() {
      return 0;
    }
  });

  @Test
  public void testSimplePinch() {
    //when
    pinchRecognizer.onTouchStart(new MockTouchStartEvent(1, 0, 0));
    pinchRecognizer.onTouchStart(new MockTouchStartEvent(1, 0, 0, 2, 100, 100));
    pinchRecognizer.onTouchMove(new MockTouchMoveEvent(1, 0, 0, 2, 50, 50));

    //then
    GwtEvent<?> event = hasHandlers.getEvent();

    if (!(event instanceof PinchEvent)) {
      Assert.fail("no pinch event");
    }
    PinchEvent pinchEvent = (PinchEvent) event;

    assertEquals(25, pinchEvent.getX());
    assertEquals(25, pinchEvent.getY());

    assertEquals(1.41421356237309, pinchEvent.getScaleFactor(), 0.0001);
  }

}
