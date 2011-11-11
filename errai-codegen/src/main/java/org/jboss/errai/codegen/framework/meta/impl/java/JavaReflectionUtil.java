/*
 * Copyright 2011 JBoss, a divison Red Hat, Inc
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

package org.jboss.errai.codegen.framework.meta.impl.java;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.errai.codegen.framework.meta.MetaClassFactory;
import org.jboss.errai.codegen.framework.meta.MetaType;
import org.jboss.errai.codegen.framework.meta.MetaTypeVariable;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class JavaReflectionUtil {

  public static MetaTypeVariable[] fromTypeVariable(TypeVariable[] typeVariables) {
    List<MetaTypeVariable> typeVariableList = new ArrayList<MetaTypeVariable>();

    for (TypeVariable typeVariable : typeVariables) {
      typeVariableList.add(new JavaReflectionTypeVariable(typeVariable));
    }

    return typeVariableList.toArray(new MetaTypeVariable[typeVariableList.size()]);
  }

  public static MetaType[] fromTypeArray(Type[] types) {
    List<MetaType> typeList = new ArrayList<MetaType>();

    for (Type t : types) {
      typeList.add(fromType(t));
    }

    return typeList.toArray(new MetaType[types.length]);
  }

  private static final Map<Type, MetaType> FROM_TYPE_CLASS = new HashMap<Type, MetaType>();

  public static MetaType fromType(Type t) {
    MetaType type = FROM_TYPE_CLASS.get(t);
    if (type == null) {
      if (t instanceof Class) {
        type = (MetaClassFactory.get((Class) t));
      }
      else if (t instanceof TypeVariable) {
        type = new JavaReflectionTypeVariable((TypeVariable) t);
      }
      else if (t instanceof ParameterizedType) {
        type = new JavaReflectionParameterizedType((ParameterizedType) t);
      }
      else if (t instanceof GenericArrayType) {
        type = new JavaReflectionGenericArrayType((GenericArrayType) t);
      }
      else if (t instanceof GenericDeclaration) {
        type = new JavaReflectionGenericDeclaration((GenericDeclaration) t);
      }
      else if (t instanceof WildcardType) {
        type = new JavaReflectionWildcardType((WildcardType) t);
      }

      FROM_TYPE_CLASS.put(t, type);
    }

    return type;
  }
}
