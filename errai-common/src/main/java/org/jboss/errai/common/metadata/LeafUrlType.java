/*
 * Copyright (C) 2009 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.common.metadata;

import java.net.URL;
import java.util.Collections;

import org.jboss.errai.common.client.api.Assert;
import org.jboss.errai.reflections.vfs.Vfs;

/**
 * A trivial URL Type handler for Reflections VFS which matches any classpath
 * entry ending with a given suffix (or filename extension) and returns an empty
 * VFS directory. When used with the extension ".jnilib", this helps silence a
 * bunch of warnings that happen when you compile an Errai app on a Mac OS X
 * JVM.
 * <p>
 * Instances are fully immutable and therefore threadsafe.
 */
public final class LeafUrlType implements Vfs.UrlType {

  private final String urlSuffix;

  /**
   * A VFS directory rooted at a given URL which does not contain any entries.
   *
   * @author Jonathan Fuerth <jfuerth@redhat.com>
   */
  private static final class EmptyVfsDir implements Vfs.Dir {
    private final URL url;

    private EmptyVfsDir(URL url) {
      this.url = url;
    }

    @Override
    public String getPath() {
      return url.getPath();
    }

    @Override
    public Iterable<org.jboss.errai.reflections.vfs.Vfs.File> getFiles() {
      return Collections.emptyList();
    }

    @Override
    public void close() {
      // no op
    }
  }

  /**
   * Creates a new LeafUrlType that matches URLs with the given suffix.
   *
   * @param urlSuffix the filename suffix to match. Must not be null.
   */
  public LeafUrlType(String urlSuffix) {
    this.urlSuffix = Assert.notNull(urlSuffix);
  }

  @Override
  public boolean matches(final URL url) {
    return url.toExternalForm().endsWith(urlSuffix);
  }

  @Override
  public Vfs.Dir createDir(final URL url) {
    return new EmptyVfsDir(url);
  }

  @Override
  public String toString() {
    return "*" + urlSuffix;
  }
}
