/*
 * Copyright (C) 2015 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.cdi.server.gwt.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * A utility class for copying files and directories.
 * 
 * @author Max Barkley <mbarkley@redhat.com>
 */
public class CopyUtil {

  private static final int BUF_SIZE = 1024;

  /**
   * Recursively copy a directory.
   * 
   * @param to
   *          The file or directory to be copied to. Must already exist and be
   *          the same type as from (i.e. file or directory)
   * @param from
   *          The file or directory being copied from.
   * @param filter
   *          A filter for which files to include or exclude (or null if no all
   *          files are to be copied).
   */
  public static void recursiveCopy(final File to, final File from, final Filter filter) throws IOException {
    if (from.isDirectory()) {
      for (File orig : from.listFiles()) {
        if (filter == null || filter.include(orig)) {
          if (orig.isDirectory()) {
            // Make new directory
            final File newDir = new File(to, orig.getName());
            if (!newDir.exists() && !newDir.mkdir()) {
              throw new IOException(String.format("Unable to create directory %s", newDir.getAbsolutePath()));
            }
            recursiveCopy(newDir, orig, filter);
          }
          else {
            final File newFile = new File(to, orig.getName());
            copyFile(newFile, orig);
          }
        }
      }
    }
  }

  /**
   * Copy the contents of a file.
   * 
   * @param to
   *          The file to be copied to (must already exist).
   * @param from
   *          The file to be copied from.
   */
  public static void copyFile(final File to, final File from) throws IOException {
    BufferedInputStream reader = new BufferedInputStream(new FileInputStream(from));
    BufferedOutputStream writer = new BufferedOutputStream(new FileOutputStream(to));

    copyStream(writer, reader);

    writer.close();
    reader.close();
  }

  /**
   * Copy the contents of a stream to the given file.
   * 
   * @param to
   *          A file (which may not yet exist).
   * @param from
   *          The stream from which to copy (must be open).
   */
  public static void copyStreamToFile(final File to, final InputStream from) throws IOException {
    if (!to.exists()) {
      to.createNewFile();
    }
    BufferedOutputStream writer = new BufferedOutputStream(new FileOutputStream(to));

    copyStream(writer, from);

    writer.close();
  }

  /**
   * Copy one stream to another until the input stream has no more data.
   * 
   * @param to
   *          The open stream to write to.
   * @param from
   *          The open stream to read from.
   */
  public static void copyStream(final OutputStream to, final InputStream from) throws IOException {
    final byte[] buf = new byte[BUF_SIZE];
    int len = from.read(buf);
    while (len > 0) {
      to.write(buf, 0, len);
      len = from.read(buf);
    }
  }

  /**
   * Copy a zip stream to the given directory.
   * 
   * @param dir
   *          The target directory.
   * @param from
   *          The zip input stream to copy.
   */
  public static void unzip(final File dir, final ZipInputStream from) throws IOException {
    ZipEntry entry;
    while ((entry = from.getNextEntry()) != null) {
      if (entry.isDirectory()) {
        new File(dir, entry.getName()).mkdirs();
      }
      else {
        final File file = new File(dir, entry.getName());
        file.getParentFile().mkdirs();
        file.createNewFile();
        copyStreamToFile(file, from);
        from.closeEntry();
      }
    }
  }

  /**
   * Recursively set this directory to be deleted on termination of the JVM
   * (using {@link File#deleteOnExit()}).
   * 
   * @param rootDir
   *          The directory to be deleted on exit.
   */
  public static void recursiveDeleteOnExit(File rootDir) {
    recursiveDeleteOnExitHelper(rootDir);
  }
  
  private static void recursiveDeleteOnExitHelper(File fileOrDir) {
    fileOrDir.deleteOnExit();
    if (fileOrDir.isDirectory()) {
      for (File file : fileOrDir.listFiles()) {
        recursiveDeleteOnExitHelper(file);
      }
    }
  }

  /**
   * For excluding files and folders from being copied by
   * {@link CopyUtil#recursiveCopy(File, File, Filter)}.
   * 
   * @author Max Barkley <mbarkley@redhat.com>
   */
  public static interface Filter {

    /**
     * @return Return true iff the file should be copied.
     */
    public boolean include(File orig);

  }

}
