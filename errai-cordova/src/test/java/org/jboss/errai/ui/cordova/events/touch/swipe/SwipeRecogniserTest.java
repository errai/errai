/*
 * Copyright (C) 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.ui.cordova.events.touch.swipe;

import static org.jboss.errai.ui.cordova.events.touch.swipe.SwipeEvent.Direction;
import static org.junit.Assert.*;

import org.jboss.errai.ui.cordova.events.touch.mock.MockHasHandlers;
import org.jboss.errai.ui.cordova.events.touch.mock.MockTouchEndEvent;
import org.jboss.errai.ui.cordova.events.touch.mock.MockTouchMoveEvent;
import org.jboss.errai.ui.cordova.events.touch.mock.MockTouchStartEvent;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.gwt.event.shared.GwtEvent;
import com.google.gwtmockito.GwtMockitoTestRunner;

/**
 * @author edewit@redhat.com
 */
@RunWith(GwtMockitoTestRunner.class)
public class SwipeRecogniserTest {

  private MockHasHandlers hasHandlers = new MockHasHandlers();
  private SwipeRecognizer swipeRecognizer = new SwipeRecognizer(hasHandlers);

  @Test(expected = IllegalArgumentException.class)
  public void testSwipeRecognizerHasHandlers() {
    new SwipeRecognizer(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSwipeRecognizerHasHandlersInt() {
    new SwipeRecognizer(hasHandlers, -1);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSwipeRecognizerHasHandlersInt1() {
    new SwipeRecognizer(hasHandlers, 2);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSwipeRecognizerHasHandlersIntInt() {
    new SwipeRecognizer(hasHandlers, 20, -1);
  }

  @Test
  public void testSimpleHorizontalSwipe() {
    swipeRecognizer.onTouchStart(new MockTouchStartEvent(1, 0, 0));

    boolean first = true;
    int i;
    for (i = 1; i < 60; i++) {
      swipeRecognizer.onTouchMove(new MockTouchMoveEvent(1, i, 0));
      if (i >= swipeRecognizer.getThreshold()) {
        GwtEvent<?> event = hasHandlers.getEvent();

        if (first) {
          first = false;
          if (!(event instanceof SwipeStartEvent)) {
            fail("swipe start event was not fired");
          }
          SwipeStartEvent swipeStartEvent = (SwipeStartEvent) event;

          assertEquals(Direction.LEFT_TO_RIGHT, swipeStartEvent.getDirection());
          assertEquals(swipeRecognizer.getThreshold(),
                  swipeStartEvent.getDistance());

        }
        else {
          if (!(event instanceof SwipeMoveEvent)) {
            fail("swipe move event was not fired");
          }
          SwipeMoveEvent swipeMoveEvent = (SwipeMoveEvent) event;

          assertEquals(Direction.LEFT_TO_RIGHT, swipeMoveEvent.getDirection());
          assertEquals(i, swipeMoveEvent.getDistance());

          if (swipeMoveEvent.getDistance() > swipeRecognizer.getMinDistance()) {
            assertTrue(swipeMoveEvent.isDistanceReached());
          }
          else {
            Assert.assertFalse(swipeMoveEvent.isDistanceReached());
          }

        }
      }
    }

    swipeRecognizer.onTouchEnd(new MockTouchEndEvent());

    GwtEvent<?> event = hasHandlers.getEvent();

    if (!(event instanceof SwipeEndEvent)) {
      fail("swipe end event was not fired");
    }
    SwipeEndEvent swipeEndEvent = (SwipeEndEvent) event;

    assertEquals(Direction.LEFT_TO_RIGHT, swipeEndEvent.getDirection());
    assertEquals(i - 1, swipeEndEvent.getDistance());

    assertTrue(swipeEndEvent.isDistanceReached());

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
            fail("swipe start event was not fired");
          }
          SwipeStartEvent swipeStartEvent = (SwipeStartEvent) event;

          assertEquals(Direction.LEFT_TO_RIGHT, swipeStartEvent.getDirection());
          assertEquals(swipeRecognizer.getThreshold(),
                  swipeStartEvent.getDistance());

        }
        else {
          if (!(event instanceof SwipeMoveEvent)) {
            fail("swipe move event was not fired");
          }
          SwipeMoveEvent swipeMoveEvent = (SwipeMoveEvent) event;

          assertEquals(Direction.LEFT_TO_RIGHT, swipeMoveEvent.getDirection());
          assertEquals(i, swipeMoveEvent.getDistance());

          if (swipeMoveEvent.getDistance() > swipeRecognizer.getMinDistance()) {
            assertTrue(swipeMoveEvent.isDistanceReached());
          }
          else {
            Assert.assertFalse(swipeMoveEvent.isDistanceReached());
          }

        }
      }
    }

    swipeRecognizer.onTouchEnd(new MockTouchEndEvent());

    GwtEvent<?> event = hasHandlers.getEvent();

    if (!(event instanceof SwipeEndEvent)) {
      fail("swipe end event was not fired");
    }
    SwipeEndEvent swipeEndEvent = (SwipeEndEvent) event;

    assertEquals(Direction.LEFT_TO_RIGHT, swipeEndEvent.getDirection());
    assertEquals(i - 1, swipeEndEvent.getDistance());

    assertTrue(swipeEndEvent.isDistanceReached());

    swipeRecognizer.onTouchStart(new MockTouchStartEvent(1, 0, 0));

    first = true;

    for (i = 1; i < 60; i++) {
      swipeRecognizer.onTouchMove(new MockTouchMoveEvent(1, i, 0));
      if (i >= swipeRecognizer.getThreshold()) {
        event = hasHandlers.getEvent();

        if (first) {
          first = false;
          if (!(event instanceof SwipeStartEvent)) {
            fail("swipe start event was not fired");
          }
          SwipeStartEvent swipeStartEvent = (SwipeStartEvent) event;

          assertEquals(Direction.LEFT_TO_RIGHT, swipeStartEvent.getDirection());
          assertEquals(swipeRecognizer.getThreshold(),
                  swipeStartEvent.getDistance());

        }
        else {
          if (!(event instanceof SwipeMoveEvent)) {
            fail("swipe move event was not fired");
          }
          SwipeMoveEvent swipeMoveEvent = (SwipeMoveEvent) event;

          assertEquals(Direction.LEFT_TO_RIGHT, swipeMoveEvent.getDirection());
          assertEquals(i, swipeMoveEvent.getDistance());

          if (swipeMoveEvent.getDistance() > swipeRecognizer.getMinDistance()) {
            assertTrue(swipeMoveEvent.isDistanceReached());
          }
          else {
            Assert.assertFalse(swipeMoveEvent.isDistanceReached());
          }

        }
      }
    }

    swipeRecognizer.onTouchEnd(new MockTouchEndEvent());

    event = hasHandlers.getEvent();

    if (!(event instanceof SwipeEndEvent)) {
      fail("swipe end event was not fired");
    }
    swipeEndEvent = (SwipeEndEvent) event;

    assertEquals(Direction.LEFT_TO_RIGHT, swipeEndEvent.getDirection());
    assertEquals(i - 1, swipeEndEvent.getDistance());

    assertTrue(swipeEndEvent.isDistanceReached());

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
            fail("swipe start event was not fired");
          }
          SwipeStartEvent swipeStartEvent = (SwipeStartEvent) event;

