/*
 * Copyright 2012 Daniel Kurka
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.jboss.errai.ui.cordova.events.touch.swipe;

import org.jboss.errai.ui.cordova.events.touch.TouchPoint;

import com.google.gwt.event.shared.GwtEvent;

/**
 * A {@link SwipeMoveEvent} occurs when the user moves his finger over the
 * display
 * 
 * @author Daniel Kurka
 * 
 */
public class SwipeMoveEvent extends SwipeEvent<SwipeMoveHandler> {

  private final static GwtEvent.Type<SwipeMoveHandler> TYPE = new Type<SwipeMoveHandler>();
  private final boolean distanceReached;
  private final int distance;
  private final TouchPoint touch;

  public static GwtEvent.Type<SwipeMoveHandler> getType() {
    return TYPE;
  }

  /**
   * Construct a {@link SwipeMoveEvent}
   * 
   * @param touch
   *          the touch of the finger
   * @param distanceReached
   *          is the minimum distance reached for this swipe
   * @param distance
   *          the distance in px
   * @param direction
   *          the direction of the swipe
   */
  public SwipeMoveEvent(TouchPoint touch, boolean distanceReached,
          int distance, Direction direction) {
    super(direction);
    this.touch = touch;
    this.distanceReached = distanceReached;
    this.distance = distance;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.google.gwt.event.shared.GwtEvent#getAssociatedType()
   */
  @Override
  public com.google.gwt.event.shared.GwtEvent.Type<SwipeMoveHandler> getAssociatedType() {
    return TYPE;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.google.gwt.event.shared.GwtEvent#dispatch(com.google.gwt.event.shared
   * .EventHandler)
   */
  @Override
  protected void dispatch(SwipeMoveHandler handler) {
    handler.onSwipeMove(this);

  }

  /**
   * the distance of this swipe
   * 
   * @return the distance of this swipe in px
   */
  public int getDistance() {
    return distance;
  }

  /**
   * is the minimum distance reached
   * 
   * @return true if the minimum distance reached
   */
  public boolean isDistanceReached() {
    return distanceReached;
  }

  public TouchPoint getTouch() {
    return touch;
  }

}
