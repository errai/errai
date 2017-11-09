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

package org.jboss.errai.common.apt;

import javax.annotation.processing.Filer;
import javax.tools.JavaFileManager;
import javax.tools.StandardLocation;
import java.io.File;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

/**
 * @author Tiago Bento <tfernand@redhat.com>
 */
public class AptResourceFilesFinder implements ResourceFilesFinder {

  private static final List<JavaFileManager.Location> LOCATIONS_TO_SEARCH = Arrays.asList(StandardLocation.values());

  private final Filer filer;

  public AptResourceFilesFinder(final Filer filer) {
    this.filer = filer;
  }

  @Override
  public Optional<File> getResource(final String path) {

    final int lastSlashIndex = path.lastIndexOf("/");
    final String packageName = path.substring(0, lastSlashIndex).replace("/", ".");
    final String fileName = path.substring(lastSlashIndex + 1);

    final Set<URI> possibleUris = LOCATIONS_TO_SEARCH.stream()
            .map(location -> getUri(location, packageName, fileName))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(toSet());

    return possibleUris.stream()
            .map(File::new)
            .filter(File::exists)
            .findFirst();
  }

  private Optional<URI> getUri(final JavaFileManager.Location location,
          final String packageName,
          final String fileName) {

    try {
      return Optional.ofNullable(filer.getResource(location, packageName, fileName).toUri());
    } catch (final Exception e) {
      return Optional.empty();
    }
  }
}
