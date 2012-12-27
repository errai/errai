package org.jboss.errai.bus.server.io;

import org.jboss.errai.bus.server.api.MessageQueue;

import java.io.IOException;

/**
 * Implementing this interface on a {@link MessageDeliveryHandler} indicates that the transport is an asynchronous
 * mode interface and requires explicit signalling to notify it that new data has arrived into the buffer.
 *
 * @author Mike Brock
 */
public interface Wakeable {
  public void onWake(MessageQueue queue) throws IOException;
}
