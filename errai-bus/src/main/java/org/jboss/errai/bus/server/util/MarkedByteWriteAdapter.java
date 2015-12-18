/*
 * Copyright (C) 2012 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
