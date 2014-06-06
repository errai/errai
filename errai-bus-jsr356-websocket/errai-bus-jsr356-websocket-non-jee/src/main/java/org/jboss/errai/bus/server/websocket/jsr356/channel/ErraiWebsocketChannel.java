package org.jboss.errai.bus.server.websocket.jsr356.channel;

import org.jboss.errai.bus.server.io.QueueChannel;

/**
 * @author : Michel Werren
 */
public interface ErraiWebsocketChannel extends QueueChannel {

    public void doErraiMessage(String message);

    public void onSessionClosed();
}
