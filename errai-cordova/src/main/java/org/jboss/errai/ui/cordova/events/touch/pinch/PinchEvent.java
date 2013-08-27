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
package org.jboss.errai.ui.cordova.events.touch.pinch;

import com.google.gwt.event.shared.GwtEvent;

/**
 * A {@link PinchEvent} is fired when a user moves to finger on the display.
 *
 * A pinch event is fired around a center point which is calculated by looking
 * at the two fingers producing the event.
 *
 * <p>
 * if finger one is at x1, y1 and finger two is at x2, y2 the center point is
 * (x1 + x2) / 2 and (y1 + y2) / 2
 * </p>
 *
 *
 * @author Daniel Kurka
 *
 */
public class PinchEvent extends GwtEvent<PinchHandler> {

	private static final GwtEvent.Type<PinchHandler> TYPE = new Type<PinchHandler>();
	private final int x;
	private final int y;
	private final double scaleFactor;

	public static GwtEvent.Type<PinchHandler> getType() {
		return TYPE;
	}

	/**
	 * Construct a pinch event
	 *
	 * @param x the mid point of the pinch in x
	 * @param y the mid point of the pinch in y
	 * @param scaleFactor the new scaling factor
	 */
	public PinchEvent(int x, int y, double scaleFactor) {
		this.x = x;
		this.y = y;
		this.scaleFactor = scaleFactor;
	}

	/*
	 * (non-Javadoc)
	 * @see com.google.gwt.event.shared.GwtEvent#getAssociatedType()
	 */
	@Override
	public com.google.gwt.event.shared.GwtEvent.Type<PinchHandler> getAssociatedType() {
		return TYPE;
	}

	/*
	 * (non-Javadoc)
	 * @see com.google.gwt.event.shared.GwtEvent#dispatch(com.google.gwt.event.shared.EventHandler)
	 */
	@Override
	protected void dispatch(PinchHandler handler) {
		handler.onPinch(this);

	}

	/**
	 * The x position of the center point of the pinch.
	 * 
	 * 
	 * @return the x position
	 */
	public int getX() {
		return x;
	}

	/**
	 * The y position of the center point of the pinch.
	 * 
	 * 
	 * @return the y position
	 */
	public int getY() {
		return y;
	}

	/**
	 * the new scale factor that can be applied for getting a zoom effect
	 * 
	 * @return the scale factor
	 */
	public double getScaleFactor() {
		return scaleFactor;
	}

}
