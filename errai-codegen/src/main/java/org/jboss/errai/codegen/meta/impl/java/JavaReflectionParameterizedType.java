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

package org.jboss.errai.codegen.meta.impl.java;

import org.jboss.errai.codegen.meta.MetaClassFactory;
import org.jboss.errai.codegen.meta.MetaType;
import org.jboss.errai.codegen.meta.impl.AbstractMetaParameterizedType;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class JavaReflectionParameterizedType extends AbstractMetaParameterizedType {
  ParameterizedType parameterizedType;

  public JavaReflectionParameterizedType(final ParameterizedType parameterizedType) {
    this.parameterizedType = parameterizedType;
  }

  @Override
  public MetaType[] getTypeParameters() {
    return JavaReflectionUtil.fromTypeArray(parameterizedType.getActualTypeArguments());
  }

  @Override
  public MetaType getOwnerType() {
    return JavaReflectionUtil.fromType(parameterizedType.getOwnerType());
  }

  @Override
  public MetaType getRawType() {
    return JavaReflectionUtil.fromType(parameterizedType.getRawType());
  }

  public String toString() {
    final StringBuilder buf = new StringBuilder("<");
    final Type[] parms = parameterizedType.getActualTypeArguments();
    for (int i = 0; i < parms.length; i++) {
      if (parms[i] instanceof Class) {
        buf.append(MetaClassFactory.get((Class) parms[i]).getFullyQualifiedName());
      }
      else {
        buf.append(parms[i].toString());
      }
      if (i + 1 < parms.length) buf.append(',');
    }
    return buf.append('>').toString();
  }
}
