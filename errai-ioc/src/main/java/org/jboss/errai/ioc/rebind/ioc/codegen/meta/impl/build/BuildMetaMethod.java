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

package org.jboss.errai.ioc.rebind.ioc.codegen.meta.impl.build;

import org.jboss.errai.ioc.rebind.ioc.codegen.*;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.Builder;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.callstack.LoadClassReference;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.impl.Scope;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.*;

import java.lang.annotation.Annotation;
import java.util.List;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class BuildMetaMethod extends MetaMethod implements Builder {
  private Context context;
  private BuildMetaClass declaringClass;
  private Statement body;

  private Scope scope;

  private String name;
  private MetaClass returnType;
  private DefParameters defParameters;

  private List<MetaParameter> metaParameters;
  private MetaType genericReturnType;
  private List<MetaType> genericParameterTypes;

  private ThrowsDeclaration throwsDeclaration;

  public BuildMetaMethod(BuildMetaClass declaringClass,
                         Statement body,
                         Scope scope,
                         String name,
                         MetaClass returnType,
                         DefParameters defParameters,
                         ThrowsDeclaration throwsDeclaration) {

    this.context = Context.create(declaringClass.getContext());
    this.declaringClass = declaringClass;
    this.body = body;
    this.scope = scope;
    this.name = name;
    this.returnType = returnType;
    this.defParameters = defParameters;
    this.throwsDeclaration = throwsDeclaration;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public MetaClass getReturnType() {
    return returnType;
  }

  @Override
  public MetaType getGenericReturnType() {
    return genericReturnType;
  }

  @Override
  public MetaType[] getGenericParameterTypes() {
    return genericParameterTypes.toArray(new MetaType[genericParameterTypes.size()]);
  }

  @Override
  public MetaParameter[] getParameters() {
    return metaParameters.toArray(new MetaParameter[metaParameters.size()]);
  }

  public MetaClass getDeclaringClass() {
    return declaringClass;
  }

  public boolean isAbstract() {
    return false;
  }

  public boolean isPublic() {
    return scope == Scope.Public;
  }

  public boolean isPrivate() {
    return scope == Scope.Private;
  }

  public boolean isProtected() {
    return scope == Scope.Protected;
  }

  public boolean isFinal() {
    return false;
  }

  public boolean isStatic() {
    return false;
  }

  public boolean isTransient() {
    return false;
  }

  public boolean isSynthetic() {
    return false;
  }

  public boolean isSynchronized() {
    return false;
  }

  public Annotation[] getAnnotations() {
    return new Annotation[0];
  }

  public boolean isAnnotationPresent(Class<? extends Annotation> annotation) {
    return false;
  }

  public <A extends Annotation> A getAnnotation(Class<A> annotation) {
    return null;
  }

  public MetaTypeVariable[] getTypeParameters() {
    return new MetaTypeVariable[0];
  }

  public void setContext(Context context) {
    this.context = context;
  }

  public void setDeclaringClass(BuildMetaClass declaringClass) {
    this.declaringClass = declaringClass;
  }

  public void setScope(Scope scope) {
    this.scope = scope;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setReturnType(MetaClass returnType) {
    this.returnType = returnType;
  }

  public void setMetaParameters(List<MetaParameter> metaParameters) {
    this.metaParameters = metaParameters;
  }

  public void setGenericReturnType(MetaType genericReturnType) {
    this.genericReturnType = genericReturnType;
  }

  public void setGenericParameterTypes(List<MetaType> genericParameterTypes) {
    this.genericParameterTypes = genericParameterTypes;
  }

  public void setBody(Statement body) {
    this.body = body;
  }

  public void setDefParameters(DefParameters defParameters) {
    this.defParameters = defParameters;
  }

  public void setThrowsDeclaration(ThrowsDeclaration throwsDeclaration) {
    this.throwsDeclaration = throwsDeclaration;
  }

  public String toJavaString() {
    for (Parameter p : defParameters.getParameters()) {
      context.addVariable(Variable.create(p.getName(), p.getType()));
    }
    return new StringBuilder().append(scope.getCanonicalName())
            .append(" ")
            .append(LoadClassReference.getClassReference(returnType, context))
            .append(" ")
            .append(name)
            .append(defParameters.generate(context))
            .append(" ")
            .append(throwsDeclaration.generate(context))
            .append(" {\n").append(body.generate(context)).append("\n}\n")
            .toString();
  }
}
