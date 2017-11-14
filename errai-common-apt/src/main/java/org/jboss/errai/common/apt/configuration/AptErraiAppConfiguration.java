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
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.common.configuration.ErraiApp;
import org.jboss.errai.config.ErraiAppConfiguration;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

import static org.jboss.errai.common.configuration.ErraiApp.Property.APPLICATION_CONTEXT;
import static org.jboss.errai.common.configuration.ErraiApp.Property.ASYNC_BEAN_MANAGER;
import static org.jboss.errai.common.configuration.ErraiApp.Property.AUTO_DISCOVER_SERVICES;
import static org.jboss.errai.common.configuration.ErraiApp.Property.CUSTOM_PROPERTIES;
import static org.jboss.errai.common.configuration.ErraiApp.Property.DYNAMIC_VALIDATION_ENABLED;
import static org.jboss.errai.common.configuration.ErraiApp.Property.ENABLE_WEB_SOCKET_SERVER;
import static org.jboss.errai.common.configuration.ErraiApp.Property.FORCE_STATIC_MARSHALLERS;
import static org.jboss.errai.common.configuration.ErraiApp.Property.GWT_MODULE_NAME;
import static org.jboss.errai.common.configuration.ErraiApp.Property.JS_INTEROP_SUPPORT_ENABLED;
import static org.jboss.errai.common.configuration.ErraiApp.Property.LAZY_LOAD_BUILTIN_MARSHALLERS;
import static org.jboss.errai.common.configuration.ErraiApp.Property.LOCAL;
import static org.jboss.errai.common.configuration.ErraiApp.Property.MAKE_DEFAULT_ARRAY_MARSHALLERS;
import static org.jboss.errai.common.configuration.ErraiApp.Property.MODULES;
import static org.jboss.errai.common.configuration.ErraiApp.Property.USER_ON_HOST_PAGE_ENABLED;
import static org.jboss.errai.common.configuration.ErraiApp.Property.USE_STATIC_MARSHALLERS;

/**
 * @author Tiago Bento <tfernand@redhat.com>
 */
public class AptErraiAppConfiguration implements ErraiAppConfiguration {

  private final MetaAnnotation erraiAppMetaAnnotation;
  private final MetaClass erraiAppAnnotatedMetaClass;

  public AptErraiAppConfiguration(final MetaClass erraiAppAnnotatedMetaClass) {
    this.erraiAppAnnotatedMetaClass = erraiAppAnnotatedMetaClass;
    this.erraiAppMetaAnnotation = erraiAppAnnotatedMetaClass.getAnnotation(ErraiApp.class)
            .orElseThrow(() -> new RuntimeException("Provided MetaClass must contain an @ErraiApp annotation"));
  }

  @Override
  public boolean isUserEnabledOnHostPage() {
    return erraiAppMetaAnnotation.value(USER_ON_HOST_PAGE_ENABLED);
  }

  @Override
  public boolean isWebSocketServerEnabled() {
    return erraiAppMetaAnnotation.value(ENABLE_WEB_SOCKET_SERVER);
  }

  @Override
  public boolean isAutoDiscoverServicesEnabled() {
    return erraiAppMetaAnnotation.value(AUTO_DISCOVER_SERVICES);
  }

  @Override
  public String getApplicationContext() {
    return erraiAppMetaAnnotation.value(APPLICATION_CONTEXT);
  }

  @Override
  public boolean asyncBeanManager() {
    return erraiAppMetaAnnotation.value(ASYNC_BEAN_MANAGER);
  }

  @Override
  public boolean forceStaticMarshallers() {
    return erraiAppMetaAnnotation.value(FORCE_STATIC_MARSHALLERS);
  }

  @Override
  public boolean useStaticMarshallers() {
    return erraiAppMetaAnnotation.value(USE_STATIC_MARSHALLERS);
  }

  @Override
  public boolean lazyLoadBuiltinMarshallers() {
    return erraiAppMetaAnnotation.value(LAZY_LOAD_BUILTIN_MARSHALLERS);
  }

  @Override
  public boolean makeDefaultArrayMarshallers() {
    return erraiAppMetaAnnotation.value(MAKE_DEFAULT_ARRAY_MARSHALLERS);
  }

  @Override
  public boolean jsInteropSupportEnabled() {
    return erraiAppMetaAnnotation.value(JS_INTEROP_SUPPORT_ENABLED);
  }

  @Override
  public boolean dynamicValidationEnabled() {
    return erraiAppMetaAnnotation.value(DYNAMIC_VALIDATION_ENABLED);
  }



  @Override
  public boolean isAptEnvironment() {
    return true;
  }

  public boolean local() {
    return erraiAppMetaAnnotation.value(LOCAL);
  }

  public String gwtModuleName() {
    return erraiAppMetaAnnotation.value(GWT_MODULE_NAME);
  }

  public Collection<MetaClass> modules() {
    return Arrays.asList(erraiAppMetaAnnotation.valueAsArray(MODULES, MetaClass[].class));
  }

  @Override
  public String namespace() {
    if (local()) {
      return erraiAppAnnotatedMetaClass.getCanonicalName().replace(".", "_") + "__";
    } else {
      return ErraiAppConfiguration.super.namespace();
    }
  }

  @Override
  public Optional<String> custom(final String propertyName) {
    return Arrays.stream(erraiAppMetaAnnotation.valueAsArray(CUSTOM_PROPERTIES, MetaAnnotation[].class))
            .filter(a -> a.value("name").equals(propertyName))
            .findFirst()
            .map(a -> a.value("value"));
  }

}
