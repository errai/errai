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

import org.jboss.errai.ioc.rebind.ioc.codegen.Context;
import org.jboss.errai.ioc.rebind.ioc.codegen.DefParameters;
import org.jboss.errai.ioc.rebind.ioc.codegen.Parameter;
import org.jboss.errai.ioc.rebind.ioc.codegen.Statement;
import org.jboss.errai.ioc.rebind.ioc.codegen.Variable;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.Builder;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.impl.Scope;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.*;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

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
  //  private List<Annotation> annotations = new ArrayList<Annotation>();
  private List<MetaTypeVariable> typeVariables = new ArrayList<MetaTypeVariable>();

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

  public MetaClass getDeclaringClass() {
    return declaringClass;
  }

  public boolean isAbstract() {
    return isAbstract;
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

  public void setScope(Scope scope) {
    this.scope = scope;
  }

  public Annotation[] getAnnotations() {
    return new Annotation[0];
  }

  public boolean isAnnotationPresent(Class<? extends Annotation> annotation) {
//    for (Annotation a : annotations) {
//      if (a.getClass().equals(annotation)) return true;
//    }
    return false;
  }

  public <A extends Annotation> A getAnnotation(Class<A> annotation) {
    return null;
  }

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
}
