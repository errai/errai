package org.jboss.errai.common.metadata;

import com.google.common.io.Files;
import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.typeinfo.JPackage;
import com.google.gwt.dev.cfg.ModuleDef;
import com.google.gwt.dev.javac.StandardGeneratorContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * @author Mike Brock <cbrock@redhat.com>
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class RebindUtils {

  static Logger logger = LoggerFactory.getLogger(RebindUtils.class);
  private static String hashSeed = "errai21CR2";
  private final static Pattern erraiCommonJarFinder = Pattern.compile(".*/errai\\-common.*\\.jar!/META-INF/MANIFEST.MF");

  private static volatile String _tempDirectory;

  public static String getTempDirectory() {
    if (_tempDirectory != null) {
      return _tempDirectory;
    }

    final File file = new File(System.getProperty("java.io.tmpdir") + "/errai/" + getClasspathHash() + "/");

    if (!file.exists()) {
      file.mkdirs();
    }

    return _tempDirectory = file.getAbsolutePath();
  }

  private static volatile String _classpathHashCache;

  private static final String[] hashableExtensions = {".java", ".class", ".properties", ".xml"};

  private static boolean isValidFileType(final String fileName) {
    for (String extension : hashableExtensions) {
      if (fileName.endsWith(extension)) return true;
    }
    return false;
  }

  public static String getClasspathHash() {
    if (_hasClasspathChanged != null) {
      return _classpathHashCache;
    }

    try {
      final MessageDigest md = MessageDigest.getInstance("SHA-1");
      final String classPath = System.getProperty("java.class.path");

      md.update(hashSeed.getBytes());

      for (final String p : classPath.split(System.getProperty("path.separator"))) {
        _recurseDir(new File(p), new FileVisitor() {
          @Override
          public void visit(final File f) {
            final String fileName = f.getName();
            if (isValidFileType(fileName)) {
              md.update(fileName.getBytes());
              final long lastModified = f.lastModified();
//              md.update((byte) ((lastModified >> 56 & 0xFF)));
//              md.update((byte) ((lastModified >> 48 & 0xFF)));
//              md.update((byte) ((lastModified >> 40 & 0xFF)));
//              md.update((byte) ((lastModified >> 32 & 0xFF)));
              md.update((byte) ((lastModified >> 24 & 0xFF)));
              md.update((byte) ((lastModified >> 16 & 0xFF)));
              md.update((byte) ((lastModified >> 8 & 0xFF)));
              md.update((byte) ((lastModified & 0xFF)));

              final long length = f.length();
//
//              md.update((byte) ((length >> 56 & 0xFF)));
//              md.update((byte) ((length >> 48 & 0xFF)));
//              md.update((byte) ((length >> 40 & 0xFF)));
//              md.update((byte) ((length >> 32 & 0xFF)));
              md.update((byte) ((length >> 24 & 0xFF)));
              md.update((byte) ((length >> 16 & 0xFF)));
              md.update((byte) ((length >> 8 & 0xFF)));
              md.update((byte) ((length & 0xFF)));
            }
          }
        });
      }

      return _classpathHashCache = hashToHexString(md.digest());
    }
    catch (Exception e) {
      throw new RuntimeException("failed to generate hash for classpath fingerprint", e);
    }
  }

  public static String hashToHexString(final byte[] hash) {
    final StringBuilder hexString = new StringBuilder();
    for (final byte b : hash) {
      hexString.append(Integer.toHexString(0xFF & b));
    }
    return hexString.toString();
  }

  public static File getErraiCacheDir() {
    String cacheDir = System.getProperty("errai.devel.debugCacheDir");
    if (cacheDir == null) cacheDir = new File(".errai/").getAbsolutePath();
    final File fileCacheDir = new File(cacheDir);
    //noinspection ResultOfMethodCallIgnored
    fileCacheDir.mkdirs();
    return fileCacheDir;
  }

  public static File getCacheFile(final String name) {
    return new File(getErraiCacheDir(), name).getAbsoluteFile();
  }

  public static boolean cacheFileExists(final String name) {
    return getCacheFile(name).exists();
  }

  private static boolean nocache = Boolean.getBoolean("errai.devel.nocache");
  private static Boolean _hasClasspathChanged;

  public static boolean hasClasspathChanged() {
    if (nocache) return true;
    if (_hasClasspathChanged != null) return _hasClasspathChanged;
    final File hashFile = new File(getErraiCacheDir().getAbsolutePath() + "/classpath.sha");
    final String hashValue = RebindUtils.getClasspathHash();

    if (!hashFile.exists()) {
      writeStringToFile(hashFile, hashValue);
    }
    else {
      final String fileHashValue = readFileToString(hashFile);
      if (!fileHashValue.equals(hashValue)) {
        writeStringToFile(hashFile, hashValue);
        return _hasClasspathChanged = true;
      }
    }

    return _hasClasspathChanged = false;
  }

  private static Map<Class<? extends Annotation>, Boolean> _changeMapForAnnotationScope
      = new HashMap<Class<? extends Annotation>, Boolean>();

  public static boolean hasClasspathChangedForAnnotatedWith(final Set<Class<? extends Annotation>> annotations) {
    if (Boolean.getBoolean("errai.devel.forcecache")) return true;

    boolean result = false;
    for (final Class<? extends Annotation> a : annotations) {
      /**
       * We don't terminate prematurely, because we want to cache the hashes for the next run.
       */
      if (hasClasspathChangedForAnnotatedWith(a)) result = true;
    }


    return result;
  }

  public static boolean hasClasspathChangedForAnnotatedWith(final Class<? extends Annotation> annoClass) {
    if (nocache) return true;
    Boolean changed = _changeMapForAnnotationScope.get(annoClass);
    if (changed == null) {
      final File hashFile = new File(getErraiCacheDir().getAbsolutePath() + "/"
          + annoClass.getName().replaceAll("\\.", "_") + ".sha");

      final MetaDataScanner singleton = ScannerSingleton.getOrCreateInstance();
      final String hash = singleton.getHashForTypesAnnotatedWith(hashSeed, annoClass);

      if (!hashFile.exists()) {
        writeStringToFile(hashFile, hash);
        changed = Boolean.TRUE;
      }
      else {
        final String fileHashValue = readFileToString(hashFile);
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

  public static void writeStringToFile(final File file, final String data) {
    try {
      final OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(file, false));
      outputStream.write(data.getBytes("UTF-8"));
      outputStream.close();
    }
    catch (IOException e) {
      throw new RuntimeException("could not write file for debug cache", e);
    }
  }

  public static String readFileToString(final File file) {
    try {
      return Files.toString(file, Charset.forName("UTF-8"));
    }
    catch (IOException e) {
      throw new RuntimeException("could not read file for debug cache", e);
    }
  }

  public static String packageNameToDirName(final String pkg) {
    final StringBuilder sb = new StringBuilder();
    for (int i = 0; i < pkg.length(); i++) {
      if (pkg.charAt(i) == '.') {
        sb.append(File.separator);
      }
      else {
        sb.append(pkg.charAt(i));
      }
    }
    return sb.toString();
  }

  private interface FileVisitor {
    public void visit(File f);
  }

  private static void _recurseDir(final File f, final FileVisitor visitor) {
    if (f.isDirectory()) {
      for (final File file : f.listFiles()) {
        _recurseDir(file, visitor);
      }
    }
    else {
      visitor.visit(f);
    }
  }

  private static final String[] moduleRootExclusions = {"target/", "out/", "build/", "src/", "war/", "exploded/"};

  public static String guessWorkingDirectoryForModule(final GeneratorContext context) {
    if (context == null) {
      logger.warn("could not determine module location, using CWD (no context)");
      return new File("").getAbsolutePath() + "/";
    }
    try {
      final List<URL> configUrls = MetaDataScanner.getConfigUrls();
      final Set<String> candidateRoots = new HashSet<String>();
      final String workingDir = new File("").getAbsolutePath();

      Pathcheck:
      for (final URL url : configUrls) {
        String filePath = url.getFile();
        if (filePath.startsWith(workingDir) && filePath.indexOf('!') == -1) {
          final int start = workingDir.length() + 1;
          int firstSubDir = -1;
          for (int i = start; i < filePath.length(); i++) {
            if (filePath.charAt(i) == File.separatorChar) {
              firstSubDir = i;
              break;
            }
          }

          if (firstSubDir != -1) {
            filePath = filePath.substring(start, firstSubDir) + "/";

            for (final String excl : moduleRootExclusions) {
              if (filePath.startsWith(excl)) continue Pathcheck;
            }

            candidateRoots.add(workingDir + "/" + filePath);
          }
        }
      }

      if (candidateRoots.isEmpty()) {
        logger.warn("could not determine module location, using CWD");
        return new File("").getAbsolutePath() + "/";
      }
      else if (candidateRoots.size() != 1) {
        for (final String res : candidateRoots) {
          logger.warn(" Multiple Possible Roots for Project -> " + res);
        }

        throw new RuntimeException("ambiguous module locations for GWT module (specify path property for module)");
      }
      else {
        return candidateRoots.iterator().next();
      }
    }
    catch (Exception e) {
      throw new RuntimeException("could not determine module package", e);

    }
  }


  public static String getModuleName(final GeneratorContext context) {
    try {
      final StandardGeneratorContext standardGeneratorContext =
          (StandardGeneratorContext) context;
      final Field field = StandardGeneratorContext.class.getDeclaredField("module");
      field.setAccessible(true);
      final ModuleDef moduleDef = (ModuleDef) field.get(standardGeneratorContext);
      return moduleDef.getCanonicalName();
    }
    catch (Throwable t) {
      return null;
    }
  }


  /**
   * Returns the list of translatable packages in the module that caused the generator to run (the module under compilation).
   */
  public static Set<String> findTranslatablePackagesInModule(final GeneratorContext context) {
    final Set<String> packages = new HashSet<String>();
    try {
      final StandardGeneratorContext stdContext = (StandardGeneratorContext) context;
      final Field field = StandardGeneratorContext.class.getDeclaredField("module");
      field.setAccessible(true);
      final Object o = field.get(stdContext);

      final ModuleDef moduleDef = (ModuleDef) o;

      if (moduleDef == null) {
        return Collections.emptySet();
      }

      // moduleName looks like "com.foo.xyz.MyModule" and we just want the package part
      // for tests .JUnit is appended to the module name by GWT
      final String moduleName = moduleDef.getCanonicalName().replace(".JUnit", "");
      final String modulePackage = moduleName.substring(0, moduleName.lastIndexOf('.'));

      for (final String packageName : findTranslatablePackages(context)) {
        if (packageName != null && packageName.startsWith(modulePackage)) {
          packages.add(packageName);
        }
      }
    }
    catch (NoSuchFieldException e) {
      logger.error("the version of GWT you are running does not appear to be compatible with this version of Errai", e);
      throw new RuntimeException("could not access the module field in the GeneratorContext");
    }
    catch (Exception e) {
      throw new RuntimeException("could not determine module package", e);
    }

    return packages;
  }

  private static volatile GeneratorContext _lastTranslatableContext;
  private static volatile Set<String> _translatablePackagesCache;

  /**
   * Returns a list of all translatable packages accessible to the module under compilation (including inherited modules).
   */
  public static Set<String> findTranslatablePackages(final GeneratorContext context) {
    if (context.equals(_lastTranslatableContext) && _translatablePackagesCache != null) {
      return _translatablePackagesCache;
    }
    _lastTranslatableContext = context;

    final JPackage[] jPackages = context.getTypeOracle().getPackages();
    final Set<String> packages = new HashSet<String>(jPackages.length * 2);
    for (final JPackage p : jPackages) {
      packages.add(p.getName());
    }

    return _translatablePackagesCache = Collections.unmodifiableSet(packages);
  }
}