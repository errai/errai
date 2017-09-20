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

/**
 * @author Tiago Bento <tfernand@redhat.com>
 */
@ErraiModule(bindableTypes = ErraiTestCustomModule1.Bindable1.class,
             serializableTypes = ErraiTestCustomModule1.Serializable1.class,
             nonSerializableTypes = ErraiTestCustomModule1.NonSerializable1.class,
             iocAlternatives = ErraiTestCustomModule1.IocAlternative1.class,
             iocBlacklist = ErraiTestCustomModule1.IocBlacklisted1.class,
             iocWhitelist = ErraiTestCustomModule1.IocWhitelisted1.class)
class ErraiTestCustomModule1 {

  static class Serializable1 {
  }

  static class NonSerializable1 {
  }

  static class Bindable1 {
  }

  static class IocAlternative1 {
  }

  static class IocBlacklisted1 {
  }

  static class IocWhitelisted1 {
  }
}
