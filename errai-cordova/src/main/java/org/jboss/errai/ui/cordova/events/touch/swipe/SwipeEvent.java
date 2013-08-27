package org.jboss.errai.ui.cordova.events.touch.swipe;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

/**
 * base class for all swipte events
 * 
 * @author Daniel Kurka
 * 
 * @param <H> Handler type of the event
 */
public abstract class SwipeEvent<H extends EventHandler> extends GwtEvent<H> {

	public enum Direction {
		LEFT_TO_RIGHT, RIGHT_TO_LEFT, TOP_TO_BOTTOM, BOTTOM_TO_TOP
    }

	private final Direction direction;

	/**
	 * Construct a swipe event with a given direction
	 * 
	 * @param direction the direction to use
	 */
	public SwipeEvent(Direction direction) {
		this.direction = direction;

	}

	/**
	 * the direction of the event
	 * 
	 * @return the direction
	 */
	public Direction getDirection() {
		return direction;
	}

}
