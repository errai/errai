/*
 * Copyright (C) 2012 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.bus.server.util;

import org.jboss.errai.common.metadata.RebindUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * @author Heiko Braun
 */
public abstract class AbstractConfigBase {
  protected static final Logger log = LoggerFactory.getLogger(AbstractConfigBase.class);
  protected static Map<String, File> scanAreas = new HashMap<String, File>();
  protected static Map<String, List<String>> scanCache = new HashMap<String, List<String>>();
  protected static Set<String> cacheBlackList = new HashSet<String>();
  protected static Set<String> activeCacheContexts = new HashSet<String>();
  protected static String tmpUUID = "erraiBootstrap_" + UUID.randomUUID().toString().replaceAll("\\-", "_");
  private static String CLASS_RESOURCES_ROOT = "WEB-INF.classes.";

  protected static void recordCache(String context, String cls) {
    if (scanCache == null || cacheBlackList.contains(context)) return;

    List<String> cache = scanCache.get(context);

    if (cache == null) {
      log.debug("caching context '" + context + "'");
      scanCache.put(context, cache = new LinkedList<String>());
    }

    cache.add(cls);
  }

  protected static void _traverseFiles(File root, File start, Set<String> loadedTargets, VisitDelegate visitor) {
    if (start.getPath().endsWith(".svn")) return;
    if (start.isDirectory()) {
      loadFromDirectory(root, start, loadedTargets, visitor);
    }
    else if (start.isFile()) {
      loadFromZippedResource(root, start, loadedTargets, visitor, null);
    }
    else {
      /**
       * This path is not directly resolvable to a directory or file target, which may mean that it's a
       * virtual path.  So in order to over-come this, we'll perform a brute-force reparsing of the URI
       * until we find something to work with.
       */
      findValidPath(root, start, loadedTargets, visitor);
    }
  }

  protected static void findValidPath(File root, File start, Set<String> loadedTargets, VisitDelegate visitor) {
    String originalPath = start.getPath();
    int pivotPoint;

    String rootPath;
    do {
      start = new File((rootPath = start.getPath())
          .substring(0, (pivotPoint = rootPath.lastIndexOf("/")) < 0 ? 0 : pivotPoint));

    } while (!start.isFile() && pivotPoint > 0);

    if (start.isFile()) {
      loadFromZippedResource(root, start, loadedTargets, visitor, originalPath.substring(pivotPoint + 1));
    }
  }

  protected static InputStream findResource(ClassLoader loader, String resourceName) {
    ClassLoader cl = loader;
    InputStream is;

    while ((is = cl.getResourceAsStream(resourceName)) == null && (cl = cl.getParent()) != null) ;

    return is;
  }

  protected static String getCandidateFQCN(String rootFile, String fileName) {
    return fileName.replaceAll("(/|\\\\)", ".")
        .substring(rootFile.length() + 1, fileName.lastIndexOf('.'));
  }

  protected static void loadFromZippedResource(File root, File start, Set<String> loadedTargets, VisitDelegate visitor,
                                               String scanFilter) {

    final String pathToJar = start.getPath();
    boolean startsWithFile = pathToJar.startsWith("file:/");

    InputStream inStream = null;
    try {
      if (!pathToJar.matches(".+\\.(zip|jar|war)$")) return;

      int startIdx = startsWithFile ? 5 : 0;
      int endIdx = pathToJar.lastIndexOf(".jar");
      if (endIdx == -1) endIdx = pathToJar.lastIndexOf(".war");
      if (endIdx == -1) endIdx = pathToJar.lastIndexOf(".zip");

      if (endIdx == -1) {
        endIdx = pathToJar.length() - 1;
      }
      else {
        endIdx += 4;
      }

      String jarName = pathToJar.substring(startIdx, endIdx);

      inStream = findResource(AbstractConfigBase.class.getClassLoader(), jarName.replaceAll("/", "\\."));

      if (inStream == null) {
        /**
         * Try to load this directly as a file.
         */
        inStream = new FileInputStream(jarName);
      }
      loadZipFromStream(jarName, inStream, loadedTargets, visitor, scanFilter);

    }
    catch (Exception e) {
      log.warn("did not process '" + pathToJar + "' (probably non-fatal)", e);
    }
    finally {
      try {
        if (inStream != null) inStream.close();
      }
      catch (Exception e) {
        log.error("failed to close stream", e);
      }
    }
  }

