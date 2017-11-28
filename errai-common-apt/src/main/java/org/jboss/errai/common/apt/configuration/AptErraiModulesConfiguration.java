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
import org.jboss.errai.common.configuration.ErraiModule;
import org.jboss.errai.config.ErraiModulesConfiguration;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static org.jboss.errai.common.configuration.ErraiModule.Property.BINDABLE_TYPES;
import static org.jboss.errai.common.configuration.ErraiModule.Property.EXCLUDES;
import static org.jboss.errai.common.configuration.ErraiModule.Property.INCLUDES;
import static org.jboss.errai.common.configuration.ErraiModule.Property.IOC_ALTERNATIVES;
import static org.jboss.errai.common.configuration.ErraiModule.Property.IOC_BLACKLIST;
import static org.jboss.errai.common.configuration.ErraiModule.Property.IOC_WHITELIST;
import static org.jboss.errai.common.configuration.ErraiModule.Property.MAPPING_ALIASES;
import static org.jboss.errai.common.configuration.ErraiModule.Property.NON_BINDABLE_TYPES;
import static org.jboss.errai.common.configuration.ErraiModule.Property.NON_SERIALIZABLE_TYPES;
import static org.jboss.errai.common.configuration.ErraiModule.Property.SERIALIZABLE_TYPES;

/**
 * @author Tiago Bento <tfernand@redhat.com>
 */
public class AptErraiModulesConfiguration implements ErraiModulesConfiguration {

  private final Set<MetaAnnotation> erraiModuleMetaAnnotations;

  public AptErraiModulesConfiguration(final Set<MetaClass> erraiModuleMetaClasses) {
    this.erraiModuleMetaAnnotations = erraiModuleMetaClasses.stream()
            .map(m -> m.getAnnotation(ErraiModule.class))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(toSet());
  }

  @Override
  public Set<MetaClass> getBindableTypes() {
    return getConfiguredArrayProperty(a -> stream(a.valueAsArray(BINDABLE_TYPES, MetaClass[].class)));
  }

  @Override
  public Set<MetaClass> getNonBindableTypes() {
    return getConfiguredArrayProperty(a -> stream(a.valueAsArray(NON_BINDABLE_TYPES, MetaClass[].class)));
  }

  @Override
  public Set<MetaClass> portableTypes() {
    return getConfiguredArrayProperty(a -> stream(a.valueAsArray(SERIALIZABLE_TYPES, MetaClass[].class)));
  }

  @Override
  public Set<MetaClass> nonPortableTypes() {
    return getConfiguredArrayProperty(a -> stream(a.valueAsArray(NON_SERIALIZABLE_TYPES, MetaClass[].class)));
  }

  @Override
  public Set<MetaClass> getIocEnabledAlternatives() {
    return getConfiguredArrayProperty(a -> stream(a.valueAsArray(IOC_ALTERNATIVES, MetaClass[].class)));
  }

  @Override
  public Set<MetaClass> getIocBlacklist() {
    return getConfiguredArrayProperty(a -> stream(a.valueAsArray(IOC_BLACKLIST, MetaClass[].class)));
  }

  @Override
  public Set<MetaClass> getIocWhitelist() {
    return getConfiguredArrayProperty(a -> stream(a.valueAsArray(IOC_WHITELIST, MetaClass[].class)));
  }

  public Set<String> includes() {
    return getConfiguredArrayProperty(a -> stream(a.valueAsArray(INCLUDES, String[].class)));
  }

  public Set<String> excludes() {
    return getConfiguredArrayProperty(a -> stream(a.valueAsArray(EXCLUDES, String[].class)));
  }

  @Override
  public Map<String, String> getMappingAliases() {
    return getConfiguredArrayProperty(x -> stream(x.valueAsArray(MAPPING_ALIASES, MetaAnnotation[].class))).stream()
            .collect(toMap(a -> a.<MetaClass>value("from").getFullyQualifiedName(),
                    a -> a.<MetaClass>value("to").getFullyQualifiedName()));
  }



  private <V> Set<V> getConfiguredArrayProperty(final Function<MetaAnnotation, Stream<V>> getter) {
    return erraiModuleMetaAnnotations.stream().flatMap(getter).collect(toSet());
  }
}
