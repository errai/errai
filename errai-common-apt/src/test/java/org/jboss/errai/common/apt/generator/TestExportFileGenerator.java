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

import org.jboss.errai.codegen.meta.impl.apt.APTClass;
import org.jboss.errai.common.apt.AnnotatedSourceElementsFinder;
import org.jboss.errai.common.apt.strategies.ErraiExportingStrategiesFactory;

import javax.lang.model.util.Elements;

import static java.util.stream.Collectors.toSet;

/**
 * @author Tiago Bento <tfernand@redhat.com>
 */
class TestExportFileGenerator extends ExportFileGenerator {

  TestExportFileGenerator(final AnnotatedSourceElementsFinder annotatedSourceElementsFinder, final Elements elements) {
    super("test", annotatedSourceElementsFinder, new ErraiExportingStrategiesFactory(elements).buildFrom(),
            annotatedSourceElementsFinder.findSourceElementsAnnotatedWith(
                    org.jboss.errai.common.configuration.ErraiModule.class)
                    .stream()
                    .map(s -> new APTClass(s.asType()))
                    .collect(toSet()));
  }

}