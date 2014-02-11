package org.jboss.errai.common.metadata;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.google.common.io.Files;
import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.typeinfo.JPackage;
import com.google.gwt.dev.cfg.ModuleDef;
import com.google.gwt.dev.javac.StandardGeneratorContext;

/**
 * @author Mike Brock <cbrock@redhat.com>
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class RebindUtils {
  static Logger logger = LoggerFactory.getLogger(RebindUtils.class);
  private static String hashSeed = "errai21CR2";

  private static volatile String _tempDirectory;

  public static String getTempDirectory() {
    if (_tempDirectory != null) {
      return _tempDirectory;
    }

    final String useramePortion = System.getProperty("user.name").replaceAll("[^0-9a-zA-Z]", "-");
    final File file =
        new File(System.getProperty("java.io.tmpdir") + "/" + useramePortion + "/errai/" + getClasspathHash() + "/");

    if (!file.exists()) {
      // noinspection ResultOfMethodCallIgnored
      file.mkdirs();
    }

    return _tempDirectory = file.getAbsolutePath();
  }

  private static volatile String _classpathHashCache;

  private static final String[] hashableExtensions = { ".java", ".class", ".properties", ".xml" };

  private static boolean isValidFileType(final String fileName) {
    for (final String extension : hashableExtensions) {
      if (fileName.endsWith(extension))
        return true;
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

      final Enumeration<URL> resources = Thread.currentThread().getContextClassLoader().getResources("");

      md.update(hashSeed.getBytes());

      for (final String p : classPath.split(System.getProperty("path.separator"))) {
        _recurseDir(new File(p), new FileVisitor() {
          @Override
          public void visit(final File f) {
            final String fileName = f.getName();
            if (isValidFileType(fileName)) {
              md.update(fileName.getBytes());
              final long lastModified = f.lastModified();
              // md.update((byte) ((lastModified >> 56 & 0xFF)));
              // md.update((byte) ((lastModified >> 48 & 0xFF)));
              // md.update((byte) ((lastModified >> 40 & 0xFF)));
              // md.update((byte) ((lastModified >> 32 & 0xFF)));
              md.update((byte) ((lastModified >> 24 & 0xFF)));
              md.update((byte) ((lastModified >> 16 & 0xFF)));
              md.update((byte) ((lastModified >> 8 & 0xFF)));
              md.update((byte) ((lastModified & 0xFF)));

              final long length = f.length();
              //
              // md.update((byte) ((length >> 56 & 0xFF)));
              // md.update((byte) ((length >> 48 & 0xFF)));
              // md.update((byte) ((length >> 40 & 0xFF)));
              // md.update((byte) ((length >> 32 & 0xFF)));
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
    if (cacheDir == null)
      cacheDir = new File(".errai/").getAbsolutePath();
    final File fileCacheDir = new File(cacheDir);
    // noinspection ResultOfMethodCallIgnored
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
    if (nocache)
      return true;
    if (_hasClasspathChanged != null)
      return _hasClasspathChanged;
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

  private static Map<Class<? extends Annotation>, Boolean> _changeMapForAnnotationScope =
      new HashMap<Class<? extends Annotation>, Boolean>();

  public static boolean hasClasspathChangedForAnnotatedWith(final Set<Class<? extends Annotation>> annotations) {
    if (Boolean.getBoolean("errai.devel.forcecache"))
      return true;

    boolean result = false;
    for (final Class<? extends Annotation> a : annotations) {
      /**
       * We don't terminate prematurely, because we want to cache the hashes for the next run.
       */
      if (hasClasspathChangedForAnnotatedWith(a))
        result = true;
    }

    return result;
  }

  public static boolean hasClasspathChangedForAnnotatedWith(final Class<? extends Annotation> annoClass) {
    if (nocache)
      return true;
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

  private static final String[] moduleRootExclusions = { "target/", "out/", "build/", "src/", "war/", "exploded/" };

  public static String guessWorkingDirectoryForModule(final GeneratorContext context) {
    if (context == null) {
      logger.warn("could not determine module location, using CWD (no context)");
      return new File("").getAbsolutePath() + "/";
    }
    try {
      final List<URL> configUrls = MetaDataScanner.getConfigUrls();
      final Set<String> candidateRoots = new HashSet<String>();
      final String workingDir = new File("").getAbsolutePath();

      Pathcheck: for (final URL url : configUrls) {
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
              if (filePath.startsWith(excl))
                continue Pathcheck;
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

  private static ModuleDef getModuleDef(final GeneratorContext context) {
    final StandardGeneratorContext standardGeneratorContext =
      (StandardGeneratorContext) context;
    try {
      Field moduleField = StandardGeneratorContext.class.getDeclaredField("module");
      moduleField.setAccessible(true);
      return (ModuleDef) moduleField.get(standardGeneratorContext);
    }
    catch (Throwable t) {
      try {
        // for GWT versions higher than 2.5.1 we need to get the ModuleDef out of the
        // CompilerContext
        Field compilerContextField = StandardGeneratorContext.class.getDeclaredField("compilerContext");
        compilerContextField.setAccessible(true);
        // Using plain Object because CompilerContext doesn't exist in GWT 2.5
        Object compilerContext = compilerContextField.get(standardGeneratorContext);
        Method getModuleMethod = compilerContext.getClass().getMethod("getModule");
        return (ModuleDef) getModuleMethod.invoke(compilerContext);
      }
      catch (Throwable t2) {
        throw new RuntimeException("could not get module definition (you may be using an incompatible GWT version)", t);
      }
    }
  }

  public static Set<File> getAllModuleXMLs(final GeneratorContext context) {
    final ModuleDef moduleDef = getModuleDef(context);

    try {
      Field gwtXmlFilesField = ModuleDef.class.getDeclaredField("gwtXmlFiles");
      gwtXmlFilesField.setAccessible(true);
      return (Set<File>) gwtXmlFilesField.get(moduleDef);
    }
    catch (Throwable t) {
      throw new RuntimeException("could not access 'gwtXmlFiles' field from the module definition " +
          "(you may be using an incompatible GWT version)");
    }
  }

  public static Set<String> getInheritedModules(final GeneratorContext context) {
    final ModuleDef moduleDef = getModuleDef(context);

    try {
      Field inheritedModules = ModuleDef.class.getDeclaredField("inheritedModules");
      inheritedModules.setAccessible(true);
      return (Set<String>) inheritedModules.get(moduleDef);
    }
    catch (Throwable t) {
      throw new RuntimeException("could not access 'inheritedModules' field from the module definition " +
          "(you may be using an incompatible GWT version)");
    }
  }
  
  public static boolean isModuleInherited(final GeneratorContext context, String moduleName) {
    return getInheritedModules(context).contains(moduleName);
  }

  public static Set<String> getReloadablePackageNames(final GeneratorContext context) {
    Set<String> result = new HashSet<String>();
    ModuleDef module = getModuleDef(context);
    if (module == null) {
      return result;
    }
    
    String moduleName = module.getCanonicalName().replace(".JUnit", "");
    result.add(StringUtils.substringBeforeLast(moduleName, "."));

    List<String> dottedModulePaths = new ArrayList<String>();
    for (File moduleXmlFile : getAllModuleXMLs(context)) {
      String fileName = moduleXmlFile.getAbsolutePath();
      fileName = fileName.replace(File.separatorChar, '.');
      dottedModulePaths.add(fileName);
    }

    for (String inheritedModule : getInheritedModules(context)) {
      for (String dottedModulePath : dottedModulePaths) {
        if (dottedModulePath.contains(inheritedModule)) {
          result.add(StringUtils.substringBeforeLast(inheritedModule, "."));
        }
      }
    }

    return result;
  }

  public static Set<String> getOuterTranslatablePackages(final GeneratorContext context) {
    final Set<File> xmlRoots = getAllModuleXMLs(context);
    final Set<String> pathRoots = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
    final List<String> classPathRoots = new ArrayList<String>();
    try {
      final Enumeration<URL> resources = Thread.currentThread().getContextClassLoader().getResources("");

      while (resources.hasMoreElements()) {
        classPathRoots.add(resources.nextElement().getFile());
      }
    }
    catch (IOException e) {
      e.printStackTrace();
    }

    final ExecutorService executorService = Executors.newCachedThreadPool();

    for (final File xmlFile : xmlRoots) {
      if (xmlFile.exists()) {
        executorService.execute(new Runnable() {
          @Override
          public void run() {
            InputStream inputStream = null;
            try {

              inputStream = new BufferedInputStream(new FileInputStream(xmlFile));
              final DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
              final Document document = builder.parse(inputStream);
              final NodeList moduleNodes = document.getElementsByTagName("module");

              if (moduleNodes.getLength() > 0) {
                for (int i = 0; i < moduleNodes.getLength(); i++) {
                  final Node item = moduleNodes.item(i);
                  final String nodeName = item.getNodeName();
                  if (nodeName.equals("super-source") || nodeName.equals("source")) {
                    final String path = item.getAttributes().getNamedItem("path").getNodeValue();
                    final String filePath = new File(xmlFile.getParentFile(), path).getAbsolutePath();

                    for (final String cpRoot : classPathRoots) {
                      if (filePath.startsWith(cpRoot)) {
                        pathRoots.add(filePath.substring(cpRoot.length())
                            .replace('/', '.').replace('\\', '.'));
                      }
                    }
                  }
                }
              }

              final File clientPath = new File(xmlFile.getParentFile().getAbsoluteFile(), "client").getAbsoluteFile();
              if (clientPath.exists()) {
                final String filePath = clientPath.getAbsolutePath();
                for (final String cpRoot : classPathRoots) {
                  if (filePath.startsWith(cpRoot)) {
                    pathRoots.add(filePath.substring(cpRoot.length()).replace('/', '.').replace('\\', '.'));
                  }
                }
              }
            }
            catch (ParserConfigurationException e) {
              e.printStackTrace();
            }
            catch (SAXException e) {
              e.printStackTrace();
            }
            catch (IOException e) {
              logger.error("error accessing module XML file", e);
            }
            finally {
              if (inputStream != null) {
                try {
                  inputStream.close();
                }
                catch (IOException e) {
                  logger.warn("problem closing stream", e);
                }
              }
            }
          }
        });
      }
      else {
        logger.warn("the GWT module file '" + xmlFile.getAbsolutePath() + "' does not appear to exist.");
      }
    }

    try {
      executorService.shutdown();
      executorService.awaitTermination(60, TimeUnit.MINUTES);
    }
    catch (InterruptedException e) {
      e.printStackTrace();
    }

    return pathRoots;
  }

  public static String getModuleName(final GeneratorContext context) {
    try {
      return getModuleDef(context).getCanonicalName();
    }
    catch (Throwable t) {
      return null;
    }
  }

  /**
   * Returns the list of translatable packages in the module that caused the generator to run (the
   * module under compilation).
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
      final int endIndex = moduleName.lastIndexOf('.');
      final String modulePackage = endIndex == -1 ? "" : moduleName.substring(0, endIndex);

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
   * Returns a list of all translatable packages accessible to the module under compilation
   * (including inherited modules).
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