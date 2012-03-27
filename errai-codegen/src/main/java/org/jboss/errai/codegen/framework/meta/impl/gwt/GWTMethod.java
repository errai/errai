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

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.ext.typeinfo.JTypeParameter;
import com.google.gwt.core.ext.typeinfo.TypeOracle;
import org.jboss.errai.codegen.framework.meta.MetaClass;
import org.jboss.errai.codegen.framework.meta.MetaMethod;
import org.jboss.errai.codegen.framework.meta.MetaParameter;
import org.jboss.errai.codegen.framework.meta.MetaType;
import org.jboss.errai.codegen.framework.meta.MetaTypeVariable;
import org.jboss.errai.codegen.framework.util.GenUtil;

import com.google.gwt.core.ext.typeinfo.JMethod;
import com.google.gwt.core.ext.typeinfo.JParameter;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class GWTMethod extends MetaMethod {
  private JMethod method;
  private Annotation[] annotations;
  private TypeOracle oracle;

  GWTMethod(TypeOracle oracle, JMethod method) {
    this.method = method;
    annotations = method.getAnnotations();
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
    List<MetaParameter> parameterList = new ArrayList<MetaParameter>();

    for (JParameter jParameter : method.getParameters()) {
      parameterList.add(new GWTParameter(oracle, jParameter, this));
    }

    return parameterList.toArray(new MetaParameter[parameterList.size()]);
  }

  @Override
  public Annotation[] getAnnotations() {
    return annotations == null ? new Annotation[0] : annotations;
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
    return GWTClass.newInstance(oracle, method.getEnclosingType());
  }

  @Override
  public MetaType getGenericReturnType() {
    JTypeParameter type = method.getReturnType().isTypeParameter();
    if (type != null) {
      return new GWTTypeVariable(oracle, type);
    }
    return null;
  }

  @Override
  public MetaType[] getGenericParameterTypes() {
    List<MetaType> typeList = new ArrayList<MetaType>();
    for (JParameter parm : method.getParameters()) {
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
  public boolean equals(Object o) {
    return o instanceof MetaMethod && GenUtil.equals(this, (MetaMethod) o);
  }

  @Override
  public String toString() {
    return "GWTMethod{" +
            "method=" + method +
            '}';
  }
}
