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

import com.google.gwt.dom.client.Touch;
import com.google.gwt.event.shared.GwtEvent;
import org.jboss.errai.ui.cordova.events.touch.TouchPoint;

/**
 * A {@link SwipeStartEvent} is fired when the user moves his finger over a
 * certain amount on the display
 *
 * @author Daniel Kurka
 *
 */
public class SwipeStartEvent extends SwipeEvent<SwipeStartHandler> {

	private final static GwtEvent.Type<SwipeStartHandler> TYPE = new Type<SwipeStartHandler>();
	private final int distance;
	private final TouchPoint touch;

	public static GwtEvent.Type<SwipeStartHandler> getType() {
		return TYPE;
	}

	/**
	 * Construct a {@link SwipeStartEvent}
	 *
     * @param touch the touch of the finger
     * @param distance the distance the finger already moved
	 * @param direction the direction of the finger
	 */
	public SwipeStartEvent(TouchPoint touch, int distance, Direction direction) {
		super(direction);
		this.touch = touch;
		this.distance = distance;

	}

	/*
	 * (non-Javadoc)
	 * @see com.google.gwt.event.shared.GwtEvent#getAssociatedType()
	 */
	@Override
	public com.google.gwt.event.shared.GwtEvent.Type<SwipeStartHandler> getAssociatedType() {
		return TYPE;
	}

	/**
	 * The distance the finger moved before the event occurred
	 * 
	 * @return the distance in px
	 */
	public int getDistance() {
		return distance;
	}

	/*
	 * (non-Javadoc)
	 * @see com.google.gwt.event.shared.GwtEvent#dispatch(com.google.gwt.event.shared.EventHandler)
	 */
	@Override
	protected void dispatch(SwipeStartHandler handler) {
		handler.onSwipeStart(this);

	}

	public TouchPoint getTouch() {
		return touch;
	}

}
