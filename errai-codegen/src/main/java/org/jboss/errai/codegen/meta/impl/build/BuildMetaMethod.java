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

package org.jboss.errai.codegen.meta.impl.build;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

import org.jboss.errai.codegen.Comment;
import org.jboss.errai.codegen.Context;
import org.jboss.errai.codegen.DefModifiers;
import org.jboss.errai.codegen.DefParameters;
import org.jboss.errai.codegen.Modifier;
import org.jboss.errai.codegen.Parameter;
import org.jboss.errai.codegen.Statement;
import org.jboss.errai.codegen.ThrowsDeclaration;
import org.jboss.errai.codegen.Variable;
import org.jboss.errai.codegen.builder.Builder;
import org.jboss.errai.codegen.builder.callstack.LoadClassReference;
import org.jboss.errai.codegen.builder.impl.Scope;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.MetaClassMember;
import org.jboss.errai.codegen.meta.MetaMethod;
import org.jboss.errai.codegen.meta.MetaParameter;
import org.jboss.errai.codegen.meta.MetaType;
import org.jboss.errai.codegen.meta.MetaTypeVariable;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class BuildMetaMethod extends MetaMethod implements Builder {
  private Context context;
  private BuildMetaClass declaringClass;
  private Statement body;

  private Scope scope;
  private DefModifiers modifiers;

  private String name;
  private MetaClass returnType;
  private DefParameters defParameters;

  private List<MetaType> genericParameterTypes;

  private ThrowsDeclaration throwsDeclaration;

  private MetaMethod reifiedFormOf;

  private String methodComment;

  public BuildMetaMethod(BuildMetaClass declaringClass,
                         Statement body,
                         Scope scope,
                         DefModifiers modifiers,
                         String name,
                         MetaClass returnType,
                         MetaType genericReturnType,
                         DefParameters defParameters,
                         ThrowsDeclaration throwsDeclaration
  ) {

    this.declaringClass = declaringClass;
    this.body = body;
    this.modifiers = modifiers;
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

  // don't try to support this for now.
  @Override
  public MetaType getGenericReturnType() {
    return null;
  }

  @Override
  public MetaType[] getGenericParameterTypes() {
    if (genericParameterTypes == null)
      return new MetaType[0];

    return genericParameterTypes.toArray(new MetaType[genericParameterTypes.size()]);
  }

  @Override
  public MetaParameter[] getParameters() {
    List<Parameter> parameters = defParameters.getParameters();
    if (parameters != null) {
      List<MetaParameter> metaParameterList = new ArrayList<MetaParameter>();
      for (final Parameter p : parameters) {
        metaParameterList.add(new MetaParameter() {
          @Override
          public String getName() {
            return p.getName();
          }

          @Override
          public MetaClass getType() {
            return p.getType();
          }

          @Override
          public MetaClassMember getDeclaringMember() {
            return BuildMetaMethod.this;
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
        });
      }

      return metaParameterList.toArray(new MetaParameter[metaParameterList.size()]);
    }
    else {
      return new MetaParameter[0];
    }
  }

  @Override
  public MetaClass getDeclaringClass() {
    return declaringClass;
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
  public boolean isSynthetic() {
    return false;
  }

  @Override
  public boolean isVolatile() {
    return false;
  }

  @Override
  public boolean isSynchronized() {
    return modifiers.hasModifier(Modifier.Synchronized);
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
  public MetaTypeVariable[] getTypeParameters() {
    return new MetaTypeVariable[0];
  }

  @Override
  public MetaClass[] getCheckedExceptions() {
    return throwsDeclaration.getExceptionTypes();
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

  public void setGenericReturnType(MetaType genericReturnType) {
    //   this.genericReturnType = genericReturnType;
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

  @Override
  public boolean isVarArgs() {
    return false;
  }

  public boolean isReifiedForm() {
    return reifiedFormOf != null;
  }

  public MetaMethod getReifiedFormOf() {
    return reifiedFormOf;
  }

  public void setReifiedFormOf(MetaMethod reifiedFormOf) {
    this.reifiedFormOf = reifiedFormOf;
  }

  public void setMethodComment(String methodComment) {
    this.methodComment = methodComment;
  }

  @Override
  public String toJavaString() {
    this.context = Context.create(declaringClass.getContext());


    for (Parameter p : defParameters.getParameters()) {
      context.addVariable(Variable.create(p.getName(), p.getType()));
    }
    StringBuilder buf = new StringBuilder(256);

    if (methodComment != null) {
      buf.append(new Comment(methodComment).generate(null)).append("\n");
    }

    buf.append(scope.getCanonicalName()).append(" ")
            .append(modifiers.toJavaString()).append(" ");

    buf.append(LoadClassReference.getClassReference(returnType, context, false))
            .append(" ")
            .append(name)
            .append(defParameters.generate(context));

    if (!throwsDeclaration.isEmpty()) {
      buf.append(" ")
              .append(throwsDeclaration.generate(context));
    }

    if (modifiers.hasModifier(Modifier.Abstract)) {
      buf.append(";");
    }
    else if (modifiers.hasModifier(Modifier.JSNI)) {
      buf.append(" /*-{\n").append(body.generate(context)).append("\n}-*/;\n");
    }
    else {
      buf.append(" {\n").append(body.generate(context)).append("\n}\n");
    }

    return buf.toString();
  }

  public String toString() {
    return name + defParameters;
  }
}
