package org.jboss.errai.common.metadata;

import java.io.*;
import java.security.MessageDigest;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class RebindUtils {
  public static String getClasspathHash() {
    try {
      final MessageDigest md = MessageDigest.getInstance("SHA-256");
      final String classPath = System.getProperty("java.class.path");

      for (String p : classPath.split(System.getProperty("path.separator"))) {
        _recurseDir(new File(p), new FileVisitor() {
          @Override
          public void visit(File f) {
            md.update(f.getName().getBytes());
            md.update((byte) f.lastModified());
            md.update((byte) f.length());
          }
        });
      }

      return hashToHexString(md.digest());
    }
    catch (Exception e) {
      throw new RuntimeException("failed to generate hash for classpath fingerprint", e);
    }
  }

  public static String hashToHexString(byte[] hash) {
    final StringBuilder hexString = new StringBuilder();
    for (byte mdbyte : hash) {
      hexString.append(Integer.toHexString(0xFF & mdbyte));
    }
    return hexString.toString();
  }

  public static File getErraiCacheDir() {
    String cacheDir = System.getProperty("errai.devel.debugCacheDir");
    if (cacheDir == null) cacheDir = new File(".errai/").getAbsolutePath();
    File fileCacheDir = new File(cacheDir);
    fileCacheDir.mkdirs();
    return fileCacheDir;
  }

  private static boolean nocache = Boolean.getBoolean("errai.devel.nocache");
  private static Boolean _hasClasspathChanged;

  public static boolean hasClasspathChanged() {
    if (nocache) return true;
    if (_hasClasspathChanged != null) return _hasClasspathChanged;
    File hashFile = new File(getErraiCacheDir().getAbsolutePath() + "/classpath.sha");
    String hashValue = RebindUtils.getClasspathHash();

    if (!hashFile.exists()) {
      writeStringToFile(hashFile, hashValue);
    }
    else {
      String fileHashValue = readFileToString(hashFile);
      if (fileHashValue.equals(hashValue)) {
        return _hasClasspathChanged = true;
      }
      else {
        writeStringToFile(hashFile, hashValue);
      }
    }

    return _hasClasspathChanged = false;
  }

  private static Map<Class<? extends Annotation>, Boolean> _changeMapForAnnotationScope
          = new HashMap<Class<? extends Annotation>, Boolean>();

  public static boolean hasClasspathChangedForAnnotatedWith(Set<Class<? extends Annotation>> annotations) {
    boolean result = false;
    for (Class<? extends Annotation> a : annotations) {
      /**
       * We don't terminate prematurely, because we want to cache the hashes for the next run.
       */
      if (hasClasspathChangedForAnnotatedWith(a)) result = true;
    }
    return result;
  }

  public static boolean hasClasspathChangedForAnnotatedWith(Class<? extends Annotation> annoClass) {
    if (nocache) return true;
    Boolean changed = _changeMapForAnnotationScope.get(annoClass);
    if (changed == null) {
      File hashFile = new File(getErraiCacheDir().getAbsolutePath() + "/"
              + annoClass.getName().replaceAll("\\.", "_") + ".sha");

      MetaDataScanner singleton = ScannerSingleton.getOrCreateInstance();
      String hash = singleton.getHashForTypesAnnotatedWith(annoClass);

      if (!hashFile.exists()) {
        writeStringToFile(hashFile, hash);
        changed = Boolean.TRUE;
      }
      else {
        String fileHashValue = readFileToString(hashFile);
        if (fileHashValue.equals(hash)) {
          _changeMapForAnnotationScope.put(annoClass, changed = Boolean.FALSE);
        }
        else {
          writeStringToFile(hashFile, hash);
          _changeMapForAnnotationScope.put(annoClass, changed = Boolean.TRUE);
        }
      }

    }
    return changed;
  }

  public static void writeStringToFile(File file, String data) {
    try {
      OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(file, false));
      outputStream.write(data.getBytes());
      outputStream.close();
    }
    catch (IOException e) {
      throw new RuntimeException("could not write file for debug cache", e);
    }
  }

  public static String readFileToString(File file) {
    StringBuilder buf = new StringBuilder();
    try {
      InputStream inputStream = new BufferedInputStream(new FileInputStream(file));
      byte[] b = new byte[1024];
      int read;
      while ((read = inputStream.read(b)) != -1) {
        for (int i = 0; i < read; i++) {
          buf.append((char) b[i]);
        }
      }
      inputStream.close();
    }
    catch (FileNotFoundException e) {
      throw new RuntimeException("could not read file for debug cache", e);
    }
    catch (IOException e) {
      throw new RuntimeException("could not read file for debug cache", e);
    }

    return buf.toString();
  }

  private interface FileVisitor {
    public void visit(File f);
  }

  private static void _recurseDir(File f, FileVisitor visitor) {
    if (f.isDirectory()) {
      for (File file : f.listFiles()) {
        _recurseDir(file, visitor);
      }
    }
    else {
      visitor.visit(f);
    }
  }
}

