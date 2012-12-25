package org.jboss.errai.bus.server.io;

import org.jboss.errai.bus.server.api.MessageQueue;

/**
 * @author Mike Brock
 */
public interface Cleanable {
  public void clean(MessageQueue queue);
}
