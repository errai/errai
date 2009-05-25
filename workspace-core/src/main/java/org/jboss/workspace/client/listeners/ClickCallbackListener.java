package org.jboss.workspace.client.listeners;

import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import org.jboss.workspace.client.framework.AcceptsCallback;

public class ClickCallbackListener implements ClickHandler {
    AcceptsCallback callback;
    String messageReturned;

    public ClickCallbackListener(AcceptsCallback callback, String messageReturned) {
        this.callback = callback;
        this.messageReturned = messageReturned;
    }

    public void onClick(ClickEvent event) {
        callback.callback(messageReturned);
    }
}