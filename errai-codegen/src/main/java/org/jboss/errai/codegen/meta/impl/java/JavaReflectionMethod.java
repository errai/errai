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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.MetaClassFactory;
import org.jboss.errai.codegen.meta.MetaMethod;
import org.jboss.errai.codegen.meta.MetaParameter;
import org.jboss.errai.codegen.meta.MetaType;
import org.jboss.errai.codegen.meta.MetaTypeVariable;
import org.jboss.errai.common.client.framework.Assert;

import com.google.common.reflect.TypeToken;

public class JavaReflectionMethod extends MetaMethod {
  private final Method method;
  private final MetaClass referenceClass;
  private MetaParameter[] parameters;
  private MetaClass declaringClass;
  private MetaClass returnType;

  JavaReflectionMethod(MetaClass referenceClass, Method method) {
    this.referenceClass = Assert.notNull(referenceClass);
    this.method = Assert.notNull(method);
  }

  @Override
  public String getName() {
    return method.getName();
  }

  @Override
  public MetaParameter[] getParameters() {
    if (parameters == null) {
      List<MetaParameter> parmList = new ArrayList<MetaParameter>();

      Class<?>[] parmTypes = method.getParameterTypes();
      Type[] genParmTypes = method.getGenericParameterTypes();
      Annotation[][] parmAnnos = method.getParameterAnnotations();

      for (int i = 0; i < parmTypes.length; i++) {
        TypeToken<?> token = TypeToken.of(referenceClass.asClass());
        Class<?> parmType = token.resolveType(genParmTypes[i]).getRawType();

        MetaClass mcParm = MetaClassFactory.get(parmType, genParmTypes[i]);
        parmList.add(new JavaReflectionParameter(mcParm, parmAnnos[i], this));
      }
      parameters = parmList.toArray(new MetaParameter[parmList.size()]);
    }
    return parameters;
  }

  @Override
  public MetaClass getReturnType() {
    if (returnType == null) {
      returnType = MetaClassFactory.get(method.getReturnType());
    }
    return returnType;
  }

  @Override
  public MetaType getGenericReturnType() {
    return JavaReflectionUtil.fromType(method.getGenericReturnType());
  }

  @Override
  public MetaType[] getGenericParameterTypes() {
    return JavaReflectionUtil.fromTypeArray(method.getGenericParameterTypes());
  }

  @Override
  public MetaTypeVariable[] getTypeParameters() {
    return JavaReflectionUtil.fromTypeVariable(method.getTypeParameters());
  }

  private Annotation[] _annotationsCache;

  @Override
  public Annotation[] getAnnotations() {
    if (_annotationsCache != null)
      return _annotationsCache;
    return _annotationsCache = method.getAnnotations();
  }

  @Override
  public MetaClass[] getCheckedExceptions() {
    return MetaClassFactory.fromClassArray(method.getExceptionTypes());
  }

  @Override
  public MetaClass getDeclaringClass() {
    if (declaringClass == null) {
      declaringClass = MetaClassFactory.get(method.getDeclaringClass());
    }
    return declaringClass;
  }

  @Override
  public boolean isAbstract() {
    return (method.getModifiers() & Modifier.ABSTRACT) != 0;
  }

  @Override
  public boolean isPublic() {
    return (method.getModifiers() & Modifier.PUBLIC) != 0;
  }

  @Override
  public boolean isPrivate() {
    return (method.getModifiers() & Modifier.PRIVATE) != 0;
  }

  @Override
  public boolean isProtected() {
    return (method.getModifiers() & Modifier.PROTECTED) != 0;
  }

  @Override
  public boolean isFinal() {
    return (method.getModifiers() & Modifier.FINAL) != 0;
  }

  @Override
  public boolean isStatic() {
    return (method.getModifiers() & Modifier.STATIC) != 0;
  }

  @Override
  public boolean isTransient() {
    return (method.getModifiers() & Modifier.TRANSIENT) != 0;
  }

  @Override
  public boolean isVolatile() {
    return false;
  }

  @Override
  public boolean isSynthetic() {
    return method.isSynthetic();
  }

  @Override
  public boolean isSynchronized() {
    return (method.getModifiers() & Modifier.SYNCHRONIZED) != 0;
  }

  @Override
  public boolean isVarArgs() {
    return method.isVarArgs();
  }

  @Override
  public Method asMethod() {
    return method;
  }

  @Override
  public String toString() {
    return getName() + "(" + Arrays.toString(parameters) + ")";
  }
}
