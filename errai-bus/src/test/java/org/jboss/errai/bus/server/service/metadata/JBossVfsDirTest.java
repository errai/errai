package org.jboss.errai.bus.server.service.metadata;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;

import org.jboss.vfs.VFS;
import org.jboss.vfs.VirtualFile;
import org.jboss.vfs.protocol.VfsUrlStreamHandlerFactory;
import org.jboss.vfs.spi.RealFileSystem;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Unit testing for Errai's glue between the JBoss VFS and the Reflections VFS.
 * 
 * @author Jonathan Fuerth <jfuerth@redhat.com>
 */
public class JBossVfsDirTest {

  /**
   * A new directory created and mounted into the JBoss VFS at {
   * {@link #getJBossVfsMountPoint()} during setup. Test methods are free to
   * create and delete entries under this directory at their whim. The whole
   * directory will be deleted in {@link #teardown()}.
   */
  private File mountedDir;
  
  /**
   * Handle for unmounting mountedDir at test teardown time.
   */
  private Closeable mount;

  @BeforeClass
  public static void setURLStreamHandlerFactory() {
    try {
      java.net.URL.setURLStreamHandlerFactory(new VfsUrlStreamHandlerFactory());
    } catch (Error e) {
      System.out.println();
      System.out.println("WARNING: something else has already set the URL stream handler factory.");
      System.out.println("         These tests are likely to fail on resolving vfs: URLs.");
      System.out.println();
    }
  }
  
  @Before
  public void setup() throws IOException {
    mountedDir = new File(System.getProperty("java.io.tmpdir"), "JBossVFSDirTest_" + System.currentTimeMillis());
    mountedDir.mkdirs();
    RealFileSystem mountedDirVfs = new RealFileSystem(mountedDir);
    mount = VFS.mount(getJBossVfsMountPoint(), mountedDirVfs);
  }

  private VirtualFile getJBossVfsMountPoint() {
    return VFS.getRootVirtualFile().getChild("mnt");
  }

  @After
  public void teardown() throws IOException {
    mount.close();
    areEmDashAreEff(mountedDir);
  }
  
  @Test
  public void testEmptyVfsDirListing() throws Exception {
    JBossVfsDir jbvd = new JBossVfsDir(getJBossVfsMountPoint().asDirectoryURL());
    int count = 0;
    for (org.reflections.vfs.Vfs.File reflectionsFile : jbvd.getFiles()) {
      System.out.println("Visiting virtual file " + reflectionsFile.getRelativePath());
      count++;
    }
    assertEquals("Didn't find the expected number of VFS entries", 0, count);
  }

  @Test
  public void testShallowVfsDirListing() throws Exception {
    new File(mountedDir, "foo").createNewFile();
    new File(mountedDir, "bar").createNewFile();
    new File(mountedDir, "baz").createNewFile();
    
    JBossVfsDir jbvd = new JBossVfsDir(getJBossVfsMountPoint().asDirectoryURL());
    int count = 0;
    for (org.reflections.vfs.Vfs.File reflectionsFile : jbvd.getFiles()) {
      System.out.println("Visiting virtual file " + reflectionsFile.getRelativePath());
      count++;
    }
    assertEquals("Didn't find the expected number of VFS entries", 3, count);
  }

  /**
   * Regression test for ERRAI-163.
   */
  @Test
  public void testNestedEmptyVfsDirListing() throws Exception {
    new File(mountedDir, "deeply/nested/directories").mkdirs();
    
    JBossVfsDir jbvd = new JBossVfsDir(getJBossVfsMountPoint().asDirectoryURL());
    int count = 0;
    for (org.reflections.vfs.Vfs.File reflectionsFile : jbvd.getFiles()) {
      System.out.println("Visiting virtual file " + reflectionsFile.getRelativePath());
      count++;
    }
    assertEquals("All dirs were empty, but got non-zero count", 0, count);
  }
  
  /**
   * Simple "rm -rf" equivalent, implemented for the 12,433rd time in Java.
   * 
   * @param deleteMe
   *          The directory to delete. This directory itself and all contents
   *          will be removed from the filesystem.
   */
  private static void areEmDashAreEff(File deleteMe) {
    for (File child : deleteMe.listFiles()) {
      if (child.isDirectory()) {
        areEmDashAreEff(child);
      }
      else {
        child.delete();
      }
    }
    deleteMe.delete();
  }
  
  @Test
  public void testRejectNonJBossVfsUrl() throws Exception {
    try {
      new JBossVfsDir(new File(System.getProperty("java.io.tmpdir")).toURI().toURL());
      fail("Shouldn't have been able to create JBossVfsDir from regular file: URL");
    } catch (IllegalArgumentException ex) {
      // expected outcome
    }
  }

  @Test
  public void testVfsFullPath() throws Exception {
    new File(mountedDir, "foo").createNewFile();

    JBossVfsDir jbvd = new JBossVfsDir(getJBossVfsMountPoint().asDirectoryURL());
    String path = jbvd.getFiles().iterator().next().getFullPath();
    assertTrue("Wrong path:" + path, path.matches("/([A-Za-z]:/)?mnt/foo"));
  }

  @Test
  public void testVfsRelativePath() throws Exception {
    new File(mountedDir, "foo").createNewFile();

    JBossVfsDir jbvd = new JBossVfsDir(getJBossVfsMountPoint().asDirectoryURL());
    String path = jbvd.getFiles().iterator().next().getRelativePath();
    assertTrue("Wrong path:" + path, path.matches("/([A-Za-z]:/)?mnt/foo"));
  }
  @Test
  public void testVfsFileName() throws Exception {
    new File(mountedDir, "foo").createNewFile();

    JBossVfsDir jbvd = new JBossVfsDir(getJBossVfsMountPoint().asDirectoryURL());
    assertEquals("foo", jbvd.getFiles().iterator().next().getName());
  }
}
