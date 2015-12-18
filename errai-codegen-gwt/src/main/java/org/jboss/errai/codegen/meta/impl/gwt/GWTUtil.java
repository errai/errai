/*
 * Copyright (C) 2011 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.codegen.meta.impl.gwt;

import java.util.ArrayList;
import java.util.List;

import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.MetaClassFactory;
import org.jboss.errai.codegen.meta.MetaType;
import org.jboss.errai.codegen.meta.MetaTypeVariable;

import com.google.gwt.core.ext.typeinfo.JArrayType;
import com.google.gwt.core.ext.typeinfo.JType;
import com.google.gwt.core.ext.typeinfo.JTypeParameter;
import com.google.gwt.core.ext.typeinfo.TypeOracle;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class GWTUtil {

  public static MetaTypeVariable[] fromTypeVariable(final TypeOracle oracle,
                                                    final JTypeParameter[] typeParameters) {
    final List<MetaTypeVariable> typeVariableList = new ArrayList<MetaTypeVariable>();

    for (final JTypeParameter typeVariable : typeParameters) {
      typeVariableList.add(new GWTTypeVariable(oracle, typeVariable));
    }

    return typeVariableList.toArray(new MetaTypeVariable[typeVariableList.size()]);
  }


  public static MetaType[] fromTypeArray(final TypeOracle oracle, final JType[] types) {
    final List<MetaType> typeList = new ArrayList<MetaType>();

    for (final JType t : types) {
      typeList.add(fromType(oracle, t));
    }

    return typeList.toArray(new MetaType[types.length]);
  }

  private static JType getRootComponentType(JArrayType type) {
    JType root = null;
    while (type.getComponentType() != null) {
      if (type.getComponentType().isArray() != null) {
        type = type.getComponentType().isArray();
      }
      else {
        root = type.getComponentType();
        break;
      }

    }
    return root;
  }

  public static MetaClass eraseOrReturn(final TypeOracle oracle, final JType t) {

    if (t.isArray() != null) {
      final JType root = getRootComponentType(t.isArray());
      if (root.isTypeParameter() != null) {
        return MetaClassFactory.get(Object.class);
      }
    }
    if (t.isTypeParameter() != null) {
      JTypeParameter tp = t.isTypeParameter();
      return MetaClassFactory.get(tp.getErasedType().getQualifiedBinaryName());
    }
    return GWTClass.newInstance(oracle, t);
  }

  public static MetaType fromType(final TypeOracle oracle, final JType t) {
    if (t.isTypeParameter() != null) {
      return new GWTTypeVariable(oracle, t.isTypeParameter());
    }
    else if (t.isArray() != null
        && (t.isArray().getComponentType().isTypeParameter() != null
        || t.isArray().getComponentType().isWildcard() != null)) {
      return new GWTGenericArrayType(oracle, t.isArray());
    }
    else if (t.isParameterized() != null) {
      return new GWTParameterizedType(oracle, t.isParameterized());
    }
    else if (t.isWildcard() != null) {
      return new GWTWildcardType(oracle, t.isWildcard());
    }
    else if (t.isClassOrInterface() != null
        || t.isEnum() != null
        || t.isPrimitive() != null
        || t.isRawType() != null
        || t.isArray() != null) {
      return GWTClass.newInstance(oracle, t);
    }
    else {
      throw new RuntimeException("Don't know how to make a MetaType from given JType " + t +
          " (which is a " + (t == null ? null : t.getClass()) + ")");
    }
  }

}
