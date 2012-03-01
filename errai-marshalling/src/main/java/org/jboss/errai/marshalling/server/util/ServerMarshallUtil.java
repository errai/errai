/*
 * Copyright 2011 JBoss, by Red Hat, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.marshalling.server.util;

import static org.slf4j.LoggerFactory.getLogger;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.regex.Pattern;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

import org.jboss.errai.common.metadata.MetaDataScanner;
import org.jboss.errai.common.metadata.RebindUtils;
import org.jboss.errai.marshalling.client.api.MarshallerFactory;
import org.jboss.errai.marshalling.rebind.MarshallerGeneratorFactory;
import org.jboss.errai.marshalling.rebind.MarshallerOuputTarget;
import org.jboss.errai.marshalling.rebind.MarshallersGenerator;
import org.slf4j.Logger;

/**
 * Utility which provides convenience methods for generating marshallers for the server-side.
 *
 * @author Mike Brock
 */
public abstract class ServerMarshallUtil {
  private static final String CLASSLOADING_MODE_PROPERTY = "errai.marshalling.classloading.mode";
  private static final String classLoadingMode;

  private static Logger log = getLogger("ErraiMarshalling");

  static {
    if (System.getProperty(CLASSLOADING_MODE_PROPERTY) != null) {
      classLoadingMode = System.getProperty(CLASSLOADING_MODE_PROPERTY);
    }
    else {
      classLoadingMode = "thread";
    }
  }

  private static List<String> urlToFile(Enumeration<URL> urls) {
    ArrayList<String> files = new ArrayList<String>();
    while (urls.hasMoreElements()) {
      files.add(urls.nextElement().getFile());
    }
    return files;
  }

  public static Class<? extends MarshallerFactory> getGeneratedMarshallerFactoryForServer() {
    String packageName = MarshallersGenerator.SERVER_MARSHALLER_PACKAGE_NAME;
    String className = MarshallersGenerator.SERVER_MARSHALLER_CLASS_NAME;

    try {
      log.info("searching for marshaller class: " + packageName + "." + className);

      final String classResource = packageName.replaceAll("\\.", "/") + "/" + className + ".class";
      Set<String> locations = new HashSet<String>();

      // look for the class in every classloader we can think of. For example, current thread
      // classloading works in Jetty but not JBoss AS 7.
      locations.addAll(urlToFile(Thread.currentThread().getContextClassLoader().getResources(classResource)));
      locations.addAll(urlToFile(ServerMarshallUtil.class.getClassLoader().getResources(classResource)));
      locations.addAll(urlToFile(ClassLoader.getSystemResources(classResource)));

      File newest = null;
      for (String url : locations) {
        File file = getFileIfExists(url);
        if (file != null && (newest == null || file.lastModified() > newest.lastModified())) {
          newest = file;
        }
      }

      if (locations.size() > 1) {
        log.warn("*** MULTIPLE VERSIONS OF " + packageName + "." + className + " FOUND IN CLASSPATH: " +
                "Attempted to guess the newest one based on file dates. But you should clean your output directories");

        for (String loc : locations) {
          log.warn(" Ambiguous version -> " + loc);
        }
      }

      if (newest == null) {
        try {
          // maybe we're in an appserver with a VFS, so try to load anyways.
          return Thread.currentThread().getContextClassLoader().loadClass(packageName + "." + className)
                  .asSubclass(MarshallerFactory.class);
        }
        catch (ClassNotFoundException e) {
          log.warn("could not locate marshaller class. will attempt dynamic generation.");
        }
      }
      else {
        return loadClassDefinition(newest.getAbsolutePath(), packageName, className);
      }
    }
    catch (IOException e) {
      e.printStackTrace();
      log.warn("could not read marshaller classes: " + e);
    }


    final String classStr = MarshallerGeneratorFactory.getFor(MarshallerOuputTarget.Java)
            .generate(packageName, className);

    File directory =
            new File(RebindUtils.getTempDirectory() + "/errai.gen/classes/" + packageName.replaceAll("\\.", "/"));

    File sourceFile = new File(directory.getAbsolutePath() + File.separator + className + ".java");

    try {
      if (directory.exists()) {
        for (File file : directory.listFiles()) {
          file.delete();
        }

        directory.delete();
      }

      directory.mkdirs();

      FileOutputStream outputStream = new FileOutputStream(sourceFile);

      outputStream.write(classStr.getBytes());
      outputStream.flush();
      outputStream.close();

      String compiledClassPath = compileClass(directory.getAbsolutePath(), packageName, className,
              directory.getAbsolutePath());

      return loadClassDefinition(compiledClassPath, packageName, className);
    }
    catch (IOException e) {
      throw new RuntimeException("failed to generate class ", e);
    }


  }

