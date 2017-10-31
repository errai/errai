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

package org.jboss.errai.ui.nav.apt;

import org.jboss.errai.common.apt.ErraiAptExportedTypes;
import org.jboss.errai.common.apt.ErraiAptGenerators;
import org.jboss.errai.common.configuration.ErraiGenerator;
import org.jboss.errai.ui.nav.rebind.NavigationGraphGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * IMPORTANT: Do not move this class. ErraiAppAptGenerator depends on it being in this exact package.
 *
 * @author Tiago Bento <tfernand@redhat.com>
 */
@ErraiGenerator
public class NavigationGraphAptGenerator extends ErraiAptGenerators.SingleFile {

  public static final Logger logger = LoggerFactory.getLogger(NavigationGraphAptGenerator.class);

  private final NavigationGraphGenerator navigationGraphGenerator;

  // IMPORTANT: Do not remove. ErraiAppAptGenerator depends on this constructor
  public NavigationGraphAptGenerator(final ErraiAptExportedTypes exportedTypes) {
    super(exportedTypes);
    this.navigationGraphGenerator = new NavigationGraphGenerator();
  }

  @Override
  public String generate() {
    logger.info("Generating " + getClassSimpleName() + "...");
    final String generated = navigationGraphGenerator.generateSource(metaClassFinder(), getResolvedFullyQualifiedClassName());
    logger.info("Generated " + getClassSimpleName());
    return generated;
  }

  @Override
  public String getPackageName() {
    return NavigationGraphGenerator.PACKAGE_NAME;
  }

  @Override
  public String getClassSimpleName() {
    return NavigationGraphGenerator.CLASS_NAME;
  }
}
