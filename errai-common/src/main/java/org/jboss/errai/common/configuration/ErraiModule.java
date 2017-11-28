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

package org.jboss.errai.common.configuration;

/**
 * @author Tiago Bento <tfernand@redhat.com>
 */
public @interface ErraiModule {

  Class<?>[] bindableTypes() default {};

  Class<?>[] nonBindableTypes() default {};

  Class<?>[] serializableTypes() default {};

  Class<?>[] nonSerializableTypes() default {};

  Class<?>[] iocAlternatives() default {};

  Class<?>[] iocBlacklist() default {};

  Class<?>[] iocWhitelist() default {};

  MappingAlias[] mappingAliases() default {};

  String[] includes() default { ".*" };

  String[] excludes() default { "server.*" };

  interface Property {
    String BINDABLE_TYPES = "bindableTypes";
    String NON_BINDABLE_TYPES = "nonBindableTypes";
    String SERIALIZABLE_TYPES = "serializableTypes";
    String NON_SERIALIZABLE_TYPES = "nonSerializableTypes";
    String IOC_ALTERNATIVES = "iocAlternatives";
    String IOC_BLACKLIST = "iocBlacklist";
    String IOC_WHITELIST = "iocWhitelist";
    String MAPPING_ALIASES = "mappingAliases";
    String INCLUDES = "includes";
    String EXCLUDES = "excludes";
  }

}
