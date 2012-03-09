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

package org.jboss.errai.codegen.framework.meta.impl.gwt;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.ext.typeinfo.JArrayType;
import com.google.gwt.core.ext.typeinfo.TypeOracle;
import org.jboss.errai.codegen.framework.meta.MetaClass;
import org.jboss.errai.codegen.framework.meta.MetaClassFactory;
import org.jboss.errai.codegen.framework.meta.MetaType;
import org.jboss.errai.codegen.framework.meta.MetaTypeVariable;

import com.google.gwt.core.ext.typeinfo.JType;
import com.google.gwt.core.ext.typeinfo.JTypeParameter;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class GWTUtil {
  public static MetaTypeVariable[] fromTypeVariable(TypeOracle oracle, JTypeParameter[] typeParameters) {
    List<MetaTypeVariable> typeVariableList = new ArrayList<MetaTypeVariable>();

    for (JTypeParameter typeVariable : typeParameters) {
      typeVariableList.add(new GWTTypeVariable(oracle, typeVariable));
    }

    return typeVariableList.toArray(new MetaTypeVariable[typeVariableList.size()]);
  }


  public static MetaType[] fromTypeArray(TypeOracle oracle, JType[] types) {
    List<MetaType> typeList = new ArrayList<MetaType>();

    for (JType t : types) {
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

  public static MetaClass eraseOrReturn(TypeOracle oracle, JType t) {

    if (t.isArray() != null) {
      JType root = getRootComponentType(t.isArray());
      if (root.isTypeParameter() != null) {
        return MetaClassFactory.get(Object.class);
      }
    }
    if (t.isTypeParameter() != null) {
      return MetaClassFactory.get(Object.class);
    }
    else {
      return GWTClass.newInstance(oracle, t);
    }
  }

  public static MetaType fromType(TypeOracle oracle, JType t) {
    if (t.isTypeParameter() != null) {
      return new GWTTypeVariable(oracle, t.isTypeParameter());
    }
    else if (t.isGenericType() != null) {
      if (t.isArray() != null) {
        return new GWTGenericArrayType(oracle, t.isGenericType());
      }
      else {
        return new GWTGenericDeclaration(oracle, t.isGenericType());
      }
    }
    else if (t.isParameterized() != null) {
      return new GWTParameterizedType(oracle, t.isParameterized());
    }
    else if (t.isWildcard() != null) {
      return new GWTWildcardType(oracle, t.isWildcard());
    }
    else if (t.isClassOrInterface() != null) {
      return GWTClass.newInstance(oracle, t.isClassOrInterface());
    }
    else {
      return null;
    }
  }
}
