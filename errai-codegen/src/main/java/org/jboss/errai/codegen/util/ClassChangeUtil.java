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

package org.jboss.errai.codegen.util;

import static org.slf4j.LoggerFactory.getLogger;

import org.eclipse.jdt.core.compiler.CompilationProgress;
import org.eclipse.jdt.core.compiler.batch.BatchCompiler;
import org.jboss.errai.common.metadata.MetaDataScanner;
import org.jboss.errai.common.metadata.RebindUtils;
import org.slf4j.Logger;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.regex.Pattern;

/**
 * @author Mike Brock
 */
public class ClassChangeUtil {
  private static final String USE_NATIVE_JAVA_COMPILER = "errai.marshalling.use_native_javac";
  private static final String CLASSLOADING_MODE_PROPERTY = "errai.marshalling.classloading.mode";

  private static final String classLoadingMode;
  private static final boolean useNativeJavac = Boolean.getBoolean(USE_NATIVE_JAVA_COMPILER);
  private static Logger log = getLogger("ErraiMarshalling");

  static {
    if (System.getProperty(CLASSLOADING_MODE_PROPERTY) != null) {
      classLoadingMode = System.getProperty(CLASSLOADING_MODE_PROPERTY);
    }
    else {
      classLoadingMode = "thread";
    }
  }


  private static interface CompilerAdapter {
    int compile(OutputStream out, OutputStream errors, String outputPath, String toCompile, String classpath);
  }

  public static class JDKCompiler implements CompilerAdapter {
    final JavaCompiler compiler;

    public JDKCompiler(final JavaCompiler compiler) {
      this.compiler = compiler;
    }

    @Override
    public int compile(final OutputStream out, final OutputStream errors, final String outputPath, final String toCompile, final String classpath) {
      return compiler.run(null, out, errors, "-classpath", classpath, "-d", outputPath, toCompile);
    }
  }

  public static class JDTCompiler implements CompilerAdapter {
    @Override
    public int compile(final OutputStream out,
                       final OutputStream errors,
                       final String outputPath,
                       final String toCompile,
                       final String classpath) {

      return BatchCompiler.compile(new String[] { "-classpath", classpath, "-d", outputPath, "-source", "1.6", toCompile },
          new PrintWriter(out), new PrintWriter(errors),
          new CompilationProgress() {
            @Override
            public void begin(final int remainingWork) {
            }

            @Override
            public void done() {
            }

            @Override
            public boolean isCanceled() {
              return false;
            }

            @Override
            public void setTaskName(final String name) {
            }

            @Override
            public void worked(final int workIncrement, final int remainingWork) {
            }
          }) ? 0 : -1;
    }
  }

  public static Class compileAndLoad(final File sourceFile,
                                     final String fullyQualifiedName) throws IOException {
    final String packageName = getPackageFromFQCN(fullyQualifiedName);
    final String className = getNameFromFQCN(fullyQualifiedName);

    return compileAndLoad(sourceFile, packageName, className);
  }

  public static Class compileAndLoad(final File sourceFile,
                                     final String packageName,
                                     final String className) throws IOException {

    return compileAndLoad(sourceFile.getParentFile().getAbsolutePath(), packageName, className);

  }

  public static Class compileAndLoad(final String sourcePath,
                                     final String packageName,
                                     final String className) throws IOException {
    final String tempDirectory = RebindUtils.getTempDirectory();

    return compileAndLoad(sourcePath, packageName, className, tempDirectory);
  }


  public static Class compileAndLoad(final String sourcePath,
                                     final String packageName,
                                     final String className,
                                     final String outputPath) throws IOException {

    final String outputLocation = compileClass(sourcePath, packageName, className, outputPath);
    return loadClassDefinition(outputLocation, packageName, className);
  }

