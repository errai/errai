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
  private DefModifiers modifiers;

  private String name;
  private MetaClass returnType;
  private DefParameters defParameters;

  private MetaType genericReturnType;
  private List<MetaType> genericParameterTypes;

  private ThrowsDeclaration throwsDeclaration;

  public BuildMetaMethod(BuildMetaClass declaringClass,
                         Statement body,
                         Scope scope,
                         DefModifiers modifiers,
                         String name,
                         MetaClass returnType,
                         DefParameters defParameters,
                         ThrowsDeclaration throwsDeclaration
                         ) {

    this.context = Context.create(declaringClass.getContext());
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
    List<Parameter> parameters = defParameters.getParameters();
    if (parameters != null) {
      return defParameters.getParameters().toArray(new MetaParameter[defParameters.getParameters().size()]);
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
  public boolean isSynchronized() {
    return false;
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

  @Override
  public String toJavaString() {
    for (Parameter p : defParameters.getParameters()) {
      context.addVariable(Variable.create(p.getName(), p.getType()));
    }
    StringBuilder buf = new StringBuilder().append(scope.getCanonicalName())
            .append(" ");

    buf.append(modifiers.toJavaString()).append(" ");

    buf.append(LoadClassReference.getClassReference(returnType, context))
        .append(" ")
        .append(name)
        .append(defParameters.generate(context));
        
    if (!throwsDeclaration.isEmpty()) {    
      buf.append(" ")
          .append(throwsDeclaration.generate(context));
    }
    
     if (modifiers.hasModifier(Modifier.Abstract)) {
       buf.append(";");
     } else {
       buf.append(" {\n").append(body.generate(context)).append("\n}\n");
     }

    return buf.toString();
  }
}
