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

package org.jboss.errai.forge.config;

import java.io.File;

/**
 * An enumeration of project properties stored in a {@link ProjectConfig}.
 * 
 * @author Max Barkley <mbarkley@redhat.com>
 */
public enum ProjectProperty {
  MODULE_FILE(File.class),
  MODULE_LOGICAL(String.class),
  /*
   * This can be different than the logical name if the module uses the
   * "rename-to" attribute.
   */
  MODULE_NAME(String.class),
  INSTALLED_FEATURES(SerializableSet.class),
  ERRAI_VERSION(String.class),
  CORE_IS_INSTALLED(Boolean.class);

  public final Class<?> valueType;

  private ProjectProperty(Class<?> type) {
    valueType = type;
  }
}
