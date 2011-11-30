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

package org.jboss.errai.bus.server.util;

import org.jboss.errai.bus.server.ErraiBootstrapFailure;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;

/**
 * Contains methods used for configuring and bootstrapping Errai.
 */
public class ConfigUtil extends AbstractConfigBase {

  /**
   * Gets a list of all the configuration targets in the form of <tt>File</tt>s
   *
   * @return a <tt>File</tt> list of all the configuration targets
   */
  public static List<File> findAllConfigTargets() {
    try {
      Enumeration<URL> t = ConfigUtil.class.getClassLoader().getResources(ERRAI_CONFIG_STUB_NAME);
      List<File> targets = new LinkedList<File>();
      while (t.hasMoreElements()) {
        String fileName = URLDecoder.decode(t.nextElement().getFile(), "UTF-8");

        // this is referencing a file inside a compressed archive.
        int trimIdx = fileName.lastIndexOf("!");
        if (trimIdx != -1) {
          // get the path to the archive
          fileName = fileName.substring(0, trimIdx);
        }

        // if it starts with a URI scheme prefix, let's strip it away.
        if (fileName.startsWith("file:/")) {
          fileName = fileName.substring(5);
        }

        // we don't want to bother with source JARs.
        if (fileName.endsWith("-sources.jar")) {
          continue;
        }

        // obtain a File object
        File file = new File(fileName);

        // If this is a direct filesystem path, we get the parent file (directory)
        targets.add(trimIdx == -1 ? file.getParentFile() : file);
      }

      log.debug("configuration scan targets");
      for (File tg : targets) {
        log.debug(" -> " + tg.getPath());
      }

      return targets;
    }
    catch (Exception e) {
      throw new ErraiBootstrapFailure("could not locate config target paths", e);
    }
  }


  /**
   * Cleans up the startup temporary files, including those stored under the system's temp directory
   */
  public static void cleanupStartupTempFiles() {
    if (scanAreas == null) return;

    log.info("Cleaning up ...");
    for (File f : scanAreas.values()) {
      f.delete();
    }
    new File(System.getProperty("java.io.tmpdir") + "/" + tmpUUID).delete();
    scanAreas = null;
    scanCache = null;
  }

  public static void visitAllErraiAppProperties(final List<File> targets, final BundleVisitor visitor) {
    for (File file : targets) {
      File propertyFile = new File(file.getPath() + "/" + "ErraiApp.properties");

      if (propertyFile.exists()) {
        try {
          FileInputStream stream = new FileInputStream(propertyFile);
          try {
            visitor.visit(new PropertyResourceBundle(stream));
          }
          finally {
            stream.close();
          }
        }
        catch (FileNotFoundException e) {
          throw new RuntimeException(e);
        }
        catch (IOException e) {
          throw new RuntimeException(e);
        }
      }
    }
  }


  /**
   * Visits all targets that can be found under <tt>root</tt>, using the <tt>ConfigVisitor</tt> specified
   *
   * @param root    - the root file to start visiting from
   * @param visitor - the visitor delegate to use
   */
  public static void visitAll(File root, final ConfigVisitor visitor) {
    _traverseFiles(root, root, new HashSet<String>(), new VisitDelegate<Class>() {
      public void visit(String clazz) {
        try {
          Class c = Class.forName(clazz);
          visitor.visit(c);
          for (Class sc : c.getDeclaredClasses()) {
            visitor.visit(sc);
          }
        }
        catch (ClassNotFoundException e) {
        }
      }

      public void visitError(String className, Throwable t) {
      }

      public String getFileExtension() {
        return ".class";
      }
    });

    if (activeCacheContexts != null) activeCacheContexts.add(root.getPath());
  }


  /**
   * Visits all the targets listed in the file, using the <tt>ConfigVisitor</tt> specified
   *
   * @param targets - the file targets to visit
   * @param visitor - the visitor delegate to use
   */
  public static void visitAllTargets(List<File> targets, ConfigVisitor visitor) {
    for (File file : targets) {
      visitAll(file, visitor);
    }
  }

  /**
   * Returns true if the specified class, <tt>clazz</tt>, has annotations from the class <tt>annotation</tt>. Also,
   * checks that <tt>clazz</tt> is represented by <tt>ofType</tt>
   *
   * @param clazz      - the class to check for the annotations
   * @param annotation - the annotations to look for
   * @param ofType     - the class type we want to be sure to check, as <tt>clazz</tt> could be a visitor delegate.
   * @return true if the <tt>clazz</tt> has those <tt>annotation</tt>s
   */
  public static boolean isAnnotated(Class clazz, Class<? extends Annotation> annotation, Class ofType) {
    return ofType.isAssignableFrom(clazz) && clazz.isAnnotationPresent(annotation);
  }
}
