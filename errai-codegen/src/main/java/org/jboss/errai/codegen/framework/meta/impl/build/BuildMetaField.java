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

package org.jboss.errai.codegen.framework.meta.impl.build;

import java.lang.annotation.Annotation;

import org.jboss.errai.codegen.framework.Statement;
import org.jboss.errai.codegen.framework.Variable;
import org.jboss.errai.codegen.framework.builder.Builder;
import org.jboss.errai.codegen.framework.builder.impl.Scope;
import org.jboss.errai.codegen.framework.meta.MetaClass;
import org.jboss.errai.codegen.framework.meta.MetaField;
import org.jboss.errai.codegen.framework.meta.MetaType;
import org.jboss.errai.codegen.framework.util.GenUtil;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class BuildMetaField extends MetaField implements Builder {
  private BuildMetaClass declaringClass;
  private Statement statement;

  private Scope scope;

  private MetaClass type;
  private MetaType genericType;
  private String name;

  private boolean isFinal;
  private boolean isStatic;
  private boolean isTransient;
  private boolean isVolatile;

  public BuildMetaField(BuildMetaClass declaringClass, Statement statement, Scope scope, MetaClass type, String name) {
    this.declaringClass = declaringClass;
    this.statement = statement;
    this.scope = scope;
    this.type = type;
    this.name = name;
  }

  @Override
  public MetaClass getType() {
    return type;
  }

  @Override
  public MetaType getGenericType() {
    return genericType;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public Annotation[] getAnnotations() {
    return new Annotation[0];
  }

  @Override
  public MetaClass getDeclaringClass() {
    return declaringClass;
  }

  @Override
  public boolean isAbstract() {
    return false;
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
    return isFinal;
  }

  @Override
  public boolean isStatic() {
    return isStatic;
  }

  @Override
  public boolean isTransient() {
    return isTransient;
  }

  @Override
  public boolean isSynthetic() {
    return false;
  }

  @Override
  public boolean isVolatile() {
    return isVolatile;
  }

  @Override
  public boolean isSynchronized() {
    return false;
  }

  @Override
  public boolean isAnnotationPresent(Class<? extends Annotation> annotation) {
    return false;
  }

  @Override
  public <A extends Annotation> A getAnnotation(Class<A> annotation) {
    return null;
  }

  public void setDeclaringClass(BuildMetaClass declaringClass) {
    this.declaringClass = declaringClass;
  }

  public void setScope(Scope scope) {
    this.scope = scope;
  }

  public void setType(MetaClass type) {
    this.type = type;
  }

  public void setGenericType(MetaType genericType) {
    this.genericType = genericType;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setFinal(boolean aFinal) {
    isFinal = aFinal;
  }

  public void setStatic(boolean aStatic) {
    isStatic = aStatic;
  }

  public void setTransient(boolean aTransient) {
    isTransient = aTransient;
  }

  public void setVolatile(boolean aVolatile) {
    isVolatile = aVolatile;
  }

  public void setStatement(Statement statement) {
    this.statement = statement;
  }

  @Override
  public String toJavaString() {
    declaringClass.getContext().addVariable(Variable.create(name, type));

    return statement.generate(declaringClass.getContext());
  }

  @Override
  public boolean equals(Object o) {
    return o instanceof MetaField && GenUtil.equals(this, (MetaField) o);
  }

}
