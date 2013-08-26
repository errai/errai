package org.jboss.errai.ui.cordova.events.touch.swipe;

import com.google.gwt.event.shared.GwtEvent;
import com.google.gwtmockito.GwtMockitoTestRunner;
import org.jboss.errai.ui.cordova.events.swipe.SwipeEndEvent;
import org.jboss.errai.ui.cordova.events.swipe.SwipeMoveEvent;
import org.jboss.errai.ui.cordova.events.swipe.SwipeRecognizer;
import org.jboss.errai.ui.cordova.events.swipe.SwipeStartEvent;
import org.jboss.errai.ui.cordova.events.touch.mock.MockHasHandlers;
import org.jboss.errai.ui.cordova.events.touch.mock.MockTouchEndEvent;
import org.jboss.errai.ui.cordova.events.touch.mock.MockTouchMoveEvent;
import org.jboss.errai.ui.cordova.events.touch.mock.MockTouchStartEvent;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.jboss.errai.ui.cordova.events.swipe.SwipeEvent.Direction;

/**
 * @author edewit@redhat.com
 */
@RunWith(GwtMockitoTestRunner.class)
public class SwipeRecogniserTest {

  private MockHasHandlers hasHandlers = new MockHasHandlers();
  private SwipeRecognizer swipeRecognizer = new SwipeRecognizer(hasHandlers);

  @Test
  public void testSwipeRecognizerHasHandlers() {
    try {
      new SwipeRecognizer(null);
      Assert.fail("expected exception did not occur");

    } catch (IllegalArgumentException e) {

    }
  }

  @Test
  public void testSwipeRecognizerHasHandlersInt() {
    try {
      new SwipeRecognizer(hasHandlers, -1);
      Assert.fail("expected exception did not occur");

    } catch (IllegalArgumentException e) {

    }
  }

  @Test
  public void testSwipeRecognizerHasHandlersInt1() {
    try {
      new SwipeRecognizer(hasHandlers, 2);
      Assert.fail("expected exception did not occur");

    } catch (IllegalArgumentException e) {

    }
  }

  @Test
  public void testSwipeRecognizerHasHandlersIntInt() {
    try {
      new SwipeRecognizer(hasHandlers, 20, -1);
      Assert.fail("expected exception did not occur");

    } catch (IllegalArgumentException e) {

    }
  }

  @Test
  public void testSimpleHorizontalSwipe() {
    swipeRecognizer.onTouchStart(new MockTouchStartEvent(1, 0, 0));

    boolean first = true;
    int i = 1;
    for (i = 1; i < 60; i++) {
      swipeRecognizer.onTouchMove(new MockTouchMoveEvent(1, i, 0));
      if (i >= swipeRecognizer.getThreshold()) {
        GwtEvent<?> event = hasHandlers.getEvent();

        if (first) {
          first = false;
          if (!(event instanceof SwipeStartEvent)) {
            Assert.fail("swipe start event was not fired");
          }
          SwipeStartEvent swipeStartEvent = (SwipeStartEvent) event;

          Assert.assertEquals(Direction.LEFT_TO_RIGHT, swipeStartEvent.getDirection());
          Assert.assertEquals(swipeRecognizer.getThreshold(), swipeStartEvent.getDistance());

        } else {
          if (!(event instanceof SwipeMoveEvent)) {
            Assert.fail("swipe move event was not fired");
          }
          SwipeMoveEvent swipeMoveEvent = (SwipeMoveEvent) event;

          Assert.assertEquals(Direction.LEFT_TO_RIGHT, swipeMoveEvent.getDirection());
          Assert.assertEquals(i, swipeMoveEvent.getDistance());

          if (swipeMoveEvent.getDistance() > swipeRecognizer.getMinDistance()) {
            Assert.assertTrue(swipeMoveEvent.isDistanceReached());
          } else {
            Assert.assertFalse(swipeMoveEvent.isDistanceReached());
          }

        }
      }
    }

    swipeRecognizer.onTouchEnd(new MockTouchEndEvent());

    GwtEvent<?> event = hasHandlers.getEvent();

    if (!(event instanceof SwipeEndEvent)) {
      Assert.fail("swipe end event was not fired");
    }
    SwipeEndEvent swipeEndEvent = (SwipeEndEvent) event;

    Assert.assertEquals(Direction.LEFT_TO_RIGHT, swipeEndEvent.getDirection());
    Assert.assertEquals(i - 1, swipeEndEvent.getDistance());

    Assert.assertTrue(swipeEndEvent.isDistanceReached());

  }

