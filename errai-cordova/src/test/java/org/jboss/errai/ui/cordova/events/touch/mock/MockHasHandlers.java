package org.jboss.errai.ui.cordova.events.touch.mock;

import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;

public class MockHasHandlers implements HasHandlers {

	private GwtEvent<?> event;

	@Override
	public void fireEvent(GwtEvent<?> event) {
		this.event = event;

	}

	public GwtEvent<?> getEvent() {
		return event;
	}

}