  public static String compileClass(String sourcePath, String packageName, String className, String outputPath) {
    try {
      File inFile = new File(sourcePath + File.separator + className + ".java");
      //  File outFile = new File(sourcePath + File.separator + className + ".class");

      ByteArrayOutputStream errorOutputStream = new ByteArrayOutputStream();
      JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

      if (compiler == null) {
        throw new RuntimeException("Could not locate a compiler. You may be running in a JRE and not a JDK. " +
                "For the purpose of development mode Errai requires the use of a JDK so it may produce server " +
                "marshalling code on-the-fly.");
      }

      File classOutputDir = new File(outputPath
              + File.separatorChar + RebindUtils.packageNameToDirName(packageName)
              + File.separatorChar).getAbsoluteFile();

      // delete any marshaller classes already there
      Pattern matcher = Pattern.compile("^" + className + "(\\.|$).*class$");
      if (classOutputDir.exists()) {
        for (File file : classOutputDir.listFiles()){
          if (matcher.matcher(file.getName()).matches()) {
            file.delete();
          }
        }
      }

      /**
       * Attempt to run the compiler without any classpath specified.
       */
      if (compiler.run(null, null, errorOutputStream, "-d", outputPath, inFile.getAbsolutePath()) != 0) {
        errorOutputStream.reset();

        /**
         * That didn't work. Let's try and figure out the classpath.
         */
        StringBuilder sb = new StringBuilder();

        List<URL> configUrls = MetaDataScanner.getConfigUrls();
        List<File> classpathElements = new ArrayList<File>(configUrls.size());

        for (URL url : configUrls) {
          File file = getFileIfExists(url.getFile());
          if (file != null) {
            classpathElements.add(file);
          }
        }

        for (File file : classpathElements)
          sb.append(file.getAbsolutePath()).append(File.pathSeparator);

        sb.append(System.getProperty("java.class.path"));
        sb.append(findAllJarsByManifest());

        if (compiler.run(null, null, errorOutputStream, "-d", outputPath, "-cp", sb.toString(), inFile.getAbsolutePath()) != 0) {
          System.out.println("*** FAILED TO COMPILE MARSHALLER CLASS ***");
          System.out.println("*** Classpath Used: " + sb.toString());


          for (byte b : errorOutputStream.toByteArray()) {
            System.out.print((char) b);
          }
          return null;
        }
      }


      return new File(classOutputDir.getAbsolutePath() + File.separatorChar
              + className + ".class").getAbsolutePath();
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static Class loadClassDefinition(String path, String packageName, String className) throws IOException {
    if (path == null) return null;

    FileInputStream inputStream = new FileInputStream(path);
    byte[] classDefinition = new byte[inputStream.available()];

    String classBase = path.substring(0, path.length() - ".class".length());

    BootstrapClassloader clsLoader = new BootstrapClassloader(new File(path).getParentFile().getAbsolutePath(),
            "system".equals(classLoadingMode) ?
                    ClassLoader.getSystemClassLoader() :
                    Thread.currentThread().getContextClassLoader());

    inputStream.read(classDefinition);

    for (File file : new File(path).getParentFile().listFiles()) {
      if (file.getName().startsWith(className + "$")) {
        String s = file.getName();
        s = s.substring(s.indexOf('$') + 1, s.lastIndexOf('.'));

        String fqcn = packageName + "." + className + "$" + s;

        Class cls = null;
        try {
          cls = clsLoader.loadClass(fqcn);
        }
        catch (ClassNotFoundException e) {
        }

        if (cls != null) continue;

        String innerClassBaseName = classBase + "$" + s;
        File innerClass = new File(innerClassBaseName + ".class");
        if (innerClass.exists()) {
          try {
            inputStream = new FileInputStream(innerClass);
            classDefinition = new byte[inputStream.available()];
            inputStream.read(classDefinition);

            clsLoader.defineClassX(fqcn, classDefinition, 0, classDefinition.length);
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


    Class<?> mainClass = clsLoader
            .defineClassX(packageName + "." + className, classDefinition, 0, classDefinition.length);

    inputStream.close();


    for (int i = 1; i < Integer.MAX_VALUE; i++) {

      String fqcn = packageName + "." + className + "$" + i;

      Class cls = null;
      try {
        cls = clsLoader.loadClass(fqcn);
      }
      catch (ClassNotFoundException e) {
      }

      if (cls != null) continue;


      String innerClassBaseName = classBase + "$" + i;
      File innerClass = new File(innerClassBaseName + ".class");
      if (innerClass.exists()) {
        try {
          inputStream = new FileInputStream(innerClass);
          classDefinition = new byte[inputStream.available()];
          inputStream.read(classDefinition);

          clsLoader.defineClassX(fqcn, classDefinition, 0, classDefinition.length);
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

    private BootstrapClassloader(String searchPath, ClassLoader classLoader) {
      super(classLoader);
      this.searchPath = searchPath;
    }

    public Class<?> defineClassX(String className, byte[] b, int off, int len) {
      return super.defineClass(className, b, off, len);
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
      try {
        return super.findClass(name);
      }
      catch (ClassNotFoundException e) {
        try {
          FileInputStream inputStream = null;
          byte[] classDefinition;


          File innerClass = new File(searchPath + "/" + name.substring(name.lastIndexOf('.') + 1) + ".class");
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
    StringBuilder cp = new StringBuilder();
    try {
      Enumeration[] enumers = new Enumeration[]
              {
                      Thread.currentThread().getContextClassLoader().getResources(JarFile.MANIFEST_NAME),
                      ClassLoader.getSystemClassLoader().getResources(JarFile.MANIFEST_NAME)
              };

      for (Enumeration resEnum : enumers) {
        while (resEnum.hasMoreElements()) {
          InputStream is = null;
          try {
            URL url = (URL) resEnum.nextElement();

            File file = getFileIfExists(url.getFile());
            if (file != null) {
              cp.append(File.pathSeparator).append(file.getAbsolutePath());
            }
          }
          catch (Exception e) {
            log.info("Ignoring classpath entry with invalid manifest", e);
          }
          finally {
            if (is != null) is.close();
          }
        }
      }
    }
    catch (IOException e1) {
      // Silently ignore wrong manifests on classpath?
      log.info("Failed to build classpath using manifest discovery. Expect compile failures...", e1);
    }

    return cp.toString();
  }

  private static File getFileIfExists(String path) {
    //   String path = url.getFile();

    if (path.startsWith("file:")) {
      path = path.substring(5);

      int outerElement = path.indexOf('!');
      if (outerElement != -1) {
        path = path.substring(0, outerElement);
      }
    }
    else if (path.startsWith("jar:")) {
      path = path.substring(4);

      int outerElement = path.indexOf('!');
      if (outerElement != -1) {
        path = path.substring(0, outerElement);
      }
    }

    File file = new File(path);
    if (file.exists()) {
      return file;
    }
    return null;
  }

  public static Set<File> findAllMatching(String fileName, File from) {
    HashSet<File> matching = new HashSet<File>();
    _findAllMatching(matching, fileName, from);
    return matching;
  }

  public static void _findAllMatching(HashSet<File> matching, String fileName, File from) {
    if (from.isDirectory()) {
      for (File file : from.listFiles()) {
        _findAllMatching(matching, fileName, file);
      }
    }
    else {
      if (fileName.equals(from.getName())) {
        matching.add(from);
      }
    }
  }

  public static Set<File> findMatchingOutputDirectoryByModel(Map<String, String> toMatch, File from) {
    HashSet<File> matching = new HashSet<File>();
    _findMatchingOutputDirectoryByModel(matching, toMatch, from);
    return matching;
  }

  private static void _findMatchingOutputDirectoryByModel(Set<File> matching, Map<String, String> toMatch, File from) {
    if (from.isDirectory()) {
      for (File file : from.listFiles()) {
        int currMatch = matching.size();
        _findMatchingOutputDirectoryByModel(matching, toMatch, file);
        if (matching.size() > currMatch) {
          break;
        }
      }
    }
    else {
      String name = from.getName();
      if (name.endsWith(".class") && toMatch.containsKey(name = name.substring(0, name.length() - 6))) {
        String full = toMatch.get(name);
        ReverseMatchResult res = reversePathMatch(full, from);

        if (res.isMatch()) {
          matching.add(res.getMatchRoot());
        }
      }
    }
  }


  private static ReverseMatchResult reversePathMatch(String fqcn, File location) {
    List<String> stk = new ArrayList<String>(Arrays.asList(fqcn.split("\\.")));

    File curr = location;

    if (!stk.isEmpty()) {
      // remove the last element -- as that would be the file name.
      stk.remove(stk.size() - 1);
    }

    while (!stk.isEmpty()) {
      String el = stk.remove(stk.size() - 1);
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

    private ReverseMatchResult(boolean match, File matchRoot) {
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

}
