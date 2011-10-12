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

import org.jboss.vfs.VirtualFile;
import org.reflections.vfs.SystemDir;
import org.reflections.vfs.Vfs;
import org.reflections.vfs.ZipDir;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * An {@link org.reflections.vfs.Vfs.UrlType} for deployment archives served through the JBoss virtual filesytem. <br/>
 * It simply delegates to {@link org.reflections.vfs.SystemDir} and
 * {@link org.reflections.vfs.ZipDir} respectively
 *
 * @author Heiko Braun <hbraun@redhat.com>
 * @author Mike Brock <cbrock@redhat.com>
 */
public class VFSUrlType implements Vfs.UrlType {
  protected static final Logger log = LoggerFactory.getLogger(VFSUrlType.class);

  final static boolean jbossAS;

  final static String VFS = "vfs";
  final static String VFSZIP = "vfszip";
  final static String VFSFILE = "vfsfile";

  static {
    boolean jbossFound;
    try {
      Class.forName("org.jboss.vfs.VirtualFile");
      jbossFound = true;
    }
    catch (ClassNotFoundException e) {
      jbossFound = false;
    }

    jbossAS = jbossFound;
  }

  public boolean matches(URL url) {
    return url.getProtocol().equals(VFS)
            || url.getProtocol().equals(VFSZIP)
            || url.getProtocol().equals(VFSFILE);
  }

  public Vfs.Dir createDir(URL url) {
    // Create non VFS Url
    File deployment = PackagingUtil.identifyDeployment(url);
    if (null == deployment)
      throw new RuntimeException("Unable identify deployment file for: " + url);

    File file = deployment.getAbsoluteFile();

    try {
      if (jbossAS) {
        try {
          if (url.getContent() instanceof VirtualFile) {
             return new JBossVFSDIr(url);
          }
        }
        catch (IOException e) {
          throw new RuntimeException("error reading from VFS", e);
        }
      }

      URL targetURL = file.toURI().toURL();

      // delegate unpacked archives to SystemDir handler
      if (deployment.isDirectory())
        return new SystemDir(targetURL);

      // if it's a file delegate to ZipDir handler
      return new ZipDir(targetURL);
    }
    catch (MalformedURLException e) {
      throw new RuntimeException("Invalid URL", e);
    }
  }

  private static URL toUrl(String s) {
    try {
      if (!s.startsWith("/")) {
        s = "/" + s;
      }

      return new URL(s);
    }
    catch (MalformedURLException e) {
      throw new RuntimeException("Invalid URL " + s, e);
    }
  }
}
