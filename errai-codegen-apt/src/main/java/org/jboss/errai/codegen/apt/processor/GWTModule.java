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

package org.jboss.errai.codegen.apt.processor;

import static java.util.Arrays.deepHashCode;

import java.net.URL;
import java.util.Arrays;
import java.util.Objects;

/**
 *
 * @author Max Barkley <mbarkley@redhat.com>
 */
public class GWTModule {

  private final URL moduleXml;
  private final URL[] sourcePaths;
  private final String[] inheritedModuleNames;

  public GWTModule(final URL moduleXml, final URL[] sourcePaths, final String[] inheritedModuleNames) {
    this.moduleXml = moduleXml;
    this.sourcePaths = sourcePaths;
    this.inheritedModuleNames = inheritedModuleNames;
  }

  public URL getModuleXml() {
    return moduleXml;
  }

  public URL[] getSourcePaths() {
    return sourcePaths;
  }

  public String[] getInheritedModuleNames() {
    return inheritedModuleNames;
  }

  @Override
  public int hashCode() {
    return deepHashCode(new Object[] { moduleXml, sourcePaths, inheritedModuleNames });
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj instanceof GWTModule) {
      final GWTModule other = (GWTModule) obj;
      return Objects.equals(moduleXml, other.moduleXml) && Objects.deepEquals(sourcePaths, other.sourcePaths)
              && Objects.deepEquals(inheritedModuleNames, other.inheritedModuleNames);
    }

    return false;
  }

  @Override
  public String toString() {
    return String.format("GWTModule{moduleXml=%s, sourcePaths=%s, inherited=%s", moduleXml,
            Arrays.toString(sourcePaths), Arrays.toString(inheritedModuleNames));
  }

}
