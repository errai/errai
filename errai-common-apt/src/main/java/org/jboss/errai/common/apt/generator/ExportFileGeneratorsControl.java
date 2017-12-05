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

package org.jboss.errai.common.apt.generator;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Tiago Bento <tfernand@redhat.com>
 */
class ExportFileGeneratorsControl {

  private static Set<String> existentGeneratorsCount = new HashSet<>();
  private static Set<String> finishedGeneratorsCount = new HashSet<>();

  synchronized static void signalExistence(final AbstractExportFileGenerator exportFileGenerator) {
    existentGeneratorsCount.add(exportFileGenerator.getCamelCaseErraiModuleName());
  }

  synchronized static void signalReady(final AbstractExportFileGenerator exportFileGenerator) {
    finishedGeneratorsCount.add(exportFileGenerator.getCamelCaseErraiModuleName());
  }

  static boolean isReadyToGenerateActualCode() {
    return finishedGeneratorsCount.equals(existentGeneratorsCount);
  }
}
