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

import com.google.gwt.core.ext.typeinfo.TypeOracle;
import org.jboss.errai.codegen.framework.DefModifiers;
import org.jboss.errai.codegen.framework.Modifier;
import org.jboss.errai.codegen.framework.builder.impl.Scope;
import org.jboss.errai.codegen.framework.meta.MetaClass;
import org.jboss.errai.codegen.framework.meta.MetaClassFactory;
import org.jboss.errai.codegen.framework.meta.MetaMethod;
import org.jboss.errai.codegen.framework.meta.MetaParameter;
import org.jboss.errai.codegen.framework.meta.MetaType;
import org.jboss.errai.codegen.framework.meta.MetaTypeVariable;
import org.jboss.errai.codegen.framework.util.GenUtil;

import java.lang.annotation.Annotation;

/**
 * @author Mike Brock
 */
public class GWTSpecialMethod extends MetaMethod {
  private DefModifiers modifiers;
  private Scope scope;
  private GWTClass declaringClass;
  private MetaClass returnType;
  private String methodName;
  private MetaParameter[] parameters;

  GWTSpecialMethod(GWTClass declaringClass, DefModifiers modifiers, Scope scope,
                   Class returnType, String methodName, MetaParameter... parameters) {
    this(declaringClass, modifiers, scope, MetaClassFactory.get(returnType), methodName, parameters);
  }

  GWTSpecialMethod(GWTClass declaringClass, DefModifiers modifiers, Scope scope,
                   MetaClass returnType, String methodName, MetaParameter... parameters) {
    this.declaringClass = declaringClass;
    this.modifiers = modifiers;
    this.scope = scope;
    this.returnType = returnType;
    this.methodName = methodName;
    this.parameters = parameters;
  }

  @Override
  public String getName() {
    return methodName;
  }

  @Override
  public MetaClass getReturnType() {
    return returnType;
  }

  @Override
  public MetaParameter[] getParameters() {
    return parameters;
  }

  @Override
  public Annotation[] getAnnotations() {
    return new Annotation[0];
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
  public MetaClass getDeclaringClass() {
    return declaringClass;
  }

  @Override
  public MetaType getGenericReturnType() {
    return null;
  }

  @Override
  public MetaType[] getGenericParameterTypes() {
    return new MetaType[0];
  }

  @Override
  public MetaClass[] getCheckedExceptions() {
    return new MetaClass[0];
  }

  @Override
  public boolean isAbstract() {
    return modifiers.hasModifier(Modifier.Abstract);
  }

  @Override
  public boolean isPublic() {
    return scope == Scope.Public;
  }

  @Override
  public boolean isPrivate() {
    return scope == Scope.Private;
  }

  @Override
  public boolean isProtected() {
    return scope == Scope.Protected;
  }

  @Override
  public boolean isFinal() {
    return modifiers.hasModifier(Modifier.Final);
  }

  @Override
  public boolean isStatic() {
    return modifiers.hasModifier(Modifier.Static);

  }

  @Override
  public boolean isTransient() {
    return false;
  }

  @Override
  public boolean isVolatile() {
    return false;
  }

  @Override
  public boolean isSynthetic() {
    return false;
  }

  @Override
  public boolean isSynchronized() {
    return false;
  }

  @Override
  public boolean isVarArgs() {
    return false;
  }

  @Override
  public MetaTypeVariable[] getTypeParameters() {
    return new MetaTypeVariable[0];
  }
}