  public static String compileClass(final String sourcePath,
                                    final String packageName,
                                    final String className,
                                    final String outputPath) {

    try {

      final ByteArrayOutputStream errorOutputStream = new ByteArrayOutputStream();
      final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
      final CompilerAdapter adapter;

      if (compiler == null || !useNativeJavac) {
        adapter = new JDTCompiler();
      }
      else {
        adapter = new JDKCompiler(compiler);
      }

      final File classOutputDir = new File(outputPath
          + File.separatorChar + RebindUtils.packageNameToDirName(packageName)
          + File.separatorChar).getAbsoluteFile();

      // delete any marshaller classes already there
      final Pattern matcher = Pattern.compile("^" + className + "(\\.|$).*class$");
      if (classOutputDir.exists()) {
        for (final File file : classOutputDir.listFiles()) {
          if (matcher.matcher(file.getName()).matches()) {
            file.delete();
          }
        }
      }

      final StringBuilder sb = new StringBuilder(4096);
      final List<URL> configUrls = MetaDataScanner.getConfigUrls();
      final List<File> classpathElements = new ArrayList<File>(configUrls.size());
      classpathElements.add(new File(outputPath));

      log.debug(">>> Searching for all jars by " + MetaDataScanner.ERRAI_CONFIG_STUB_NAME);
      for (final URL url : configUrls) {
        final File file = getFileIfExists(url.getFile());
        if (file != null) {
          classpathElements.add(file);
        }
      }
      log.debug("<<< Done searching for all jars by " + MetaDataScanner.ERRAI_CONFIG_STUB_NAME);

      for (final File file : classpathElements) {
        sb.append(file.getAbsolutePath()).append(File.pathSeparator);
      }

      sb.append(System.getProperty("java.class.path"));
      sb.append(findAllJarsByManifest());

      final String classPath = sb.toString();

      /**
       * Attempt to run the compiler without any classpath specified.
       */
      if (adapter.compile(System.out, errorOutputStream, outputPath,
          new File(sourcePath + File.separator + className + ".java").getAbsolutePath(), classPath) != 0) {

        System.out.println("*** FAILED TO COMPILE CLASS ***");
        System.out.println("*** Classpath Used: " + classPath);

        for (final byte b : errorOutputStream.toByteArray()) {
          System.out.print((char) b);
        }
        return null;
      }

      return new File(classOutputDir.getAbsolutePath() + File.separatorChar
          + className + ".class").getAbsolutePath();
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static Class loadClassDefinition(final String path,
                                          final String packageName,
                                          final String className) throws IOException {
    if (path == null) return null;

    FileInputStream inputStream = new FileInputStream(path);
    byte[] classDefinition = new byte[inputStream.available()];

    final String classBase = path.substring(0, path.length() - ".class".length());

    final BootstrapClassloader clsLoader = new BootstrapClassloader(new File(path).getParentFile().getAbsolutePath(),
        "system".equals(classLoadingMode) ?
            ClassLoader.getSystemClassLoader() :
            Thread.currentThread().getContextClassLoader());

    final String fqcn;
    if ("".equals(packageName)) {
      fqcn = className;
    }
    else {
      fqcn = packageName + "." + className;
    }

    try {
      return clsLoader.loadClass(fqcn);
    }
    catch (Throwable t) {
      // fall through
    }

    inputStream.read(classDefinition);

    for (final File file : new File(path).getParentFile().listFiles()) {
      if (file.getName().startsWith(className + "$")) {
        String s = file.getName();
        s = s.substring(s.indexOf('$') + 1, s.lastIndexOf('.'));

        final String nestedClassName = fqcn + "$" + s;

        Class cls = null;
        try {
          cls = clsLoader.loadClass(nestedClassName);
        }
        catch (ClassNotFoundException ignored) {
        }

        if (cls != null) continue;

        final String innerClassBaseName = classBase + "$" + s;
        final File innerClass = new File(innerClassBaseName + ".class");
        if (innerClass.exists()) {
          try {
            inputStream = new FileInputStream(innerClass);
            classDefinition = new byte[inputStream.available()];
            inputStream.read(classDefinition);

            clsLoader.defineClassX(nestedClassName, classDefinition, 0, classDefinition.length);
          }
          finally {
            inputStream.close();
          }
        }
        else {
          break;
        }
      }
    }

    final Class<?> mainClass = clsLoader
        .defineClassX(fqcn, classDefinition, 0, classDefinition.length);

    inputStream.close();

    for (int i = 1; i < Integer.MAX_VALUE; i++) {

      final String nestedClassName = fqcn + "$" + i;

      Class cls = null;
      try {
        cls = clsLoader.loadClass(nestedClassName);
      }
      catch (ClassNotFoundException ignored) {
      }

      if (cls != null) continue;


      final String innerClassBaseName = classBase + "$" + i;
      final File innerClass = new File(innerClassBaseName + ".class");
      if (innerClass.exists()) {
        try {
          inputStream = new FileInputStream(innerClass);
          classDefinition = new byte[inputStream.available()];
          inputStream.read(classDefinition);

          clsLoader.defineClassX(nestedClassName, classDefinition, 0, classDefinition.length);
        }
        finally {
          inputStream.close();
        }
      }
      else {
        break;
      }
    }

    return mainClass;
  }

  private static class BootstrapClassloader extends ClassLoader {
    private String searchPath;

    private BootstrapClassloader(final String searchPath, final ClassLoader classLoader) {
      super(classLoader);
      this.searchPath = searchPath;
    }

    public Class<?> defineClassX(final String className, final byte[] b, final int off, final int len) {
      return super.defineClass(className, b, off, len);
    }

    @Override
    protected Class<?> findClass(final String name) throws ClassNotFoundException {
      try {
        return super.findClass(name);
      }
      catch (ClassNotFoundException e) {
        try {
          FileInputStream inputStream = null;
          final byte[] classDefinition;


          final File innerClass = new File(searchPath + "/" + name.substring(name.lastIndexOf('.') + 1) + ".class");
          if (innerClass.exists()) {
            try {
              inputStream = new FileInputStream(innerClass);
              classDefinition = new byte[inputStream.available()];
              inputStream.read(classDefinition);

              return defineClassX(name, classDefinition, 0, classDefinition.length);
            }
            finally {
              if (inputStream != null) inputStream.close();
            }

          }
        }
        catch (IOException e2) {
          throw new RuntimeException("failed to load class: " + name, e2);
        }


      }
      throw new ClassNotFoundException(name);
    }
  }

  private static String findAllJarsByManifest() {
    final StringBuilder cp = new StringBuilder();
    try {
      log.debug(">>> Searching for all jars by " + JarFile.MANIFEST_NAME);
      final Enumeration[] enumerations = new Enumeration[]
          {
              Thread.currentThread().getContextClassLoader().getResources(JarFile.MANIFEST_NAME),
              ClassLoader.getSystemClassLoader().getResources(JarFile.MANIFEST_NAME)
          };

      for (final Enumeration resEnum : enumerations) {
        while (resEnum.hasMoreElements()) {
          try {
            final File file = getFileIfExists(((URL) resEnum.nextElement()).getFile());
            if (file != null) {
              cp.append(File.pathSeparator).append(file.getAbsolutePath());
            }
          }
          catch (Exception e) {
            log.warn("ignoring classpath entry with invalid manifest", e);
          }
        }
      }
    }
    catch (IOException e1) {
      // Silently ignore wrong manifests on classpath?
      log.warn("failed to build classpath using manifest discovery. Expect compile failures...", e1);
    }
    finally {
      log.debug("<<< Done searching for all jars by " + JarFile.MANIFEST_NAME);
    }

    return cp.toString();
  }

  public static File getFileIfExists(String path) {
    final String originalPath = path;

    if (path.startsWith("file:")) {
      path = path.substring(5);

      final int outerElement = path.indexOf('!');
      if (outerElement != -1) {
        path = path.substring(0, outerElement);
      }
    }
    else if (path.startsWith("jar:")) {
      path = path.substring(4);

      final int outerElement = path.indexOf('!');
      if (outerElement != -1) {
        path = path.substring(0, outerElement);
      }
    }

    final File file = new File(path);
    if (file.exists()) {
      if (log.isDebugEnabled()) {
        log.debug("   EXISTS: " + originalPath + " -> " + file.getAbsolutePath());
      }
      return file;
    }

    if (log.isDebugEnabled()) {
      log.debug("  !EXISTS: " + originalPath + " -> " + file.getAbsolutePath());
    }
    return null;
  }

  public static Set<File> findAllMatching(final String fileName, final File from) {
    final HashSet<File> matching = new HashSet<File>();
    _findAllMatching(matching, fileName, from);
    return matching;
  }

  public static void _findAllMatching(final HashSet<File> matching, final String fileName, final File from) {
    if (from.isDirectory()) {
      final File[] files = from.listFiles();
      if (files != null) {
        for (final File file : from.listFiles()) {
          _findAllMatching(matching, fileName, file);
        }
      }
      else {
        log.debug("Failed to read: " + from.getAbsolutePath());
      }
    }
    else {
      if (fileName.equals(from.getName())) {
        matching.add(from);
      }
    }
  }

  public static Set<File> findMatchingOutputDirectoryByModel(final Map<String, String> toMatch, final File from) {
    final HashSet<File> matching = new HashSet<File>();
    _findMatchingOutputDirectoryByModel(matching, toMatch, from);
    return matching;
  }

  @SuppressWarnings("ConstantConditions")
  private static void _findMatchingOutputDirectoryByModel(final Set<File> matching,
                                                          final Map<String, String> toMatch,
                                                          final File from) {
    if (from.isDirectory()) {
      for (final File file : from.listFiles()) {
        final int currMatch = matching.size();
        _findMatchingOutputDirectoryByModel(matching, toMatch, file);
        if (matching.size() > currMatch) {
          break;
        }
      }
    }
    else {
      String name = from.getName();
      if (name.endsWith(".class") && toMatch.containsKey(name = name.substring(0, name.length() - 6))) {
        final String full = toMatch.get(name);
        final ReverseMatchResult res = reversePathMatch(full, from);

        if (res.isMatch()) {
          matching.add(res.getMatchRoot());
        }
      }
    }
  }

  private static ReverseMatchResult reversePathMatch(final String fqcn, final File location) {
    final List<String> stk = new ArrayList<String>(Arrays.asList(fqcn.split("\\.")));

    File curr = location;

    if (!stk.isEmpty()) {
      // remove the last element -- as that would be the file name.
      stk.remove(stk.size() - 1);
    }

    while (!stk.isEmpty()) {
      final String el = stk.remove(stk.size() - 1);
      curr = curr.getParentFile();
      if (curr == null || !curr.getName().equals(el)) {
        break;
      }
    }

    if (curr != null) {
      curr = curr.getParentFile();
    }

    if (stk.isEmpty()) {
      return new ReverseMatchResult(true, curr);
    }
    else {
      return new ReverseMatchResult(false, curr);
    }
  }

  private static class ReverseMatchResult {
    private final boolean match;
    private final File matchRoot;

    private ReverseMatchResult(final boolean match, final File matchRoot) {
      this.match = match;
      this.matchRoot = matchRoot;
    }

    public boolean isMatch() {
      return match;
    }

    public File getMatchRoot() {
      return matchRoot;
    }
  }

  private static String getPackageFromFQCN(final String fqcn) {
    final int index = fqcn.lastIndexOf('.');
    if (index == -1) {
      return "";
    }
    else {
      return fqcn.substring(0, index);
    }
  }

  private static String getNameFromFQCN(final String fqcn) {
    final int index = fqcn.lastIndexOf('.');
    if (index == -1) {
      return fqcn;
    }
    else {
      return fqcn.substring(index + 1);
    }
  }
}
