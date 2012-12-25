package org.jboss.errai.bus.server.io;

import java.io.IOException;
import java.io.OutputStream;

/**
 * A simple wrapper implementation of {@link ByteWriteAdapter} which forwards all writes to the
 * {@link OutputStream} specified when creating the adapter.
 *
 * @author Mike Brock
 */
public class OutputStreamWriteAdapter extends AbstractByteWriteAdapter {
  private final OutputStream outputStream;

  public OutputStreamWriteAdapter(final OutputStream outputStream) {
    this.outputStream = outputStream;
  }

  @Override
  public void write(final byte b) throws IOException {
    outputStream.write(b);
  }

  @Override
  public void flush() throws IOException {
    outputStream.flush();
  }
}
