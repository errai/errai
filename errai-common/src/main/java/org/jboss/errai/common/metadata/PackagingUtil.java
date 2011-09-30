/*
 * Copyright 2009 JBoss, a divison Red Hat, Inc
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
package org.jboss.errai.common.metadata;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * A set of utilities for processing a {@link DeploymentContext}
 *
 * @author: Heiko Braun <hbraun@redhat.com>
 * @date: Aug 9, 2010
 */
public class PackagingUtil {
  private static Logger log = LoggerFactory.getLogger(PackagingUtil.class);

  public static File identifyDeployment(URL url) {
    String actualFilePath = url.getPath();
    String nestedPath = "";
    if (actualFilePath.startsWith("file:")) {
      actualFilePath = actualFilePath.substring(5);
    }

    int nestedSeperator = actualFilePath.indexOf('!');
    if (nestedSeperator != -1) {
      nestedPath = actualFilePath.substring(nestedSeperator + 1);
      actualFilePath = actualFilePath.substring(0, nestedSeperator);

      if (nestedPath.equals("/")) {
        nestedPath = "";
      }
    }

    log.info("identifying deployment type for uri: " + actualFilePath);

    return findActualDeploymentFile(new File(actualFilePath));
  }

  private static URL toUrl(String s) {
    try {
      return new URL(s);
    }
    catch (MalformedURLException e) {
      throw new RuntimeException("Invalid URL " + s, e);
    }
  }

  static File findActualDeploymentFile(File start) {
    int pivotPoint;
    String rootPath = start.getPath();

    do {
      start = new File(rootPath);
      rootPath = rootPath.substring(0, (pivotPoint = rootPath.lastIndexOf("/")) < 0 ? 0 : pivotPoint);
    } while (!start.exists() && pivotPoint > 0);

    return start;
  }

  static void process(DeploymentContext ctx) {
    for (URL url : ctx.getConfigUrls()) {
      File file = PackagingUtil.identifyDeployment(url);

      /**
       * several config urls may derive from the same archive
       * don't process them twice
       */
      if (!ctx.hasProcessed(file)) {
        ctx.markProcessed(file);
        PackagingUtil.processNestedZip(file, ctx);
      }
    }
  }

  private static void processNestedZip(File file, DeploymentContext ctx) {
    try {
      if (file.getName().matches(".+\\.(ear|war|sar)$") && !file.isDirectory()) // process only certain deployment types
      {
        if (file.getName().endsWith(".war"))
          ctx.getSubContexts().put(file.getName(), file); // WEB-INF/classes

        ZipInputStream zipFile = new ZipInputStream(new FileInputStream(file));
        ZipEntry zipEntry = null;

        try {
          while ((zipEntry = zipFile.getNextEntry()) != null) {
            if (zipEntry.getName().matches(".+\\.(zip|jar|war)$")) // expand nested zip archives
            {
              if (!ctx.getSubContexts().containsKey(zipEntry.getName())) {
                File tmpUnZip = expandZipEntry(zipFile, zipEntry, ctx);
                ctx.getSubContexts().put(zipEntry.getName(), tmpUnZip);
                processNestedZip(tmpUnZip, ctx);
              }
            }
          }
        }
        finally {
          zipFile.close();
        }
      }
    }
    catch (Exception e) {
      throw new RuntimeException("Failed to process nested zip", e);
    }
  }

  protected static File expandZipEntry(ZipInputStream stream, ZipEntry entry, DeploymentContext ctx) {

    String tmpUUID = "erraiBootstrap_" + UUID.randomUUID().toString().replaceAll("\\-", "_");
    String tmpDir = System.getProperty("java.io.tmpdir") + "/" + tmpUUID;
    int idx = entry.getName().lastIndexOf('/');
    String tmpFileName = tmpDir + "/" + entry.getName().substring(idx == -1 ? 0 : idx);

    try {
      File tmpDirFile = new File(tmpDir);
      tmpDirFile.mkdirs();
      ctx.markTmpFile(tmpDirFile);

      File newFile = new File(tmpFileName);

      FileOutputStream outStream = new FileOutputStream(newFile);
      byte[] buf = new byte[1024];
      int read;
      while ((read = stream.read(buf)) != -1) {
        outStream.write(buf, 0, read);
      }

      outStream.flush();
      outStream.close();

      newFile.getParentFile();

      return newFile;
    }
    catch (Exception e) {
      throw new RuntimeException("Error reading from stream", e);
    }
  }
}
