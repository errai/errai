package org.jboss.errai.example.client.local;

import org.jboss.errai.bus.client.framework.Configuration;

/**
 *
 */
public class Config implements Configuration {
    @Override
    public String getRemoteLocation() {
        return "http://localhost:8080/errai-cordova/";
    }
}
