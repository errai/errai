package org.jboss.errai.widgets.client.listeners;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import org.jboss.errai.common.client.framework.AcceptsCallback;

public class ClickCallbackListener implements ClickHandler {
    AcceptsCallback callback;
    String messageReturned;

    public ClickCallbackListener(AcceptsCallback callback, String messageReturned) {
        this.callback = callback;
        this.messageReturned = messageReturned;
    }

    public void onClick(ClickEvent event) {
        callback.callback(messageReturned, null);
    }
}