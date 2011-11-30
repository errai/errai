/*
 * Copyright 2011 JBoss, by Red Hat, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.marshalling.rebind;

import org.jboss.errai.codegen.framework.meta.MetaClass;
import org.jboss.errai.common.metadata.MetaDataProcessor;
import org.jboss.errai.common.metadata.MetaDataScanner;
import org.jboss.errai.common.metadata.ScannerSingleton;
import org.jboss.errai.marshalling.rebind.api.CustomMapping;
import org.jboss.errai.marshalling.rebind.api.model.MappingDefinition;
import org.jboss.errai.marshalling.rebind.api.model.impl.AccessorMapping;
import org.jboss.errai.marshalling.rebind.api.model.impl.ReadMapping;
import org.jboss.errai.marshalling.rebind.api.model.impl.SimpleConstructorMapping;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Mike Brock
 */
public class DefinitionsFactory {
  private static final Map<String, MappingDefinition> MAPPING_DEFINITIONS
          = new HashMap<String, MappingDefinition>();

  static {
    loadDefinitions();
  }

  public static boolean hasDefinition(MetaClass clazz) {
    return MAPPING_DEFINITIONS.containsKey(clazz.getCanonicalName());
  }

  public static boolean hasDefinition(Class<?> clazz) {
    return MAPPING_DEFINITIONS.containsKey(clazz.getCanonicalName());
  }

  public static void addDefinition(MappingDefinition definition) {
    MAPPING_DEFINITIONS.put(definition.getMappingClass().getCanonicalName(), definition);
  }

  public static MappingDefinition getDefinition(MetaClass clazz) {
    return MAPPING_DEFINITIONS.get(clazz.getCanonicalName());
  }

  public static MappingDefinition getDefinition(Class<?> clazz) {
    return MAPPING_DEFINITIONS.get(clazz.getCanonicalName());
  }

  private static void loadDefinitions() {
    MetaDataScanner scanner = ScannerSingleton.getOrCreateInstance();

    for (Class<?> cls : scanner.getTypesAnnotatedWith(CustomMapping.class)) {
      if (!MappingDefinition.class.isAssignableFrom(cls)) {
        throw new RuntimeException("@CustomMapping class: " + cls.getName() + " does not inherit " + MappingDefinition.class.getName());
      }

      try {
        MappingDefinition definition = (MappingDefinition) cls.newInstance();
        addDefinition(definition);
      }
      catch (Throwable t) {
        throw new RuntimeException("Failed to load definition", t);
      }
    }
  }
}
