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

package org.jboss.errai.common.apt.configuration;

import org.jboss.errai.common.configuration.ErraiModule;
import org.jboss.errai.config.ErraiAppConfiguration;
import org.jboss.errai.config.ErraiConfiguration;
import org.jboss.errai.config.ErraiModulesConfiguration;
import org.jboss.errai.config.MetaClassFinder;

import java.util.Optional;

import static java.util.stream.Collectors.toSet;

/**
 * @author Tiago Bento <tfernand@redhat.com>
 */
public class AptErraiConfiguration implements ErraiConfiguration {

  private final ErraiModulesConfiguration modules;
  private final ErraiAppConfiguration app;

  public AptErraiConfiguration(final ErraiAppConfiguration erraiAppConfiguration,
          final MetaClassFinder metaClassFinder) {
    
    this.app = erraiAppConfiguration;
    this.modules = new AptErraiModulesConfiguration(metaClassFinder.findAnnotatedWith(ErraiModule.class)
            .stream()
            .map(m -> m.getAnnotation(ErraiModule.class))
            .map(Optional::get)
            .collect(toSet()));
  }

  @Override
  public ErraiModulesConfiguration modules() {
    return modules;
  }

  @Override
  public ErraiAppConfiguration app() {
    return app;
  }
}
