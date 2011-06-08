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

package org.jboss.errai.ioc.rebind.ioc.codegen.meta.impl.java;

import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaParameterizedType;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaType;

import java.lang.reflect.ParameterizedType;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class JavaReflectionParameterizedType implements MetaParameterizedType {
  ParameterizedType parameterizedType;

  public JavaReflectionParameterizedType(ParameterizedType parameterizedType) {
    this.parameterizedType = parameterizedType;
  }

  public MetaType[] getTypeParameters() {
    return JavaReflectionUtil.fromTypeArray(parameterizedType.getActualTypeArguments());
  }

  public MetaType getOwnerType() {
    return JavaReflectionUtil.fromType(parameterizedType.getOwnerType());
  }

  public MetaType getRawType() {
    return JavaReflectionUtil.fromType(parameterizedType.getRawType());
  }
}
