/*
 * Copyright (C) 2017 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.ui.rebind;

import org.apache.http.impl.io.EmptyInputStream;
import org.jboss.errai.common.apt.generator.app.CodeGenResource;
import org.jboss.errai.common.apt.generator.app.ResourceFilesFinder;
import org.jboss.errai.common.client.api.Assert;
import org.lesscss.Resource;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Tiago Bento <tfernand@redhat.com>
 */
public class ResourceFilesFinderResource implements Resource {

  private final String path;
  private final ResourceFilesFinder resourcesFilesFinder;

  public ResourceFilesFinderResource(final String path, final ResourceFilesFinder resourcesFilesFinder) {
    this.path = Assert.notNull(path);
    this.resourcesFilesFinder = Assert.notNull(resourcesFilesFinder);
  }

  @Override
  public boolean exists() {
    return resourcesFilesFinder.getResource(path).isPresent();
  }

  @Override
  public long lastModified() {
    return resourcesFilesFinder.getResource(path).flatMap(CodeGenResource::lastModified).orElse(0L);
  }

  @Override
  public InputStream getInputStream() throws IOException {
    return resourcesFilesFinder.getResource(path).flatMap(CodeGenResource::getInputStream).orElse(EmptyInputStream.INSTANCE);
  }

  @Override
  public Resource createRelative(final String relativeResourcePath) throws IOException {
    final String newPath;
    if (relativeResourcePath.startsWith("/")) {
      newPath = relativeResourcePath.substring(1);
    } else {
      final int endOfParentPath = path.lastIndexOf('/');
      final String parentPath = path.substring(0, endOfParentPath + 1);
      newPath = parentPath + relativeResourcePath;
    }
    return new ResourceFilesFinderResource(newPath, resourcesFilesFinder);
  }

  @Override
  public String getName() {
    return path;
  }

  @Override
  public String toString() {
    return String.format("[%s]", path);
  }

}
