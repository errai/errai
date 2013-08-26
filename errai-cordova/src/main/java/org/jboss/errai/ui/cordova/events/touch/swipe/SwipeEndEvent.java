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

import com.google.gwt.event.shared.GwtEvent;

/**
 * A {@link SwipeEndEvent} occurs when the user lifts his finger of the display
 *
 * @author Daniel Kurka
 *
 */
public class SwipeEndEvent extends SwipeEvent<SwipeEndHandler> {

	private final static GwtEvent.Type<SwipeEndHandler> TYPE = new Type<SwipeEndHandler>();
	private final boolean distanceReached;
	private final int distance;

	public static GwtEvent.Type<SwipeEndHandler> getType() {
		return TYPE;
	}

	/**
	 * Construct a swipe end event
	 *
	 * @param distanceReached was the minumum distance reached
	 * @param distance the distance that was covered by the finger
	 * @param direction the direction of the swipe
	 */
	public SwipeEndEvent(boolean distanceReached, int distance, Direction direction) {
		super(direction);
		this.distanceReached = distanceReached;
		this.distance = distance;
	}

	/*
	 * (non-Javadoc)
	 * @see com.google.gwt.event.shared.GwtEvent#getAssociatedType()
	 */
	@Override
	public com.google.gwt.event.shared.GwtEvent.Type<SwipeEndHandler> getAssociatedType() {
		return TYPE;
	}

	/*
	 * (non-Javadoc)
	 * @see com.google.gwt.event.shared.GwtEvent#dispatch(com.google.gwt.event.shared.EventHandler)
	 */
	@Override
	protected void dispatch(SwipeEndHandler handler) {
		handler.onSwipeEnd(this);

	}

	/**
	 * the distance the finger has covered in px
	 * 
	 * @return the distance in px
	 */
	public int getDistance() {
		return distance;
	}

	/**
	 * is the minimum distance reached by this swipe
	 * 
	 * @return true if minimum distance was reached
	 */
	public boolean isDistanceReached() {
		return distanceReached;
	}

}