  @Test
  public void testSimpleHorizontalSwipe2Times() {
    swipeRecognizer.onTouchStart(new MockTouchStartEvent(1, 0, 0));

    boolean first = true;
    int i = 1;
    for (i = 1; i < 60; i++) {
      swipeRecognizer.onTouchMove(new MockTouchMoveEvent(1, i, 0));
      if (i >= swipeRecognizer.getThreshold()) {
        GwtEvent<?> event = hasHandlers.getEvent();

        if (first) {
          first = false;
          if (!(event instanceof SwipeStartEvent)) {
            Assert.fail("swipe start event was not fired");
          }
          SwipeStartEvent swipeStartEvent = (SwipeStartEvent) event;

          Assert.assertEquals(Direction.LEFT_TO_RIGHT, swipeStartEvent.getDirection());
          Assert.assertEquals(swipeRecognizer.getThreshold(), swipeStartEvent.getDistance());

        } else {
          if (!(event instanceof SwipeMoveEvent)) {
            Assert.fail("swipe move event was not fired");
          }
          SwipeMoveEvent swipeMoveEvent = (SwipeMoveEvent) event;

          Assert.assertEquals(Direction.LEFT_TO_RIGHT, swipeMoveEvent.getDirection());
          Assert.assertEquals(i, swipeMoveEvent.getDistance());

          if (swipeMoveEvent.getDistance() > swipeRecognizer.getMinDistance()) {
            Assert.assertTrue(swipeMoveEvent.isDistanceReached());
          } else {
            Assert.assertFalse(swipeMoveEvent.isDistanceReached());
          }

        }
      }
    }

    swipeRecognizer.onTouchEnd(new MockTouchEndEvent());

    GwtEvent<?> event = hasHandlers.getEvent();

    if (!(event instanceof SwipeEndEvent)) {
      Assert.fail("swipe end event was not fired");
    }
    SwipeEndEvent swipeEndEvent = (SwipeEndEvent) event;

    Assert.assertEquals(Direction.LEFT_TO_RIGHT, swipeEndEvent.getDirection());
    Assert.assertEquals(i - 1, swipeEndEvent.getDistance());

    Assert.assertTrue(swipeEndEvent.isDistanceReached());

    swipeRecognizer.onTouchStart(new MockTouchStartEvent(1, 0, 0));

    first = true;

    for (i = 1; i < 60; i++) {
      swipeRecognizer.onTouchMove(new MockTouchMoveEvent(1, i, 0));
      if (i >= swipeRecognizer.getThreshold()) {
        event = hasHandlers.getEvent();

        if (first) {
          first = false;
          if (!(event instanceof SwipeStartEvent)) {
            Assert.fail("swipe start event was not fired");
          }
          SwipeStartEvent swipeStartEvent = (SwipeStartEvent) event;

          Assert.assertEquals(Direction.LEFT_TO_RIGHT, swipeStartEvent.getDirection());
          Assert.assertEquals(swipeRecognizer.getThreshold(), swipeStartEvent.getDistance());

        } else {
          if (!(event instanceof SwipeMoveEvent)) {
            Assert.fail("swipe move event was not fired");
          }
          SwipeMoveEvent swipeMoveEvent = (SwipeMoveEvent) event;

          Assert.assertEquals(Direction.LEFT_TO_RIGHT, swipeMoveEvent.getDirection());
          Assert.assertEquals(i, swipeMoveEvent.getDistance());

          if (swipeMoveEvent.getDistance() > swipeRecognizer.getMinDistance()) {
            Assert.assertTrue(swipeMoveEvent.isDistanceReached());
          } else {
            Assert.assertFalse(swipeMoveEvent.isDistanceReached());
          }

        }
      }
    }

    swipeRecognizer.onTouchEnd(new MockTouchEndEvent());

    event = hasHandlers.getEvent();

    if (!(event instanceof SwipeEndEvent)) {
      Assert.fail("swipe end event was not fired");
    }
    swipeEndEvent = (SwipeEndEvent) event;

    Assert.assertEquals(Direction.LEFT_TO_RIGHT, swipeEndEvent.getDirection());
    Assert.assertEquals(i - 1, swipeEndEvent.getDistance());

    Assert.assertTrue(swipeEndEvent.isDistanceReached());

  }

