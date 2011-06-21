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

import org.jboss.errai.ioc.rebind.ioc.codegen.meta.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public class JavaReflectionMethod extends MetaMethod {
  private Method method;
  private MetaParameter[] parameters;
  private MetaClass declaringClass;
  private MetaClass returnType;

  JavaReflectionMethod(Method method) {
    this.method = method;

    List<MetaParameter> parmList = new ArrayList<MetaParameter>();

    for (int i = 0; i < method.getParameterTypes().length; i++) {
      parmList.add(new JavaReflectionParameter(method.getParameterTypes()[i],
          method.getParameterAnnotations()[i], this));
    }

    parameters = parmList.toArray(new MetaParameter[parmList.size()]);

    declaringClass = MetaClassFactory.get(method.getDeclaringClass());
    returnType = MetaClassFactory.get(method.getReturnType());
  }

  public String getName() {
    return method.getName();
  }

  public MetaParameter[] getParameters() {
    return parameters;
  }

  public MetaClass getReturnType() {
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

  public MetaTypeVariable[] getTypeParameters() {
    return JavaReflectionUtil.fromTypeVariable(method.getTypeParameters());
  }

  public Annotation[] getAnnotations() {
    return method.getAnnotations();
  }

  public final <A extends Annotation> A getAnnotation(Class<A> annotation) {
    for (Annotation a : getAnnotations()) {
      if (a.annotationType().equals(annotation)) return (A) a;
    }
    return null;
  }

  public boolean isAnnotationPresent(Class<? extends Annotation> annotation) {
    return getAnnotation(annotation) != null;
  }

  public MetaClass getDeclaringClass() {
    return declaringClass;
  }

  public boolean isAbstract() {
    return (method.getModifiers() & Modifier.ABSTRACT) != 0;
  }

  public boolean isPublic() {
    return (method.getModifiers() & Modifier.PUBLIC) != 0;
  }

  public boolean isPrivate() {
    return (method.getModifiers() & Modifier.PRIVATE) != 0;
  }

  public boolean isProtected() {
    return (method.getModifiers() & Modifier.PROTECTED) != 0;
  }

  public boolean isFinal() {
    return (method.getModifiers() & Modifier.FINAL) != 0;
  }

  public boolean isStatic() {
    return (method.getModifiers() & Modifier.STATIC) != 0;
  }

  public boolean isTransient() {
    return (method.getModifiers() & Modifier.TRANSIENT) != 0;
  }

  public boolean isSynthetic() {
    return method.isSynthetic();
  }

  public boolean isSynchronized() {
    return (method.getModifiers() & Modifier.SYNCHRONIZED) != 0;
  }
}
