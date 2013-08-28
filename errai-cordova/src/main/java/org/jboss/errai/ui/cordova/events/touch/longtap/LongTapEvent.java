package org.jboss.errai.ui.cordova.events.touch.longtap;

import com.google.gwt.dom.client.Touch;
import com.google.gwt.event.shared.GwtEvent;
import org.jboss.errai.ui.cordova.events.touch.TouchPoint;

import java.util.List;

/**
 * A long tap event is produced if the user touches an area of the display for a
 * given time without moving his finger(s)
 * 
 */
public class LongTapEvent extends GwtEvent<LongTapHandler> {

	private static final Type<LongTapHandler> TYPE = new Type<LongTapHandler>();

	/**
	 * Returns the type of the event
	 * 
	 * @return the type of the event
	 */
	public static Type<LongTapHandler> getType() {
		return TYPE;
	}

	private final List<TouchPoint> startPositions;
	private final int numberOfFingers;
	private final int time;

	/**
	 * Construct a LongTapEvent
	 * 
	 * @param source - the source of the event
	 * @param numberOfFingers the number of fingers used
	 * @param time the time the fingers where touching
	 * @param startPositions the start position of each finger
	 */
	public LongTapEvent(Object source, int numberOfFingers, int time, List<TouchPoint> startPositions) {
		this.numberOfFingers = numberOfFingers;
		this.time = time;
		this.startPositions = startPositions;
		setSource(source);

	}

	/*
	 * (non-Javadoc)
	 * @see com.google.gwt.event.shared.GwtEvent#getAssociatedType()
	 */
	@Override
	public com.google.gwt.event.shared.GwtEvent.Type<LongTapHandler> getAssociatedType() {
		return TYPE;
	}

	/*
	 * (non-Javadoc)
	 * @see com.google.gwt.event.shared.GwtEvent#dispatch(com.google.gwt.event.shared.EventHandler)
	 */
	@Override
	protected void dispatch(LongTapHandler handler) {
		handler.onLongTap(this);

	}

	/**
	 * the number of fingers that created this event
	 * 
	 * @return
	 */
	public int getNumberOfFingers() {
		return numberOfFingers;
	}

	/**
	 * the start position of all fingers
	 * 
	 * @return the array of start positions
	 */
	public List<TouchPoint> getStartPositions() {
		return startPositions;
	}

	/**
	 * the time the user held the fingers
	 * 
	 * @return the time in milliseconds
	 */
	public int getTime() {
		return time;
	}

}
