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

package org.jboss.errai.common.apt.exportfile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.lang.model.element.PackageElement;
import javax.lang.model.util.Elements;
import java.util.Optional;

/**
 * @author Tiago Bento <tfernand@redhat.com>
 */
final class ErraiAptPackages {

  private static final Logger log = LoggerFactory.getLogger(ErraiAptPackages.class);

  private static final String EXPORT_FILES_PACKAGE_PATH = "org.jboss.errai.apt.internal.export";

  private ErraiAptPackages() {
  }

  public static String exportFilesPackagePath() {
    return EXPORT_FILES_PACKAGE_PATH;
  }

  static Optional<PackageElement> exportFilesPackageElement(final Elements elements) {
    final PackageElement packageElement = elements.getPackageElement(exportFilesPackagePath());

    if (packageElement == null) {
      log.error("Export files package not found.");
    }

    return Optional.ofNullable(packageElement);
  }
}
