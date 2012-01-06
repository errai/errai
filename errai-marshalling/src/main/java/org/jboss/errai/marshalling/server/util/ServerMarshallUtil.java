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

import org.jboss.errai.common.metadata.MetaDataScanner;
import org.jboss.errai.marshalling.client.api.MarshallerFactory;
import org.jboss.errai.marshalling.rebind.MarshallerGeneratorFactory;
import org.jboss.errai.marshalling.rebind.MarshallerOuputTarget;
import org.jboss.errai.marshalling.rebind.MarshallersGenerator;
import org.slf4j.Logger;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarFile;

import static org.slf4j.LoggerFactory.getLogger;

/**
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

  public static Class<? extends MarshallerFactory> getGeneratedMarshallerFactoryForServer() {
    String packageName = MarshallersGenerator.SERVER_MARSHALLER_PACKAGE_NAME;
    String className = MarshallersGenerator.SERVER_MARSHALLER_CLASS_NAME;

    try {
      log.info("searching for marshaller class: " + packageName + "." + className);

      return Thread.currentThread().getContextClassLoader().loadClass(packageName + "." + className)
              .asSubclass(MarshallerFactory.class);

    }
    catch (ClassNotFoundException e) {
      log.warn("could not locate marshaller class. will attempt dynamic generation.");
    }


    final String classStr = MarshallerGeneratorFactory.getFor(MarshallerOuputTarget.Java)
            .generate(packageName, className);

    File directory =
            new File(System.getProperty("java.io.tmpdir") + "/errai.gen/classes/" + packageName.replaceAll("\\.", "/"));

    File sourceFile = new File(directory.getAbsolutePath() + "/" + className + ".java");

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

      String compiledClassPath = compileClass(sourceFile.getAbsolutePath(), packageName, className);

      return loadClassDefinition(compiledClassPath, packageName, className);
    }
    catch (IOException e) {
      throw new RuntimeException("failed to generate class ", e);
    }


  }

  public static String compileClass(String sourcePath, String packageName, String className) {
    try {

      File directory =
              new File(System.getProperty("java.io.tmpdir") + "/errai.gen/classes/" + packageName.replaceAll("\\.", "/"));


      String classBase = directory.getAbsolutePath() + "/" + className;

      File outFile = new File(classBase + ".class");

      ByteArrayOutputStream errorOutputStream = new ByteArrayOutputStream();
      JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

      /**
       * Attempt to run the compiler without any classpath specified.
       */
      if (compiler.run(null, null, errorOutputStream, sourcePath) != 0) {
        errorOutputStream.reset();

        /**
         * That didn't work. Let's try and figure out the classpath.
         */
        StringBuilder sb = new StringBuilder();

        List<URL> configUrls = MetaDataScanner.getConfigUrls();
        List<File> classpathElements = new ArrayList<File>(configUrls.size());

        for (URL url : configUrls) {
          File file = getFileIfExists(url);
          if (file != null) {
            classpathElements.add(file);
          }
        }

        for (File file : classpathElements)
          sb.append(file.getAbsolutePath()).append(File.pathSeparator);

        sb.append(System.getProperty("java.class.path"));
        sb.append(findAllJarsByManifest());

        if (compiler.run(null, null, errorOutputStream, "-cp", sb.toString(), sourcePath) != 0) {
          System.out.println("*** FAILED TO COMPILE MARSHALLER CLASS ***");
          System.out.println("*** Classpath Used: " + sb.toString());
          return null;
        }
      }

      for (byte b : errorOutputStream.toByteArray()) {
        System.out.print((char) b);
      }


      return outFile.getAbsolutePath();
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static Class loadClassDefinition(String path, String packageName, String className) throws IOException {
    FileInputStream inputStream = new FileInputStream(path);
    byte[] classDefinition = new byte[inputStream.available()];

    String classBase = path.substring(0, path.length() - ".class".length());

    inputStream.read(classDefinition);

    BootstrapClassloader clsLoader = new BootstrapClassloader("system".equals(classLoadingMode) ?
            ClassLoader.getSystemClassLoader() :
            Thread.currentThread().getContextClassLoader());

    Class<? extends MarshallerFactory> mainClass = clsLoader
            .defineClassX(packageName + "." + className, classDefinition, 0, classDefinition.length)
            .asSubclass(MarshallerFactory.class);

    inputStream.close();

    for (int i = 1; i < Integer.MAX_VALUE; i++) {
      String innerClassBaseName = classBase + "$" + i;
      File innerClass = new File(innerClassBaseName + ".class");
      if (innerClass.exists()) {
        try {
          inputStream = new FileInputStream(innerClass);
          classDefinition = new byte[inputStream.available()];
          inputStream.read(classDefinition);

          clsLoader.defineClassX(packageName + "." + className + "$" + i, classDefinition, 0, classDefinition.length);
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
    private BootstrapClassloader(ClassLoader classLoader) {
      super(classLoader);
    }

    public Class<?> defineClassX(String className, byte[] b, int off, int len) {
      return super.defineClass(className, b, off, len);
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

            File file = getFileIfExists(url);
            if (file != null) {
              cp.append(File.pathSeparator).append(file.getAbsolutePath());
            }
          }
          catch (Exception e) {
            // Silently ignore wrong manifests on classpath?
          }
          finally {
            if (is != null) is.close();
          }
        }
      }
    }
    catch (IOException e1) {
      // Silently ignore wrong manifests on classpath?
    }

    return cp.toString();
  }

  private static File getFileIfExists(URL url) {
    String path = url.getFile();

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
}
