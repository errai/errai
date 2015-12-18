/*
 * Copyright (C) 2014 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.forge.constant;

/**
 * @author Max Barkley <mbarkley@redhat.com>
 */
public class PomPropertyVault {

  /**
   * An enumeration of Maven pom file properties.
   * 
   * @author Max Barkley <mbarkley@redhat.com>
   */
  public static enum Property {
    JbossHome("errai.jboss.home"),
    ErraiVersion("errai.version"),
    DevContext("errai.dev.context");

    private String name;

    private Property(final String name) {
      this.name = name;
    }

    /**
     * @return The name of this property.
     */
    public String getName() {
      return name;
    }

    /**
     * @return An invocation of this property (i.e. for a value named
     *         {@code some.prop} this would return <code>${some.prop}</code>.
     */
    public String invoke() {
      return "${" + name + "}";
    }
  }

}
