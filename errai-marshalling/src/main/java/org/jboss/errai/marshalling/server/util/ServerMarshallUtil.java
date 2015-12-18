/*
 * Copyright (C) 2011 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.marshalling.server.util;

import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jboss.errai.codegen.util.ClassChangeUtil;
import org.jboss.errai.common.metadata.RebindUtils;
import org.jboss.errai.marshalling.client.api.MarshallerFactory;
import org.jboss.errai.marshalling.rebind.MarshallerGeneratorFactory;
import org.jboss.errai.marshalling.rebind.MarshallerOutputTarget;
import org.jboss.errai.marshalling.rebind.MarshallersGenerator;
import org.jboss.errai.marshalling.rebind.util.MarshallingGenUtil;
import org.slf4j.Logger;

/**
 * Utility which provides convenience methods for generating marshallers for the server-side.
 * 
 * @author Mike Brock
 */
public abstract class ServerMarshallUtil {
  private static Logger log = getLogger("ErraiMarshalling");

  private static List<String> urlToFile(Enumeration<URL> urls) {
    final ArrayList<String> files = new ArrayList<String>();
    while (urls.hasMoreElements()) {
      files.add(urls.nextElement().getFile());
    }
    return files;
  }

  public static Class<? extends MarshallerFactory> getGeneratedMarshallerFactoryForServer() {
    final String packageName = MarshallersGenerator.SERVER_MARSHALLER_PACKAGE_NAME;
    final String className = MarshallersGenerator.SERVER_MARSHALLER_CLASS_NAME;

    try {
      log.debug("searching for marshaller class: " + packageName + "." + className);

      final String classResource = packageName.replaceAll("\\.", "/") + "/" + className + ".class";
      final Set<String> locations = new HashSet<String>();

      // look for the class in every classloader we can think of. For example, current thread
      // classloading works in Jetty but not JBoss AS 7.
      locations.addAll(urlToFile(Thread.currentThread().getContextClassLoader().getResources(classResource)));
      locations.addAll(urlToFile(ServerMarshallUtil.class.getClassLoader().getResources(classResource)));
      locations.addAll(urlToFile(ClassLoader.getSystemResources(classResource)));

      File newest = null;
      for (String url : locations) {
        final File file = ClassChangeUtil.getFileIfExists(url);
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
          log.warn("could not locate marshaller class.");
          if (!MarshallingGenUtil.isForceStaticMarshallers()) {
            return null;
          }
          else {
            log.info("couldn't find marshaller class, attempting to generate ...");
          }
        }
      }
      else {
        return ClassChangeUtil.loadClassDefinition(newest.getAbsolutePath(), packageName, className);
      }
    }
    catch (IOException e) {
      e.printStackTrace();
      log.warn("could not read marshaller classes: " + e);
    }

    final String classStr = MarshallerGeneratorFactory.getFor(null, MarshallerOutputTarget.Java)
            .generate(packageName, className);

    final File directory =
            new File(RebindUtils.getTempDirectory()
                    + "/errai.gen/classes/" + packageName.replaceAll("\\.", "/"));

    final File sourceFile = new File(directory.getAbsolutePath() + File.separator + className + ".java");

    try {
      if (directory.exists()) {
        for (File file : directory.listFiles()) {
          file.delete();
        }
        directory.delete();
      }
      directory.mkdirs();

      final FileOutputStream outputStream = new FileOutputStream(sourceFile);
      outputStream.write(classStr.getBytes("UTF-8"));
      outputStream.flush();
      outputStream.close();

      String compiledClassPath = ClassChangeUtil.compileClass(directory.getAbsolutePath(), packageName, className,
              directory.getAbsolutePath());

      return ClassChangeUtil.loadClassDefinition(compiledClassPath, packageName, className);
    }
    catch (IOException e) {
      throw new RuntimeException("failed to generate class ", e);
    }
  }
}
