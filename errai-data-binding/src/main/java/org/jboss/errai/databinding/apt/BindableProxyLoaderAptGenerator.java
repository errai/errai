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

package org.jboss.errai.databinding.apt;

import org.jboss.errai.common.apt.ErraiAptExportedTypes;
import org.jboss.errai.common.apt.ErraiAptGenerators;
import org.jboss.errai.common.configuration.ErraiGenerator;
import org.jboss.errai.databinding.client.api.Bindable;
import org.jboss.errai.databinding.rebind.BindableProxyLoaderGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * IMPORTANT: Do not move this class. ErraiAppAptGenerator depends on it being in this exact package.
 *
 * @author Tiago Bento <tfernand@redhat.com>
 */
@ErraiGenerator
public class BindableProxyLoaderAptGenerator extends ErraiAptGenerators.SingleFile {

  private static final Logger log = LoggerFactory.getLogger(BindableProxyLoaderAptGenerator.class);

  private final BindableProxyLoaderGenerator bindableProxyLoaderGenerator;

  // IMPORTANT: Do not remove. ErraiAppAptGenerator depends on this constructor
  public BindableProxyLoaderAptGenerator(final ErraiAptExportedTypes exportedTypes) {
    super(exportedTypes);
    this.bindableProxyLoaderGenerator = new BindableProxyLoaderGenerator();
  }

  @Override
  public String generate() {
    log.info("Generating {}...", getClassSimpleName());

    final String generatedSource = bindableProxyLoaderGenerator.generate(
            metaClassFinder().extend(Bindable.class, erraiConfiguration().modules()::getBindableTypes)
                    .remove(Bindable.class, erraiConfiguration().modules()::getNonBindableTypes));

    log.info("Generated {}", getClassSimpleName());
    return generatedSource;
  }

  @Override
  public String getPackageName() {
    return bindableProxyLoaderGenerator.getPackageName();
  }

  @Override
  public String getClassSimpleName() {
    return bindableProxyLoaderGenerator.getClassSimpleName();
  }

}
