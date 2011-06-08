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

package org.jboss.errai.ioc.rebind.ioc.codegen.meta.impl.gwt;

import com.google.gwt.core.ext.typeinfo.JType;
import com.google.gwt.core.ext.typeinfo.JTypeParameter;
import org.jboss.errai.ioc.rebind.ioc.codegen.MetaClassFactory;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaType;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaTypeVariable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class GWTUtil {
  public static MetaTypeVariable[] fromTypeVariable(JTypeParameter[] typeParameters) {
    List<MetaTypeVariable> typeVariableList = new ArrayList<MetaTypeVariable>();

    for (JTypeParameter typeVariable : typeParameters) {
      typeVariableList.add(new GWTTypeVariable(typeVariable));
    }

    return typeVariableList.toArray(new MetaTypeVariable[typeVariableList.size()]);
  }


  public static MetaType[] fromTypeArray(JType[] types) {
    List<MetaType> typeList = new ArrayList<MetaType>();

    for (JType t : types) {
      typeList.add(fromType(t));
    }

    return typeList.toArray(new MetaType[types.length]);
  }

  public static MetaType fromType(JType t) {
    if (t.isClassOrInterface() != null) {
      return MetaClassFactory.get(t.isClassOrInterface());
    }
    else if (t.isTypeParameter() != null) {
      return new GWTTypeVariable(t.isTypeParameter());
    }
    else if (t.isGenericType() != null) {
      if (t.isArray() != null) {
        return new GWTGenericArrayType(t.isGenericType());
      }
      else {
        return new GWTGenericDeclaration(t.isGenericType());
      }
    }
    else if (t.isParameterized() != null) {
      return new GWTParameterizedType(t.isParameterized());
    }
    else if (t.isWildcard() != null) {
      return new GWTWildcardType(t.isWildcard());
    }
    else {
      return null;
    }
  }
}
