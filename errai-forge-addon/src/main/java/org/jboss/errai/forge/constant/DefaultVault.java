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

public class DefaultVault {
  
  public static enum DefaultValue {
    SourceDirectory("sourceDirectory", "src/main/java"),
    ResourceDirectory("directory", "src/main/resources"),
    WarSourceDirectory("warSourceDirectory", "src/main/webapp");
    
    private final String valueName;
    private final String defaultValue;
    private DefaultValue(final String valueName, final String defaultValue) {
      this.valueName = valueName;
      this.defaultValue = defaultValue;
    }
    public String getDefaultValue() {
      return defaultValue;
    }
    public String getValueName() {
      return valueName;
    }
  }
  
  public static String getValue(final String value, final DefaultValue valueType) {
    return (value != null && !value.equals("")) ? value : valueType.getDefaultValue();
  }

}
