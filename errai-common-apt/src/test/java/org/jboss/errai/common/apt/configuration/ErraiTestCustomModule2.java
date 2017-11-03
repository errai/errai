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
import org.jboss.errai.common.configuration.MappingAlias;

import java.util.Collection;
import java.util.HashSet;

/**
 * @author Tiago Bento <tfernand@redhat.com>
 */
@ErraiModule(bindableTypes = ErraiTestCustomModule2.Bindable2.class,
             nonBindableTypes = ErraiTestCustomModule2.NonBindable2.class,
             serializableTypes = ErraiTestCustomModule2.Serializable2.class,
             nonSerializableTypes = ErraiTestCustomModule2.NonSerializable2.class,
             iocAlternatives = ErraiTestCustomModule2.IocAlternative2.class,
             iocBlacklist = ErraiTestCustomModule2.IocBlacklisted2.class,
             iocWhitelist = ErraiTestCustomModule2.IocWhitelisted2.class,
             mappingAliases = { @MappingAlias(from = HashSet.class, to = Collection.class) })
class ErraiTestCustomModule2 {

  static class Serializable2 {
  }

  static class NonSerializable2 {
  }

  static class Bindable2 {
  }

  static class NonBindable2 {
  }

  static class IocAlternative2 {
  }

  static class IocBlacklisted2 {
  }

  static class IocWhitelisted2 {
  }
}
