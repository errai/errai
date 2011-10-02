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

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import org.jboss.errai.codegen.framework.meta.*;
import org.jboss.errai.codegen.framework.util.GenUtil;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class JavaReflectionConstructor extends MetaConstructor {
  private Constructor constructor;
  private MetaParameter[] parameters;
  private MetaClass declaringClass;
  private Annotation[] annotationsCache;

  JavaReflectionConstructor(Constructor c) {
    constructor = c;

    List<MetaParameter> parmList = new ArrayList<MetaParameter>();

    for (int i = 0; i < c.getParameterTypes().length; i++) {
      MetaClass mcParm = MetaClassFactory.get(c.getParameterTypes()[i], c.getGenericParameterTypes()[i]);
      parmList.add(new JavaReflectionParameter(mcParm, c.getParameterAnnotations()[i], this));
    }

    parameters = parmList.toArray(new MetaParameter[parmList.size()]);
    declaringClass = MetaClassFactory.get(c.getDeclaringClass());
  }

  @Override
  public MetaParameter[] getParameters() {
    return parameters;
  }

  @Override
  public MetaClass getDeclaringClass() {
    return declaringClass;
  }

  @Override
  public MetaType[] getGenericParameterTypes() {
    return JavaReflectionUtil.fromTypeArray(constructor.getGenericParameterTypes());
  }

  @Override
  public MetaTypeVariable[] getTypeParameters() {
    return JavaReflectionUtil.fromTypeVariable(constructor.getTypeParameters());
  }

  @Override
  public String getName() {
    return "";
  }

  @Override
  public MetaClass getReturnType() {
    return declaringClass;
  }

  @Override
  public MetaType getGenericReturnType() {
    return declaringClass;
  }

  @Override
  public Annotation[] getAnnotations() {
    if (annotationsCache == null) {
      annotationsCache = constructor.getAnnotations();
    }
    return annotationsCache;
  }

  @Override
  public final <A extends Annotation> A getAnnotation(Class<A> annotation) {
    for (Annotation a : getAnnotations()) {
      if (a.annotationType().equals(annotation)) return (A) a;
    }
    return null;
  }

  @Override
  public final boolean isAnnotationPresent(Class<? extends Annotation> annotation) {
    return getAnnotation(annotation) != null;
  }

  @Override
  public MetaClass[] getCheckedExceptions() {
    return new MetaClass[0];
  }

  @Override
  public boolean isAbstract() {
    return (constructor.getModifiers() & Modifier.ABSTRACT) != 0;
  }

  @Override
  public boolean isPublic() {
    return (constructor.getModifiers() & Modifier.PUBLIC) != 0;
  }

  @Override
  public boolean isPrivate() {
    return (constructor.getModifiers() & Modifier.PRIVATE) != 0;
  }

  @Override
  public boolean isProtected() {
    return (constructor.getModifiers() & Modifier.PROTECTED) != 0;
  }

  @Override
  public boolean isFinal() {
    return (constructor.getModifiers() & Modifier.FINAL) != 0;
  }

  @Override
  public boolean isStatic() {
    return (constructor.getModifiers() & Modifier.STATIC) != 0;
  }

  @Override
  public boolean isTransient() {
    return (constructor.getModifiers() & Modifier.TRANSIENT) != 0;
  }

  @Override
  public boolean isVolatile() {
    return false;
  }

  @Override
  public boolean isSynchronized() {
    return (constructor.getModifiers() & Modifier.SYNCHRONIZED) != 0;
  }

  @Override
  public boolean isSynthetic() {
    return constructor.isSynthetic();
  }

  @Override
  public boolean isVarArgs() {
    return constructor.isVarArgs();
  }

  @Override
  public boolean equals(Object o) {
    return o instanceof MetaConstructor && GenUtil.equals(this, (MetaConstructor) o);
  }
}
