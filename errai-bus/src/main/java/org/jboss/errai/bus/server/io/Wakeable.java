package org.jboss.errai.bus.server.io;

import org.jboss.errai.bus.server.api.MessageQueue;

import java.io.IOException;

/**
 * @author Mike Brock
 */
public interface Wakeable {
  public void onWake(MessageQueue queue) throws IOException;
}
