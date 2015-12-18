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

import static java.lang.System.nanoTime;

import org.jboss.errai.bus.server.api.MessageQueue;
import org.jboss.errai.bus.server.io.buffers.BufferFilter;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;

/**
 * @author Mike Brock
 */
public final class PageUtil {
  private PageUtil() {
  }

  private static final long DOWNGRADE_THRESHOLD = Boolean.getBoolean("org.jboss.errai.debugmode") ?
      TimeUnit.SECONDS.toNanos(1600) : TimeUnit.SECONDS.toNanos(10);

  private static final String tempDir = System.getProperty("java.io.tmpdir");

  public static String getPageFileName(final MessageQueue queue) {
    return tempDir + "/queuecache/" + queue.getSession().getSessionId().replaceAll("\\-", "_");
  }

  public static File getOrCreatePageFile(final MessageQueue queue) throws IOException {
    final File pageFile = new File(getPageFileName(queue));
    if (!pageFile.exists()) {
      pageFile.getParentFile().mkdirs();
      pageFile.createNewFile();
      pageFile.deleteOnExit();
    }
    return pageFile;
  }

  public static void writeToPageFile(final MessageQueue queue, final InputStream inputStream, final boolean append) {

    try {
      final OutputStream outputStream
          = new BufferedOutputStream(new FileOutputStream(getOrCreatePageFile(queue), append));

      int read;
      while ((read = inputStream.read()) != -1) outputStream.write(read);

      outputStream.flush();
      outputStream.close();

    }
    catch (IOException e) {
      throw new RuntimeException("paging error", e);
    }
  }

  public static boolean pageWaitingToDisk(final MessageQueue queue) {
    synchronized (queue.getPageLock()) {
      try {
        final boolean alreadyPaged = queue.isPaged();

        final OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(getOrCreatePageFile(queue), alreadyPaged));
        final ByteWriteAdapter writeAdapter = new OutputStreamWriteAdapter(outputStream);

        queue.getBuffer().read(writeAdapter, queue.getBufferColor());
        outputStream.flush();
        outputStream.close();

        queue.setPaged(true);

        return alreadyPaged;
      }
      catch (IOException e) {
        throw new RuntimeException("paging error", e);
      }
    }
  }

  public static void readInPageFile(final MessageQueue queue,
                                    final ByteWriteAdapter outputStream,
                                    final BufferFilter callback) {
    synchronized (queue.getPageLock()) {
      try {
        if (queue.isPaged()) {
          final File pageFile = new File(getPageFileName(queue));
          if (!pageFile.exists()) {
            queue.setPaged(false);
            return;
          }

          final InputStream inputStream = new BufferedInputStream(new FileInputStream(pageFile));

          callback.before(outputStream);

          int read;
          while ((read = inputStream.read()) != -1) {
            outputStream.write(callback.each(read, outputStream));
          }
          inputStream.close();

          callback.after(outputStream);

          queue.setPaged(false);
        }
      }
      catch (Exception e) {
        throw new RuntimeException("paging error", e);
      }
    }
  }

  public static void discardPageData(final MessageQueue queue) {
    if (queue.isPaged()) {
      File pageFile = new File(getPageFileName(queue));
      if (pageFile.exists()) {
        pageFile.delete();
      }
    }
  }

  public static boolean pageIfStraddling(final MessageQueue queue) {
    if (queue.getDeliveryHandler() instanceof Pageable) {

      if (((nanoTime() - queue.getLastTransmissionTime()) > DOWNGRADE_THRESHOLD)) {
        ((Pageable) queue.getDeliveryHandler()).pageOut(queue);
        return true;
      }
    }
    return false;
  }
}
