package org.jboss.errai.bus.server.io;

import java.io.IOException;

/**
 * @author Mike Brock
 */
public abstract class AbstractByteWriteAdapter implements ByteWriteAdapter {
  @Override
  public void write(int b) throws IOException {
    write((byte) b);
  }

  @Override
  public void write(byte[] b) throws IOException {
    for (byte a : b)
      write(a);
  }
}