  @Test
  public void testSimpleVerticalSwipe() {
    swipeRecognizer.onTouchStart(new MockTouchStartEvent(1, 0, 0));

    boolean first = true;
    int i = 1;
    for (i = 1; i < 60; i++) {
      swipeRecognizer.onTouchMove(new MockTouchMoveEvent(1, 0, i));
      if (i >= swipeRecognizer.getThreshold()) {
        GwtEvent<?> event = hasHandlers.getEvent();

        if (first) {
          first = false;
          if (!(event instanceof SwipeStartEvent)) {
            Assert.fail("swipe start event was not fired");
          }
          SwipeStartEvent swipeStartEvent = (SwipeStartEvent) event;

          Assert.assertEquals(Direction.TOP_TO_BOTTOM, swipeStartEvent.getDirection());
          Assert.assertEquals(swipeRecognizer.getThreshold(), swipeStartEvent.getDistance());

        } else {
          if (!(event instanceof SwipeMoveEvent)) {
            Assert.fail("swipe move event was not fired");
          }
          SwipeMoveEvent swipeMoveEvent = (SwipeMoveEvent) event;

          Assert.assertEquals(Direction.TOP_TO_BOTTOM, swipeMoveEvent.getDirection());
          Assert.assertEquals(i, swipeMoveEvent.getDistance());

          if (swipeMoveEvent.getDistance() > swipeRecognizer.getMinDistance()) {
            Assert.assertTrue(swipeMoveEvent.isDistanceReached());
          } else {
            Assert.assertFalse(swipeMoveEvent.isDistanceReached());
          }

        }
      }
    }

    swipeRecognizer.onTouchEnd(new MockTouchEndEvent());

    GwtEvent<?> event = hasHandlers.getEvent();

    if (!(event instanceof SwipeEndEvent)) {
      Assert.fail("swipe end event was not fired");
    }
    SwipeEndEvent swipeEndEvent = (SwipeEndEvent) event;

    Assert.assertEquals(Direction.TOP_TO_BOTTOM, swipeEndEvent.getDirection());
    Assert.assertEquals(i - 1, swipeEndEvent.getDistance());

    Assert.assertTrue(swipeEndEvent.isDistanceReached());

  }

  @Test
  public void testSimpleVerticalSwipeAfterRandomInput() {

    swipeRecognizer.onTouchStart(new MockTouchStartEvent(2, 0, 0));
    swipeRecognizer.onTouchMove(new MockTouchMoveEvent(2, 4, 4));
    swipeRecognizer.onTouchEnd(new MockTouchEndEvent());

    swipeRecognizer.onTouchStart(new MockTouchStartEvent(1, 0, 0));

    boolean first = true;
    int i = 1;
    for (i = 1; i < 60; i++) {
      swipeRecognizer.onTouchMove(new MockTouchMoveEvent(1, 0, i));
      if (i >= swipeRecognizer.getThreshold()) {
        GwtEvent<?> event = hasHandlers.getEvent();

        if (first) {
          first = false;
          if (!(event instanceof SwipeStartEvent)) {
            Assert.fail("swipe start event was not fired");
          }
          SwipeStartEvent swipeStartEvent = (SwipeStartEvent) event;

          Assert.assertEquals(Direction.TOP_TO_BOTTOM, swipeStartEvent.getDirection());
          Assert.assertEquals(swipeRecognizer.getThreshold(), swipeStartEvent.getDistance());

        } else {
          if (!(event instanceof SwipeMoveEvent)) {
            Assert.fail("swipe move event was not fired");
          }
          SwipeMoveEvent swipeMoveEvent = (SwipeMoveEvent) event;

          Assert.assertEquals(Direction.TOP_TO_BOTTOM, swipeMoveEvent.getDirection());
          Assert.assertEquals(i, swipeMoveEvent.getDistance());

          if (swipeMoveEvent.getDistance() > swipeRecognizer.getMinDistance()) {
            Assert.assertTrue(swipeMoveEvent.isDistanceReached());
          } else {
            Assert.assertFalse(swipeMoveEvent.isDistanceReached());
          }

        }
      }
    }

    swipeRecognizer.onTouchEnd(new MockTouchEndEvent());

    GwtEvent<?> event = hasHandlers.getEvent();

    if (!(event instanceof SwipeEndEvent)) {
      Assert.fail("swipe end event was not fired");
    }
    SwipeEndEvent swipeEndEvent = (SwipeEndEvent) event;

    Assert.assertEquals(Direction.TOP_TO_BOTTOM, swipeEndEvent.getDirection());
    Assert.assertEquals(i - 1, swipeEndEvent.getDistance());

    Assert.assertTrue(swipeEndEvent.isDistanceReached());

  }

}