          assertEquals(Direction.TOP_TO_BOTTOM, swipeStartEvent.getDirection());
          assertEquals(swipeRecognizer.getThreshold(),
                  swipeStartEvent.getDistance());

        }
        else {
          if (!(event instanceof SwipeMoveEvent)) {
            fail("swipe move event was not fired");
          }
          SwipeMoveEvent swipeMoveEvent = (SwipeMoveEvent) event;

          assertEquals(Direction.TOP_TO_BOTTOM, swipeMoveEvent.getDirection());
          assertEquals(i, swipeMoveEvent.getDistance());

          if (swipeMoveEvent.getDistance() > swipeRecognizer.getMinDistance()) {
            assertTrue(swipeMoveEvent.isDistanceReached());
          }
          else {
            Assert.assertFalse(swipeMoveEvent.isDistanceReached());
          }

        }
      }
    }

    swipeRecognizer.onTouchEnd(new MockTouchEndEvent());

    GwtEvent<?> event = hasHandlers.getEvent();

    if (!(event instanceof SwipeEndEvent)) {
      fail("swipe end event was not fired");
    }
    SwipeEndEvent swipeEndEvent = (SwipeEndEvent) event;

    assertEquals(Direction.TOP_TO_BOTTOM, swipeEndEvent.getDirection());
    assertEquals(i - 1, swipeEndEvent.getDistance());

    assertTrue(swipeEndEvent.isDistanceReached());

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
            fail("swipe start event was not fired");
          }
          SwipeStartEvent swipeStartEvent = (SwipeStartEvent) event;

          assertEquals(Direction.TOP_TO_BOTTOM, swipeStartEvent.getDirection());
          assertEquals(swipeRecognizer.getThreshold(),
                  swipeStartEvent.getDistance());

        }
        else {
          if (!(event instanceof SwipeMoveEvent)) {
            fail("swipe move event was not fired");
          }
          SwipeMoveEvent swipeMoveEvent = (SwipeMoveEvent) event;

          assertEquals(Direction.TOP_TO_BOTTOM, swipeMoveEvent.getDirection());
          assertEquals(i, swipeMoveEvent.getDistance());

          if (swipeMoveEvent.getDistance() > swipeRecognizer.getMinDistance()) {
            assertTrue(swipeMoveEvent.isDistanceReached());
          }
          else {
            Assert.assertFalse(swipeMoveEvent.isDistanceReached());
          }

        }
      }
    }

    swipeRecognizer.onTouchEnd(new MockTouchEndEvent());

    GwtEvent<?> event = hasHandlers.getEvent();

    if (!(event instanceof SwipeEndEvent)) {
      fail("swipe end event was not fired");
    }
    SwipeEndEvent swipeEndEvent = (SwipeEndEvent) event;

    assertEquals(Direction.TOP_TO_BOTTOM, swipeEndEvent.getDirection());
    assertEquals(i - 1, swipeEndEvent.getDistance());

    assertTrue(swipeEndEvent.isDistanceReached());

  }

}
