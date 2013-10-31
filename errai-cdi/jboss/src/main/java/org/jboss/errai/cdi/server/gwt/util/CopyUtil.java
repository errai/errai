package org.jboss.errai.cdi.server.gwt.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

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
   *          The file or directory to be copied to. Must already exist and be the same type as from
   *          (i.e. file or directory)
   * @param from
   *          The file or directory being copied from.
   * @param filter TODO
   */
  public static void recursiveCopy(File to, File from, Filter filter) throws IOException {
    if (from.isDirectory()) {
      for (File orig : from.listFiles()) {
        if (filter.include(orig)) {
          if (orig.isDirectory()) {
            // Make new directory
            final File newDir = new File(to, orig.getName());
            if (!newDir.mkdir()) {
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
  public static void copyFile(File to, File from) throws IOException {
    BufferedInputStream reader = new BufferedInputStream(new FileInputStream(from));
    BufferedOutputStream writer = new BufferedOutputStream(new FileOutputStream(to));

    final byte[] buf = new byte[BUF_SIZE];
    int len = reader.read(buf);
    while (len > 0) {
      writer.write(buf, 0, len);
      len = reader.read(buf);
    }

    reader.close();
    writer.close();
  }
  
  /**
   * For excluding files and folders from being copied by {@link CopyUtil#recursiveCopy(File, File, Filter)}.
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
