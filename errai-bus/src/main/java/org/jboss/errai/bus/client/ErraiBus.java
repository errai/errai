package org.jboss.errai.bus.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;

public class ErraiBus implements EntryPoint {
    private static MessageBus bus = GWT.create(MessageBus.class);

    public static MessageBus get() {
        return bus;
    }

    public void onModuleLoad() {
    }
}
