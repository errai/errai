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

import org.jboss.errai.codegen.meta.MetaAnnotation;
import org.jboss.errai.common.apt.MetaClassFinder;
import org.jboss.errai.common.configuration.ErraiApp;
import org.jboss.errai.config.ErraiAppConfiguration;

import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;
import static org.jboss.errai.common.configuration.ErraiApp.Property.APPLICATION_CONTEXT;
import static org.jboss.errai.common.configuration.ErraiApp.Property.ASYNC_BEAN_MANAGER;
import static org.jboss.errai.common.configuration.ErraiApp.Property.AUTO_DISCOVER_SERVICES;
import static org.jboss.errai.common.configuration.ErraiApp.Property.ENABLE_WEB_SOCKET_SERVER;
import static org.jboss.errai.common.configuration.ErraiApp.Property.USER_ON_HOST_PAGE_ENABLED;

/**
 * @author Tiago Bento <tfernand@redhat.com>
 */
public class AptErraiAppConfiguration implements ErraiAppConfiguration {

  private final MetaAnnotation erraiAppAnnotation;

  public AptErraiAppConfiguration(final MetaClassFinder metaClassFinder) {
    final List<MetaAnnotation> erraiAppMetaAnnotation = metaClassFinder.findAnnotatedWith(ErraiApp.class)
            .stream()
            .map(app -> app.getAnnotation(ErraiApp.class))
            .map(Optional::get)
            .collect(toList());

    if (erraiAppMetaAnnotation.size() > 1) {
      throw new RuntimeException("There should exist only one @ErraiApp annotated class.");
    }

    this.erraiAppAnnotation = erraiAppMetaAnnotation.get(0);
  }

  @Override
  public boolean isUserEnabledOnHostPage() {
    return erraiAppAnnotation.value(USER_ON_HOST_PAGE_ENABLED);
  }

  @Override
  public boolean isWebSocketServerEnabled() {
    return erraiAppAnnotation.value(ENABLE_WEB_SOCKET_SERVER);
  }

  @Override
  public boolean isAutoDiscoverServicesEnabled() {
    return erraiAppAnnotation.value(AUTO_DISCOVER_SERVICES);
  }

  @Override
  public String getApplicationContext() {
    return erraiAppAnnotation.value(APPLICATION_CONTEXT);
  }

  @Override
  public boolean asyncBeanManager() {
    return erraiAppAnnotation.value(ASYNC_BEAN_MANAGER);
  }

  @Override
  public boolean isAptEnvironment() {
    return true;
  }
}
