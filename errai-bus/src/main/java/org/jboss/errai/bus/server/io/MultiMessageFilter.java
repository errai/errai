/*
 * Copyright 2012 JBoss, by Red Hat, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
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
 * <p>
 * For instance, consider the following buffer data:
 * <p>
 * <code>
 * {"foo":"bar"}{"bar":"foo"}
 * </code>
 * <p>
 * This filter will transform this to:
 * <p>
 * <code>
 * [{"foo":"bar"},{"bar":"foo"}]
 * </code>

 * @author Mike Brock
 */
public class MultiMessageFilter implements BufferFilter {
  private int bracketCount;
  private int seg;

  @Override
  public void before(final ByteWriteAdapter writer) throws IOException {
    writer.write('[');
  }

  @Override
  public int each(final int i, final ByteWriteAdapter writer) throws IOException {
    if (i == '{' && ++bracketCount == 1 && seg != 0) {
      writer.write(',');
    }
    else if (i == '}' && bracketCount != 0 && --bracketCount == 0) {
      seg++;
    }
    return i;
  }

  @Override
  public void after(final ByteWriteAdapter writer) throws IOException {
    if (bracketCount == 1) {
      writer.write('}');
    }
    writer.write(']');
  }
}