  protected static void loadZipFromStream(String zipName, InputStream inStream, Set<String> loadedTargets,
                                          VisitDelegate visitor, String scanFilter) throws IOException {
    ZipInputStream zipFile = new ZipInputStream(inStream);
    ZipEntry zipEntry;

    String ctx = zipName + (scanFilter == null ? ":*" : ":" + scanFilter);

    boolean scanClass = ".class".equals(visitor.getFileExtension());

    if (activeCacheContexts != null && scanCache != null &&
        activeCacheContexts.contains(ctx) && scanCache.containsKey(ctx)) {
      List<String> cache = scanCache.get(ctx);
      for (String loadClass : cache) {
        visitor.visit(loadClass);
      }
    }
    else {
      while ((zipEntry = zipFile.getNextEntry()) != null) {
        if (scanFilter != null && !zipEntry.getName().startsWith(scanFilter)) continue;

        if (scanClass && zipEntry.getName().endsWith(".class")) {

          String classEntry;
          String className = null;
          boolean cached = false;
          try {
            classEntry = zipEntry.getName().replaceAll("/", "\\.");
            int beginIdx = classEntry.indexOf(CLASS_RESOURCES_ROOT);
            if (beginIdx == -1) {
              beginIdx = 0;
            }
            else {
              beginIdx += CLASS_RESOURCES_ROOT.length();
            }

            className = classEntry.substring(beginIdx, classEntry.lastIndexOf(".class"));

            visitor.visit(className);
            recordCache(ctx, className);
            cached = true;
          }
          catch (Throwable e) {
            if (!cached) {
              log.trace("Failed to load: " + className
                  + "(" + e.getMessage() + ") -- Probably non-fatal.");
            }
          }

        }
        else if (zipEntry.getName().matches(".+\\.(zip|jar|war)$")) {
          /**
           * Let's decompress this to a temp dir so we can look at it:
           */

          InputStream tmpZipStream = null;
          try {

            if (scanAreas.containsKey(zipEntry.getName())) {
              File tmpFile = scanAreas.get(zipEntry.getName());
              tmpZipStream = new FileInputStream(tmpFile);
              loadZipFromStream(tmpFile.getName(), tmpZipStream, loadedTargets, visitor, null);
            }
            else {
              File tmpUnZip = expandZipEntry(zipFile, zipEntry);

              scanAreas.put(zipEntry.getName(), tmpUnZip);

              tmpZipStream = new FileInputStream(tmpUnZip);

              loadZipFromStream(tmpUnZip.getName(), tmpZipStream, loadedTargets, visitor, null);
            }
          }
          finally {
            if (tmpZipStream != null) {
              tmpZipStream.close();
            }
          }
        }

      }

      activeCacheContexts.add(zipName);
    }
  }

  protected static File expandZipEntry(ZipInputStream stream, ZipEntry entry) {

    String tmpDir = RebindUtils.getTempDirectory() + "/" + tmpUUID;
    int idx = entry.getName().lastIndexOf('/');
    String tmpFileName = tmpDir + "/" + entry.getName().substring(idx == -1 ? 0 : idx);
    try {
      File tmpDirFile = new File(tmpDir);
      tmpDirFile.mkdirs();

      File newFile = new File(tmpFileName);

      FileOutputStream outStream = new FileOutputStream(newFile);
      byte[] buf = new byte[1024];
      int read;
      while ((read = stream.read(buf)) != -1) {
        outStream.write(buf, 0, read);
      }

      outStream.flush();
      outStream.close();

      newFile.getParentFile().deleteOnExit();

      return newFile;
    }
    catch (Exception e) {
      log.error("error reading from stream", e);
      return null;
    }
  }

  protected static void loadFromDirectory(File root, File start, Set<String> loadedTargets, VisitDelegate visitor) {
    if (scanCache != null && activeCacheContexts != null && activeCacheContexts.contains(root.getPath())
        && scanCache.containsKey(root.getPath())) {
      for (String loadClass : scanCache.get(root.getPath())) {
        visitor.visit(loadClass);
      }
    }
    else {
      for (File file : start.listFiles()) {
        if (file.isDirectory()) _traverseFiles(root, file, loadedTargets, visitor);
        if (file.getName().endsWith(".class")) {
          String FQCN = getCandidateFQCN(root.getAbsolutePath(), file.getAbsolutePath());
          try {
            if (loadedTargets.contains(FQCN)) {
              return;
            }
            else {
              loadedTargets.add(FQCN);
            }

            visitor.visit(FQCN);
            recordCache(root.getPath(), FQCN);

          }
          catch (Throwable t) {
            cacheBlackList.add(root.getPath());
            if (scanCache != null) scanCache.remove(root.getPath());
            visitor.visitError(FQCN, t);
          }
        }
      }
    }
  }
}
