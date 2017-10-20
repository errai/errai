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

package org.jboss.errai.config.propertiesfile;

import org.jboss.errai.config.ErraiAppConfiguration;
import org.jboss.errai.config.ErraiConfiguration;
import org.jboss.errai.config.ErraiModulesConfiguration;

/**
 * @author Tiago Bento <tfernand@redhat.com>
 */
public class ErraiAppPropertiesConfiguration implements ErraiConfiguration {

  private final ErraiAppConfiguration app;
  private final ErraiModulesConfiguration modules;

  public ErraiAppPropertiesConfiguration() {
    this.modules = new ErraiAppPropertiesErraiModulesConfiguration();
    this.app = new ErraiAppPropertiesErraiAppConfiguration();
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
