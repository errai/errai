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

package org.jboss.errai.codegen.framework.meta.impl.java;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.jboss.errai.codegen.framework.meta.MetaClass;
import org.jboss.errai.codegen.framework.meta.MetaClassFactory;
import org.jboss.errai.codegen.framework.meta.MetaMethod;
import org.jboss.errai.codegen.framework.meta.MetaParameter;
import org.jboss.errai.codegen.framework.meta.MetaType;
import org.jboss.errai.codegen.framework.meta.MetaTypeVariable;
import org.jboss.errai.codegen.framework.util.GenUtil;

public class JavaReflectionMethod extends MetaMethod {
  private Method method;
  private MetaParameter[] parameters;
  private MetaClass declaringClass;
  private MetaClass returnType;

  JavaReflectionMethod(Method method) {
    this.method = method;
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
        MetaClass mcParm = MetaClassFactory.get(parmTypes[i], genParmTypes[i]);

        parmList.add(new JavaReflectionParameter(mcParm,
                parmAnnos[i], this));
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
    if (_annotationsCache != null) return _annotationsCache;
    return _annotationsCache = method.getAnnotations();
  }

  @Override
  public final <A extends Annotation> A getAnnotation(Class<A> annotation) {
    for (Annotation a : getAnnotations()) {
      if (a.annotationType().equals(annotation)) return (A) a;
    }
    return null;
  }

  @Override
  public boolean isAnnotationPresent(Class<? extends Annotation> annotation) {
    return getAnnotation(annotation) != null;
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
  public boolean equals(Object o) {
    return o instanceof MetaMethod && GenUtil.equals(this, (MetaMethod) o);
  }
}
