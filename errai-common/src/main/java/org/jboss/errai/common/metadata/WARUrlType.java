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

import org.reflections.vfs.SystemDir;
import org.reflections.vfs.Vfs;
import org.reflections.vfs.ZipDir;

import java.io.File;
import java.net.URL;

/**
 * An {@link org.reflections.vfs.Vfs.UrlType} for scanning web application archives.
 * It simply delegates to {@link org.reflections.vfs.SystemDir} and
 * {@link org.reflections.vfs.ZipDir} respectively
 *
 * @author: Heiko Braun <hbraun@redhat.com>
 * @date: Aug 9, 2010
 */
public class WARUrlType implements Vfs.UrlType {
  public boolean matches(URL url) {
    return url.getProtocol().equals("file") && url.toExternalForm().endsWith(".war");
  }

  public Vfs.Dir createDir(URL url) {
    File file = new File(url.toExternalForm());

    if (file.isDirectory())
      return new SystemDir(url);
    else
      return new ZipDir(url);
  }
}
