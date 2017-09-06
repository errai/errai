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

import javax.lang.model.element.PackageElement;
import javax.lang.model.util.Elements;
import java.util.Optional;

/**
 * @author Tiago Bento <tfernand@redhat.com>
 */
public final class ErraiAptPackages {

  private static final String EXPORT_FILES_PACKAGE_PATH = "org.jboss.errai.apt.internal.export";
  private static final String EXPORTED_ANNOTATIONS_PACKAGE_PATH = "org.jboss.errai.apt.internal.export.annotation";
  private static final String GENERATORS_PACKAGE_PACKAGE_PATH = "org.jboss.errai.apt.internal.generator";

  private ErraiAptPackages() {
  }

  public static String exportFilesPackagePath() {
    return EXPORT_FILES_PACKAGE_PATH;
  }

  public static String exportedAnnotationsPackagePath() {
    return EXPORTED_ANNOTATIONS_PACKAGE_PATH;
  }

  public static String generatorsPackagePath() {
    return GENERATORS_PACKAGE_PACKAGE_PATH;
  }

  public static Optional<PackageElement> exportFilesPackageElement(final Elements elementUtils) {
    final PackageElement packageElement = elementUtils.getPackageElement(exportFilesPackagePath());

    if (packageElement == null) {
      System.out.println("Export files package not found");
    }

    return Optional.ofNullable(packageElement);
  }

  public static Optional<PackageElement> exportedAnnotationsPackageElement(final Elements elementUtils) {
    final PackageElement packageElement = elementUtils.getPackageElement(exportedAnnotationsPackagePath());

    if (packageElement == null) {
      System.out.println("Exported annotations package not found");
    }

    return Optional.ofNullable(packageElement);
  }

  public static Optional<PackageElement> generatorsPackageElement(final Elements elementUtils) {
    final PackageElement packageElement = elementUtils.getPackageElement(generatorsPackagePath());

    if (packageElement == null) {
      System.out.println("Generators package not found");
    }

    return Optional.ofNullable(packageElement);
  }

}
