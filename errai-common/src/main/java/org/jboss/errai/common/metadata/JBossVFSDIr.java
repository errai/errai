package org.jboss.errai.common.metadata;

import org.jboss.vfs.VirtualFile;
import org.reflections.vfs.Vfs;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Implementation of {@link Vfs.Dir} to support JBoss VFS for proper classpath scanning on JBoss AS.
 *
 * @author Mike Brock <cbrock@redhat.com>
 */
public class JBossVFSDIr implements Vfs.Dir {
  private VirtualFile virtualFile;

  public JBossVFSDIr(URL url) {
    try {
      Object content = url.getContent();
      if (content instanceof VirtualFile) {
        virtualFile = (VirtualFile) content;
      }
      else {
        throw new RuntimeException("URL content is not a JBoss VFS VirtualFile. Type is: " + (content == null ?
                "null" : content.getClass().getName()));
      }
    }
    catch (IOException e) {
      throw new RuntimeException("could not instantiate VFS directory", e);
    }
  }

  @Override
  public String getPath() {
    return virtualFile.getPathName();
  }

  @Override
  public Iterable<Vfs.File> getFiles() {
    return new Iterable<Vfs.File>() {
      @Override
      public Iterator<Vfs.File> iterator() {
        final ArrayList<VirtualFile> toVisit = new ArrayList<VirtualFile>();

        {
          toVisit.addAll(virtualFile.getChildren());
        }

        return new Iterator<Vfs.File>() {

          @Override
          public boolean hasNext() {
            return !toVisit.isEmpty();
          }

          @Override
          public Vfs.File next() {
            final VirtualFile file = toVisit.remove(toVisit.size() - 1);

            if (file.isDirectory()) {
              toVisit.addAll(file.getChildren());
              return next();
            }

            return new Vfs.File() {
              @Override
              public String getName() {
                return file.getName();
              }

              @Override
              public String getRelativePath() {
                return file.getPathName();
              }

              @Override
              public String getFullPath() {
                return file.getPathName();
              }

              @Override
              public InputStream openInputStream() throws IOException {
                return file.openStream();
              }
            };
          }

          @Override
          public void remove() {
            throw new IllegalAccessError();
          }
        };
      }
    };
  }

  @Override
  public void close() {
  }
}
