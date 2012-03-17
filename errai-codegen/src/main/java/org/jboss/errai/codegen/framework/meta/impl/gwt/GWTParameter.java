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
import java.lang.reflect.Constructor;

import com.google.gwt.core.ext.typeinfo.TypeOracle;
import org.jboss.errai.codegen.framework.meta.MetaClass;
import org.jboss.errai.codegen.framework.meta.MetaClassMember;
import org.jboss.errai.codegen.framework.meta.MetaConstructor;
import org.jboss.errai.codegen.framework.meta.MetaMethod;
import org.jboss.errai.codegen.framework.meta.MetaParameter;

import com.google.gwt.core.ext.typeinfo.JAbstractMethod;
import com.google.gwt.core.ext.typeinfo.JParameter;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class GWTParameter extends MetaParameter {
  private JParameter parameter;
  private Annotation[] annotations;
  private MetaClassMember declaredBy;
  private TypeOracle oracle;

  GWTParameter(TypeOracle oracle, JParameter parameter, MetaMethod declaredBy) {
    this.parameter = parameter;
    this.declaredBy = declaredBy;
    annotations = parameter.getAnnotations();
    this.oracle = oracle;
  }

  GWTParameter(TypeOracle oracle,JParameter parameter, MetaConstructor declaredBy) {
    this.parameter = parameter;
    this.declaredBy = declaredBy;
    this.oracle = oracle;
    this.annotations = parameter.getAnnotations();
  }

  @Override
  public String getName() {
    return parameter.getName();
  }

  @Override
  public MetaClass getType() {
    return GWTUtil.eraseOrReturn(oracle, parameter.getType());
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
  public final boolean isAnnotationPresent(Class<? extends Annotation> annotation) {
    return getAnnotation(annotation) != null;
  }

  @Override
  public MetaClassMember getDeclaringMember() {
    return declaredBy;
  }
}
