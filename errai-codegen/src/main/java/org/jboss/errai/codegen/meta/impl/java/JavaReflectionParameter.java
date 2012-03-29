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

import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.MetaClassMember;
import org.jboss.errai.codegen.meta.MetaParameter;
import org.mvel2.util.ReflectionUtil;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class JavaReflectionParameter extends MetaParameter {
  private String name;
  private MetaClass type;
  private Annotation[] annotations;
  private MetaClassMember declaredBy;

  public JavaReflectionParameter(MetaClass type, Annotation[] annotations, MetaClassMember declaredBy) {
    this.name = ReflectionUtil.getPropertyFromAccessor(type.getName());
    this.type = type;
    this.annotations = annotations;
    this.declaredBy = declaredBy;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public MetaClass getType() {
    return type;
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

  @Override
  public String toString() {
    return type.getFullyQualifiedName();
  }
}
