package org.jboss.errai.bus.client;

import com.google.gwt.core.client.GWT;


public class ErraiClient {
    private static MessageBus bus = GWT.create(MessageBus.class);

    public static MessageBus getBus() {
        return bus;
    }
}

