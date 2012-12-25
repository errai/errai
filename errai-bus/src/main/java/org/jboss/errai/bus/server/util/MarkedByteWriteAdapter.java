package org.jboss.errai.bus.server.util;

import org.jboss.errai.bus.server.io.AbstractByteWriteAdapter;
import org.jboss.errai.bus.server.io.ByteWriteAdapter;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author Mike Brock
 */
public class MarkedByteWriteAdapter extends AbstractByteWriteAdapter {
  private final ByteWriteAdapter writeAdapter;
  private int read;

  public MarkedByteWriteAdapter(final ByteWriteAdapter writeAdapter) {
    this.writeAdapter = writeAdapter;
  }

  @Override
  public void write(byte b) throws IOException {
    read++;
    writeAdapter.write(b);
  }

  @Override
  public void write(byte[] b) throws IOException {
    writeAdapter.write(b);
  }

  @Override
  public void flush() throws IOException {
    writeAdapter.flush();
  }

  public boolean dataWasWritten() {
    return read > 0;
  }

  public int getBytesWritten() {
    return read;
  }
}
