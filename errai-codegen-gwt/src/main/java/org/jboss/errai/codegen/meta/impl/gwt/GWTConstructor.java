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

import com.google.gwt.core.ext.typeinfo.JConstructor;
import com.google.gwt.core.ext.typeinfo.JParameter;
import com.google.gwt.core.ext.typeinfo.TypeOracle;

import org.jboss.errai.codegen.meta.*;
import org.jboss.errai.codegen.util.GenUtil;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class GWTConstructor extends MetaConstructor {
  private final JConstructor constructor;
  private final MetaClass declaringClass;
  private final Annotation[] annotations;
  private final TypeOracle oracle;

  public GWTConstructor(final TypeOracle oracle, final JConstructor c) {
    this.constructor = c;
    this.annotations = AnnotationParser.parseAnnotations(c.getAnnotations());

    this.declaringClass = GWTClass.newInstance(oracle, c.getEnclosingType());
    this.oracle = oracle;
  }

  @Override
  public MetaParameter[] getParameters() {
    final List<MetaParameter> parameterList = new ArrayList<MetaParameter>();

    for (final JParameter jParameter : constructor.getParameters()) {
      parameterList.add(new GWTParameter(oracle, jParameter, this));
    }

    return parameterList.toArray(new MetaParameter[parameterList.size()]);
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
  public MetaClass getDeclaringClass() {
    return declaringClass;
  }
  
  @Override
  public String getDeclaringClassName() {
    return declaringClass.getName();
  }

  @Override
  public Annotation[] getAnnotations() {
    return annotations;
  }

  @Override
  public MetaType[] getGenericParameterTypes() {
    return null;
  }

  @Override
  public MetaClass[] getCheckedExceptions() {
    return new MetaClass[0];
  }

  @Override
  public boolean isVarArgs() {
    return constructor.isVarArgs();
  }

  @Override
  public boolean isAbstract() {
    return false;
  }

  @Override
  public boolean isPublic() {
    return constructor.isPublic();
  }

  @Override
  public boolean isPrivate() {
    return constructor.isPrivate();
  }

  @Override
  public boolean isProtected() {
    return constructor.isProtected();
  }

  @Override
  public boolean isFinal() {
    return false;
  }

  @Override
  public boolean isStatic() {
    return true;
  }

  @Override
  public boolean isTransient() {
    return false;
  }

  @Override
  public boolean isSynthetic() {
    return false;
  }

  @Override
  public boolean isVolatile() {
    return false;
  }

  @Override
  public boolean isSynchronized() {
    return false;
  }

  @Override
  public MetaTypeVariable[] getTypeParameters() {
    return GWTUtil.fromTypeVariable(oracle, constructor.getTypeParameters());
  }

  @Override
  public boolean equals(final Object o) {
    return o instanceof MetaConstructor && GenUtil.equals(this, (MetaConstructor) o);
  }
}
