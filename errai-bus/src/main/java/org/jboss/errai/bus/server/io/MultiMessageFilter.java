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

package org.jboss.errai.bus.server.io;

import org.jboss.errai.bus.server.io.buffers.BufferFilter;

import java.io.IOException;
import java.io.OutputStream;

/**
 * This {@link BufferFilter} is used for re-writing the data in the buffer to permit properly formed
 * multi-message payloads. This is because ErraiBus J.REP messages are delivered into the bus as
 * single-message payloads. This filter prepends '<tt>[</tt>' to the data coming from the buffer,
 * and inserts a '<tt>,</tt>' between each message in the stream. It then appends a '<tt>]</tt>' to the
 * message to complete the payload.
 * <p/>
 * For instance, consider the following buffer data:
 * <p/>
 * <code>
 * {"foo":"bar"}{"bar":"foo"}
 * </code>
 * <p/>
 * This filter will transform this to:
 * <p/>
 * <code>
 * [{"foo":"bar"},{"bar":"foo"}]
 * </code>
 *
 * @author Mike Brock
 */
public class MultiMessageFilter implements BufferFilter {
  int brackCount;
  int seg;
  boolean inString;
  boolean escape;

  @Override
  public void before(final ByteWriteAdapter outstream) throws IOException {
    outstream.write('[');
  }

  @Override
  public int each(int i, final ByteWriteAdapter outstream) throws IOException {
    if (inString) {
      if (escape) {
        escape = false;
      }
      else if (i == '\\') {
        escape = true;
      }
      else if (i == '"') {
        inString = false;
      }
    }
    else {
      if (i == '"') {
        inString = true;
      }
      else if (i == '{' && ++brackCount == 1 && seg != 0) {
        outstream.write(',');
      }
      else if (i == '}' && brackCount != 0 && --brackCount == 0) {
        seg++;
      }
    }
    return i;
  }

  @Override
  public void after(final ByteWriteAdapter outstream) throws IOException {
    if (brackCount == 1) {
      outstream.write('}');
    }
    outstream.write(']');
  }
}
