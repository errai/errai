package org.jboss.errai.bus.server.io;

import java.io.IOException;

/**
 * @author Mike Brock
 */
public interface ByteWriteAdapter {
  public void write(int b) throws IOException;
  public void write(byte b) throws IOException;
  public void write(byte[] b) throws IOException;
  public void flush() throws IOException;
}
