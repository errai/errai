package org.jboss.errai.bus.server.io;

import org.jboss.errai.bus.server.api.MessageQueue;

/**
 * @author Mike Brock
 */
public interface Pageable {
  public boolean pageOut(MessageQueue queue);
  public void discardPageData(MessageQueue queue);
}
