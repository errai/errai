package org.errai.samples.helloworld.client;

import org.jboss.errai.bus.server.annotations.ExposeEntity;

@ExposeEntity
public class Messager {
    private long time;
    private String message;

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
