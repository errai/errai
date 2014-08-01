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

package org.jboss.errai.codegen.meta.impl.gwt;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

import org.jboss.errai.codegen.meta.*;
import org.jboss.errai.codegen.util.GenUtil;

import com.google.gwt.core.ext.typeinfo.JMethod;
import com.google.gwt.core.ext.typeinfo.JParameter;
import com.google.gwt.core.ext.typeinfo.JType;
import com.google.gwt.core.ext.typeinfo.TypeOracle;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class GWTMethod extends MetaMethod {

  private final JMethod method;
  private volatile Annotation[] annotations;
  private final TypeOracle oracle;

  GWTMethod(final TypeOracle oracle, final JMethod method) {
    this.method = method;
    this.annotations = AnnotationParser.parseAnnotations(method.getAnnotations());
    this.oracle = oracle;
  }

  @Override
  public String getName() {
    return method.getName();
  }

  @Override
  public MetaClass getReturnType() {
    return GWTUtil.eraseOrReturn(oracle, method.getReturnType());
  }

  @Override
  public MetaParameter[] getParameters() {
    final List<MetaParameter> parameterList = new ArrayList<MetaParameter>();

    for (final JParameter jParameter : method.getParameters()) {
      parameterList.add(new GWTParameter(oracle, jParameter, this));
    }

    return parameterList.toArray(new MetaParameter[parameterList.size()]);
  }

  @Override
  public synchronized Annotation[] getAnnotations() {
    return annotations;
  }

  @Override
  public MetaClass getDeclaringClass() {
    return GWTClass.newInstance(oracle, method.getEnclosingType());
  }
  
  @Override
  public String getDeclaringClassName() {
    return method.getEnclosingType().getName();
  }

  @Override
  public MetaType getGenericReturnType() {
    try {
      JType returnType = method.getReturnType();
      return GWTUtil.fromType(oracle, returnType);
    }
    catch (Exception e) {
      throw new RuntimeException(
              "Failed to produce a generic MetaType for return type of method " +
              method.getReadableDeclaration() + " in class " +
              method.getEnclosingType().getQualifiedSourceName() +
              " (underlying GWT return type is " +
              method.getReturnType().getClass() + ")", e);
    }
  }

  @Override
  public MetaType[] getGenericParameterTypes() {
    final List<MetaType> typeList = new ArrayList<MetaType>();
    for (final JParameter parm : method.getParameters()) {
      typeList.add(GWTUtil.fromType(oracle, parm.getType()));
    }

    return typeList.toArray(new MetaType[typeList.size()]);
  }

  @Override
  public MetaClass[] getCheckedExceptions() {
    return GWTClass.fromClassArray(oracle, method.getThrows());
  }

  @Override
  public boolean isAbstract() {
    return method.isAbstract();
  }

  @Override
  public boolean isPublic() {
    return method.isPublic();
  }

  @Override
  public boolean isPrivate() {
    return method.isPrivate();
  }

  @Override
  public boolean isProtected() {
    return method.isProtected();
  }

  @Override
  public boolean isFinal() {
    return method.isFinal();
  }

  @Override
  public boolean isStatic() {
    return method.isStatic();
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
    return method.isVarArgs();
  }

  @Override
  public MetaTypeVariable[] getTypeParameters() {
    return GWTUtil.fromTypeVariable(oracle, method.getTypeParameters());
  }

  @Override
  public boolean equals(final Object o) {
    return o instanceof MetaMethod && GenUtil.equals(this, (MetaMethod) o);
  }
  
}
