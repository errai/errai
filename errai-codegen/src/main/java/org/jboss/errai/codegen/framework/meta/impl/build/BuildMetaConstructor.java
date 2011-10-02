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
import java.util.ArrayList;
import java.util.List;

import org.jboss.errai.codegen.framework.Context;
import org.jboss.errai.codegen.framework.DefParameters;
import org.jboss.errai.codegen.framework.Parameter;
import org.jboss.errai.codegen.framework.Statement;
import org.jboss.errai.codegen.framework.Variable;
import org.jboss.errai.codegen.framework.builder.Builder;
import org.jboss.errai.codegen.framework.builder.impl.Scope;
import org.jboss.errai.codegen.framework.meta.*;
import org.jboss.errai.codegen.framework.util.GenUtil;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class BuildMetaConstructor extends MetaConstructor implements Builder {
  private Context context;
  private BuildMetaClass declaringClass;
  private Statement body;

  private boolean isVarArgs;
  private boolean isAbstract;
  private Scope scope;

  private DefParameters defParameters;
  private List<MetaTypeVariable> typeVariables = new ArrayList<MetaTypeVariable>();

  private MetaConstructor reifiedFormOf;

  public BuildMetaConstructor(BuildMetaClass declaringClass) {
    this.context = Context.create(declaringClass.getContext());
    this.declaringClass = declaringClass;
  }

  public BuildMetaConstructor(BuildMetaClass declaringClass, Statement body) {
    this.context = Context.create(declaringClass.getContext());
    this.declaringClass = declaringClass;
    this.body = body;
  }

  public BuildMetaConstructor(BuildMetaClass declaringClass, Statement body, DefParameters defParameters) {
    this.context = Context.create(declaringClass.getContext());
    this.declaringClass = declaringClass;
    this.body = body;
    this.defParameters = defParameters;
  }
  
  public BuildMetaConstructor(BuildMetaClass declaringClass, Statement body, Scope scope, DefParameters defParameters) {
    this.context = Context.create(declaringClass.getContext());
    this.declaringClass = declaringClass;
    this.body = body;
    this.defParameters = defParameters;
    this.scope = scope;
  }

  @Override
  public MetaParameter[] getParameters() {
    return defParameters == null ?
            new MetaParameter[0] : defParameters.getParameters()
            .toArray(new MetaParameter[defParameters.getParameters().size()]);
  }

  @Override
  public MetaType[] getGenericParameterTypes() {
    return typeVariables.toArray(new MetaTypeVariable[typeVariables.size()]);
  }

  @Override
  public boolean isVarArgs() {
    return isVarArgs;
  }

  @Override
  public MetaClass getDeclaringClass() {
    return declaringClass;
  }

  @Override
  public boolean isAbstract() {
    return isAbstract;
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
    return false;
  }

  @Override
  public boolean isStatic() {
    return false;
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

  public void setScope(Scope scope) {
    this.scope = scope;
  }

  @Override
  public Annotation[] getAnnotations() {
    return new Annotation[0];
  }

  @Override
  public boolean isAnnotationPresent(Class<? extends Annotation> annotation) {
//    for (Annotation a : annotations) {
//      if (a.getClass().equals(annotation)) return true;
//    }
    return false;
  }

  @Override
  public <A extends Annotation> A getAnnotation(Class<A> annotation) {
    return null;
  }

  @Override
  public MetaTypeVariable[] getTypeParameters() {
    return typeVariables.toArray(new MetaTypeVariable[typeVariables.size()]);
  }

  public Statement getBody() {
    return body;
  }

  public void setBody(Statement body) {
    this.body = body;
  }

  public void setDefParameters(DefParameters defParameters) {
    this.defParameters = defParameters;
  }
//
//  public void addCheckedException(MetaClass clazz) {
//    if (!clazz.isAssignableFrom(Throwable.class)) {
//      throw new RuntimeException("not an exception type: " + clazz.getFullyQualifiedName());
//    }
//    checkedExceptions.add(clazz);
//  }


  @Override
  public MetaClass[] getCheckedExceptions() {
    return new MetaClass[0];
  }

  @Override
  public String getName() {
    return "";
  }

  @Override
  public MetaClass getReturnType() {
    return this.declaringClass;
  }

  @Override
  public MetaType getGenericReturnType() {
    return this.declaringClass;
  }

  public boolean isReifiedForm() {
    return reifiedFormOf != null;
  }

  public MetaConstructor getReifiedFormOf() {
    return reifiedFormOf;
  }

  public void setReifiedFormOf(MetaConstructor reifiedFormOf) {
    this.reifiedFormOf = reifiedFormOf;
  }

  @Override
  public String toJavaString() {
    for (Parameter p : defParameters.getParameters()) {
      context.addVariable(Variable.create(p.getName(), p.getType()));
    }

    return new StringBuilder().append(scope.getCanonicalName())
            .append(" ")
            .append(declaringClass.getName())
            .append(defParameters.generate(context))
            .append(" {\n").append(body.generate(context)).append("\n}\n")
            .toString();
  }

  @Override
  public boolean equals(Object o) {
    return o instanceof MetaConstructor && GenUtil.equals(this, (MetaConstructor) o);
  }
}
