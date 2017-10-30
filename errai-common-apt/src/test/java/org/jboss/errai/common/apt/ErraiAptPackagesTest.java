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

import org.jboss.errai.apt.internal.export.annotation.TestExportedAnnotations;
import org.jboss.errai.apt.internal.export.test_hYbKGF_ExportFile_org_jboss_errai_common_apt_exportfile_TestAnnotation;
import org.jboss.errai.codegen.apt.test.ErraiAptTest;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * @author Tiago Bento <tfernand@redhat.com>
 */
public class ErraiAptPackagesTest extends ErraiAptTest {

  @Test
  public void testPackagesExist() {
    assertTrue(ErraiAptPackages.exportFilesPackageElement(elements).isPresent());
    assertTrue(ErraiAptPackages.exportedAnnotationsPackageElement(elements).isPresent());
  }

  @Test
  public void testElementsInPackage() {
    assertTrue(ErraiAptPackages.exportFilesPackageElement(elements)
            .map(p -> p.getEnclosedElements()
                    .contains(getTypeElement(
                            test_hYbKGF_ExportFile_org_jboss_errai_common_apt_exportfile_TestAnnotation.class)))
            .orElse(false));

    assertTrue(ErraiAptPackages.exportedAnnotationsPackageElement(elements)
            .map(p -> p.getEnclosedElements().contains(getTypeElement(TestExportedAnnotations.class)))
            .orElse(false));
  }
}