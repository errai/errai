/*
 * Copyright (C) 2015 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.ioc.tests.extensions.client.res;

import javax.inject.Inject;

import org.jboss.errai.common.client.api.annotations.NamedLogger;
import org.jboss.errai.ioc.client.api.EntryPoint;
import org.slf4j.Logger;

@EntryPoint
public class ClassWithNamedLoggerField {

  public static final String LOGGER_NAME = "a unique logger name!!!";
  
  @Inject @NamedLogger(LOGGER_NAME) private Logger logger;
  
  public Logger getLogger() {
    return logger;
  }
}
