package org.reflections.vfs;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import com.google.common.collect.AbstractIterator;
import org.jboss.vfs.VFS;
import org.jboss.vfs.VirtualFile;
import org.reflections.vfs.Vfs;
import org.reflections.vfs.Vfs.Dir;
import org.reflections.vfs.Vfs.UrlType;

/**
 * UrlType to be used by Reflections library for JBoss 6.
 * <p/>
 * See: http://code.google.com/p/reflections/issues/detail?id=27
 *
 * @author szimano
 */
public class JBoss6UrlType implements UrlType {
    final String VFS_PROTOCOL = "vfs";

    /*@Override*/
    public boolean matches(URL url) {
        return url.getProtocol().equals(VFS_PROTOCOL);
    }

    /*@Override*/
    public Dir createDir(URL url) {
        try {
            VirtualFile file = VFS.getChild(url);

            if (!file.isDirectory()) {
                throw new RuntimeException("URL " + url + " doesn't point to a Directory");
            }

            return new JBoss6Dir(file);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}

class JBoss6Dir implements Vfs.Dir {

    private VirtualFile parent;

    public JBoss6Dir(VirtualFile parent) {
        this.parent = parent;
    }

    /*@Override*/
    public String getPath() {
        return parent.getPathName();
    }

    /*@Override*/
    public Iterable<Vfs.File> getFiles() {
        return new Iterable<Vfs.File>() {
            public Iterator<Vfs.File> iterator() {
                return new AbstractIterator<Vfs.File>() {
                    final Stack<VirtualFile> stack = new Stack<VirtualFile>();
                    {stack.addAll(listFiles(parent));}

                    protected Vfs.File computeNext() {
                        while (!stack.isEmpty()) {
                            final VirtualFile file = stack.pop();
                            if (file.isDirectory()) {
                                stack.addAll(listFiles(file));
                            } else {
                                return new JBoss6File(parent, file);
                            }
                        }

                        return endOfData();
                    }
                };
            }
        };
    }

    private List<VirtualFile> listFiles(VirtualFile file) {
        return file.getChildren();
    }

    /*@Override*/
    public void close() {
    }
}

class JBoss6File implements Vfs.File {
    private VirtualFile file;
    private VirtualFile parent;

    public JBoss6File(VirtualFile parent, VirtualFile file) {
        this.file = file;
        this.parent = parent;
    }


    /*@Override*/
    public String getName() {
        return file.getName();
    }

    /*@Override*/
    public String getRelativePath() {
        return file.getPathNameRelativeTo(parent);
    }

    /*@Override*/
    public String getFullPath() {
        return file.getPathName();
    }

    /*@Override*/
    public InputStream openInputStream() throws IOException {
        return file.openStream();
    }
}

