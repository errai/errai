package org.jboss.errai.bus.server.io;

import org.jboss.errai.bus.server.api.MessageQueue;

import java.io.IOException;

/**
 * @author Mike Brock
 */
public interface Buffered {
  public boolean copyFromBuffer(boolean waitForData, MessageQueue queue, ByteWriteAdapter toAdapter) throws IOException;
}
