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

package org.jboss.errai.marshalling.rebind.util;

import org.jboss.errai.codegen.framework.meta.MetaClass;
import org.jboss.errai.codegen.framework.meta.MetaClassFactory;
import org.jboss.errai.codegen.framework.meta.MetaMethod;
import org.jboss.errai.codegen.framework.util.GenUtil;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class MarshallingGenUtil {
  @Deprecated
  /**
   * Use 'errai.marshalling.serializableTypes' now.
   */
  public static final String CONFIG_ERRAI_OLD_SERIALIZABLE_TYPE = "errai.bus.serializableTypes";

  public static final String CONFIG_ERRAI_SERIALIZABLE_TYPE = "errai.marshalling.serializableTypes";
  public static final String CONFIG_ERRAI_MAPPING_ALIASES = "errai.marshalling.mappingAliases";

  public static String getVarName(MetaClass clazz) {
    return clazz.isArray()
            ? getArrayVarName(clazz.getOuterComponentType().getFullyQualifiedName())
            + "_D" + GenUtil.getArrayDimensions(clazz)
            : getVarName(clazz.asBoxed().getFullyQualifiedName());
  }

  public static String getVarName(Class<?> clazz) {
    return getVarName(MetaClassFactory.get(clazz));
  }

  private static final String ARRAY_VAR_PREFIX = "arrayOf_";

  public static String getArrayVarName(String clazz) {
    char[] newName = new char[clazz.length() + ARRAY_VAR_PREFIX.length()];
    _replaceAllDotsWithUnderscores(ARRAY_VAR_PREFIX, newName, 0);
    _replaceAllDotsWithUnderscores(clazz, newName, ARRAY_VAR_PREFIX.length());
    return new String(newName);
  }

  public static String getVarName(String clazz) {
    char[] newName = new char[clazz.length()];
    _replaceAllDotsWithUnderscores(clazz, newName, 0);
    return new String(newName);
  }
  
  private static void _replaceAllDotsWithUnderscores(String sourceString, char[] destArray, int offset) {
    char c;
    for (int i = 0; i < sourceString.length(); i++) {
      if ((c = sourceString.charAt(i)) == '.') {
        destArray[i + offset] = '_';
      }
      else {
        destArray[i + offset] = c;
      }
    }
  }
  
  public static MetaMethod findGetterMethod(MetaClass cls, String key) {
    MetaMethod metaMethod = _findGetterMethod("get", cls, key);
    if (metaMethod != null) return metaMethod;
    metaMethod = _findGetterMethod("is", cls, key);
     return metaMethod;
  }
  
  private static MetaMethod _findGetterMethod(String prefix, MetaClass cls, String key) {
    key = (prefix + key).toUpperCase();
    for (MetaMethod m : cls.getDeclaredMethods()) {
      if (m.getName().toUpperCase().equals(key) && m.getParameters().length == 0) {
        return m;
      }
    }
    return null;
  }
}
