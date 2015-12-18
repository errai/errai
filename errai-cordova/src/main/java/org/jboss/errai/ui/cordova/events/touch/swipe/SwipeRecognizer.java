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

import com.google.gwt.dom.client.Touch;
import com.google.gwt.event.dom.client.TouchCancelEvent;
import com.google.gwt.event.dom.client.TouchEndEvent;
import com.google.gwt.event.dom.client.TouchMoveEvent;
import com.google.gwt.event.dom.client.TouchStartEvent;
import com.google.gwt.event.shared.HasHandlers;
import org.jboss.errai.ui.cordova.events.touch.AbstractRecognizer;
import org.jboss.errai.ui.cordova.events.touch.TouchPoint;

import static org.jboss.errai.ui.cordova.events.touch.swipe.SwipeEvent.Direction;

public class SwipeRecognizer extends AbstractRecognizer {

  private final HasHandlers source;
  private final int minDistance;
  private final int threshold;
  private int touchCount;

  private State state;

  private Direction direction;

  private int lastDistance;
  private int x;
  private int y;

  /**
   * construct a swipe recognizer
   *
   * @param source the source to fire events on
   */
  public SwipeRecognizer(HasHandlers source) {
    this(source, 40);
  }

  /**
   * construct a swipe recognizer
   *
   * @param source      the source to fire events on
   * @param minDistance the minimum distance to cover before this counts as a swipe
   */
  public SwipeRecognizer(HasHandlers source, int minDistance) {
    this(source, minDistance, 10);
  }

  /**
   * construct a swipe recognizer
   *
   * @param source      the source to fire events on
   * @param minDistance the minimum distance to cover before this counts as a swipe
   * @param threshold   the initial threshold before swipe start is fired
   */
  public SwipeRecognizer(HasHandlers source, int minDistance, int threshold) {
    if (source == null)
      throw new IllegalArgumentException("source can not be null");

    if (minDistance <= 0 || minDistance < threshold) {
      throw new IllegalArgumentException("minDistance > 0 and minDistance > threshold");
    }

    if (threshold <= 0) {
      throw new IllegalArgumentException("threshold > 0");
    }

    this.source = source;
    this.minDistance = minDistance;
    this.threshold = threshold;
    this.touchCount = 0;
    state = State.READY;
  }

  @Override
  public void onTouchStart(TouchStartEvent event) {
    touchCount++;

    switch (state) {
      case INVALID:
        break;

      case READY:
        state = State.FINDER_DOWN;

        x = event.getTouches().get(0).getPageX();
        y = event.getTouches().get(0).getPageY();
        break;

      case FINDER_DOWN:
      default:
        state = State.INVALID;
        break;
    }

  }

  @Override
  public void onTouchMove(TouchMoveEvent event) {
    Touch touch = event.getTouches().get(0);

    switch (state) {
      case INVALID:

        break;
      case READY:
        // WTF?
        state = State.INVALID;
        break;
      case FINDER_DOWN:

        // log(" X: " + touch.getPageX() + " old: " + touchStart.getPageX() + " test: " + x);

        if (Math.abs(touch.getPageX() - x) >= threshold) {
          state = State.FOUND_DIRECTION;

          direction = touch.getPageX() - x > 0 ? Direction.LEFT_TO_RIGHT : Direction.RIGHT_TO_LEFT;

          SwipeStartEvent swipeStartEvent =
                  new SwipeStartEvent(new TouchPoint(touch), touch.getPageX() - x, direction);

          source.fireEvent(swipeStartEvent);

        } else {
          if (Math.abs(touch.getPageY() - y) >= threshold) {
            state = State.FOUND_DIRECTION;

            direction = touch.getPageY() - y > 0 ? Direction.TOP_TO_BOTTOM : Direction.BOTTOM_TO_TOP;

            SwipeStartEvent swipeStartEvent =
                    new SwipeStartEvent(new TouchPoint(touch), touch.getPageY() - y, direction);

            source.fireEvent(swipeStartEvent);

          }

        }
        break;

      case FOUND_DIRECTION:

        switch (direction) {
          case TOP_TO_BOTTOM:
          case BOTTOM_TO_TOP:
            lastDistance = Math.abs(touch.getPageY() - y);
            source.fireEvent(
                    new SwipeMoveEvent(new TouchPoint(touch), lastDistance > minDistance,
                            lastDistance, direction));
            break;

          case LEFT_TO_RIGHT:
          case RIGHT_TO_LEFT:
            lastDistance = Math.abs(touch.getPageX() - x);
            source.fireEvent(
                    new SwipeMoveEvent(new TouchPoint(touch), lastDistance > minDistance,
                            lastDistance, direction));

            break;

          default:
            break;
        }

        break;

      default:
        break;
    }

  }

  @Override
  public void onTouchEnd(TouchEndEvent event) {
    touchCount--;

    switch (state) {
      case FOUND_DIRECTION:
        source.fireEvent(new SwipeEndEvent(lastDistance > minDistance, lastDistance, direction));
        reset();
        break;

      default:
        reset();
        break;
    }

  }

  @Override
  public void onTouchCancel(TouchCancelEvent event) {
    touchCount--;
    if (touchCount <= 0) {
      reset();
    }
  }

  /**
   * the threshold before an event is fired (deadzone)
   *
   * @return the threshold in px
   */
  public int getThreshold() {
    return threshold;
  }

  /**
   * the distance that needs to be covered before counting as a swipe
   *
   * @return the distance in px
   */
  public int getMinDistance() {
    return minDistance;
  }

  private void reset() {
    state = State.READY;
    touchCount = 0;
  }
}
