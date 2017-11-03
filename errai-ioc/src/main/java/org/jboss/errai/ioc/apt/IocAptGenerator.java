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

package org.jboss.errai.ioc.apt;

import org.jboss.errai.common.apt.ErraiAptExportedTypes;
import org.jboss.errai.common.apt.ErraiAptGenerators;
import org.jboss.errai.common.apt.ResourceFilesFinder;
import org.jboss.errai.common.configuration.ErraiGenerator;
import org.jboss.errai.ioc.apt.util.AptIocRelevantClassesFinder;
import org.jboss.errai.ioc.rebind.ioc.bootstrapper.IOCGenerator;
import org.jboss.errai.ioc.rebind.ioc.bootstrapper.IocRelevantClassesFinder;

/**
 * @author Tiago Bento <tfernand@redhat.com>
 */
@ErraiGenerator
public class IocAptGenerator extends ErraiAptGenerators.SingleFile {

  private final IOCGenerator iocGenerator;
  private final ResourceFilesFinder resourceFilesFinder;

  // IMPORTANT: Do not remove. ErraiAppAptGenerator depends on this constructor
  public IocAptGenerator(final ErraiAptExportedTypes exportedTypes) {
    super(exportedTypes);
    this.resourceFilesFinder = exportedTypes.resourceFilesFinder();
    this.iocGenerator = new IOCGenerator();
  }

  @Override
  public String generate() {
    final IocRelevantClassesFinder relevantClasses = new AptIocRelevantClassesFinder(metaClassFinder());
    return iocGenerator.generate(null, metaClassFinder(), erraiConfiguration(), relevantClasses, resourceFilesFinder,
            getResolvedClassSimpleName());
  }

  @Override
  public String getPackageName() {
    return iocGenerator.getPackageName();
  }

  @Override
  public String getClassSimpleName() {
    return iocGenerator.getClassSimpleName();
  }
}
