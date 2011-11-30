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
    loadDefaults();
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

  private static void loadDefaults() {
    SimpleConstructorMapping stackTraceMapping = new SimpleConstructorMapping(StackTraceElement.class);
    stackTraceMapping.mapParmToIndex("declaringClass", 0, String.class);
    stackTraceMapping.mapParmToIndex("methodName", 1, String.class);
    stackTraceMapping.mapParmToIndex("fileName", 2, String.class);
    stackTraceMapping.mapParmToIndex("lineNumber", 3, Integer.class);

    MappingDefinition stackTraceElementDef = new MappingDefinition(StackTraceElement.class, stackTraceMapping);
    stackTraceElementDef.setMarshal(false);
    addDefinition(stackTraceElementDef);

    SimpleConstructorMapping throwableMapping = new SimpleConstructorMapping(Throwable.class);
    throwableMapping.mapParmToIndex("message", 0, String.class);
    throwableMapping.mapParmToIndex("cause", 1, Throwable.class);

    MappingDefinition throwableDef = new MappingDefinition(Throwable.class, throwableMapping);
    throwableDef.addMemberMapping(new AccessorMapping(Throwable.class, "stackTrace", StackTraceElement[].class, "setStackTrace", "getStackTrace"));
    throwableDef.addMemberMapping(new ReadMapping(Throwable.class, "message", String.class, "getMessage"));
    throwableDef.addMemberMapping(new ReadMapping(Throwable.class, "cause", Throwable.class, "getCause"));
    addDefinition(throwableDef);
  }

}
