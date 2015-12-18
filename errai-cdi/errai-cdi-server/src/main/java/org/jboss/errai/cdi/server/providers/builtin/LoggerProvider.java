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

package org.jboss.errai.cdi.server.providers.builtin;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

import org.jboss.errai.common.client.api.annotations.NamedLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggerProvider {
  
  

  @Produces
  public Logger produceLogger(final InjectionPoint injectionPoint) {
    final String loggerName;
    if (injectionPoint.getAnnotated().isAnnotationPresent(NamedLogger.class)) {
      loggerName = injectionPoint.getAnnotated().getAnnotation(NamedLogger.class).value();
    }
    else {
      loggerName = injectionPoint.getMember().getDeclaringClass().getName();
    }

    return LoggerFactory.getLogger(loggerName);
  }

}
