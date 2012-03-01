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

package org.jboss.errai.codegen.framework;

import java.lang.annotation.Annotation;

import org.jboss.errai.codegen.framework.builder.callstack.LoadClassReference;
import org.jboss.errai.codegen.framework.meta.MetaClass;
import org.jboss.errai.codegen.framework.meta.MetaClassFactory;
import org.jboss.errai.codegen.framework.meta.MetaClassMember;
import org.jboss.errai.codegen.framework.meta.MetaParameter;

/**
 * @author Mike Brock <cbrock@redhat.com>
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class Parameter extends AbstractStatement implements MetaParameter {
  private MetaClass type;
  private String name;
  private boolean isFinal;

  public Parameter(MetaClass type, String name, boolean isFinal) {
    this.type = type;
    this.name = name;
    this.isFinal = isFinal;
  }

  public static Parameter of(MetaClass type, String name) {
    return new Parameter(type, name, false);
  }

  public static Parameter of(Class<?> type, String name) {
    return new Parameter(MetaClassFactory.get(type), name, false);
  }

  public static Parameter of(MetaClass type, String name, boolean isFinal) {
    return new Parameter(type, name, isFinal);
  }

  public static Parameter of(Class<?> type, String name, boolean isFinal) {
    return new Parameter(MetaClassFactory.get(type), name, isFinal);
  }

  public static Parameter[] of (MetaParameter[] parameters) {
    Parameter[] ps = new Parameter[parameters.length];
    for (int i = 0; i < ps.length; i++) {
      String name = parameters[i].getName();
      if (name == null) {
        name = "a" + i;
      }
      
      ps[i] = Parameter.of(parameters[i].getType(), name);
    }
    return ps;
  }
  
  String generatedCache;

  @Override
  public String generate(Context context) {
    if (generatedCache != null)
      return generatedCache;

    generatedCache = (isFinal) ? Modifier.Final.getCanonicalString() + " " : "";
    return generatedCache += LoadClassReference.getClassReference(type, context) + " " + name;
  }

  @Override
  public MetaClass getType() {
    return type;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public MetaClassMember getDeclaringMember() {
    return null;
  }

  @Override
  public Annotation[] getAnnotations() {
    return new Annotation[0];
  }

  @Override
  public boolean isAnnotationPresent(Class<? extends Annotation> annotation) {
    return false;
  }

  @Override
  public <A extends Annotation> A getAnnotation(Class<A> annotation) {
    return null;
  }

  @Override
  public String toString() {
    return type.getFullyQualifiedName();
  }
}
