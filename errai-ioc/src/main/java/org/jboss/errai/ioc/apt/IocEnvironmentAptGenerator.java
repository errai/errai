/*
 * Copyright (C) 2013 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.ioc.apt;

import org.jboss.errai.common.apt.exportfile.ExportedTypesFromExportFiles;
import org.jboss.errai.common.apt.ErraiAptGenerators;
import org.jboss.errai.common.apt.generator.ErraiAptGeneratedSourceFile;
import org.jboss.errai.common.configuration.ErraiGenerator;
import org.jboss.errai.ioc.rebind.ioc.bootstrapper.IOCEnvironmentGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.jboss.errai.common.apt.generator.ErraiAptGeneratedSourceFile.Type.CLIENT;

/**
 * @author Tiago Bento <tfernand@redhat.com>
 */
@ErraiGenerator
public class IocEnvironmentAptGenerator extends ErraiAptGenerators.SingleFile {

  private static final Logger log = LoggerFactory.getLogger(IocEnvironmentAptGenerator.class);

  private final IOCEnvironmentGenerator iocEnvironmentGenerator;

  // IMPORTANT: Do not remove. ErraiAppAptGenerator depends on this constructor
  public IocEnvironmentAptGenerator(final ExportedTypesFromExportFiles exportedTypes) {
    super(exportedTypes);
    this.iocEnvironmentGenerator = new IOCEnvironmentGenerator();
  }

  @Override
  public String generate() {
    log.info("Generating {}...", getClassSimpleName());
    final String generatedSource = iocEnvironmentGenerator.generate(erraiConfiguration(), getResolvedFullyQualifiedClassName());
    log.info("Generated {}", getClassSimpleName());
    return generatedSource;
  }

  @Override
  public String getPackageName() {
    return IOCEnvironmentGenerator.PACKAGE_NAME;
  }

  @Override
  public String getClassSimpleName() {
    return IOCEnvironmentGenerator.CLASS_NAME;
  }

  @Override
  public ErraiAptGeneratedSourceFile.Type getType() {
    return CLIENT;
  }
}
