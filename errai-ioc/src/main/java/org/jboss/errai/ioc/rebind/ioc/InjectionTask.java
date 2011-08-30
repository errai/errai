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

package org.jboss.errai.ioc.rebind.ioc;

import static org.jboss.errai.ioc.rebind.ioc.InjectUtil.getPrivateFieldInjectorName;
import static org.jboss.errai.ioc.rebind.ioc.InjectUtil.getPrivateMethodName;
import static org.jboss.errai.ioc.rebind.ioc.InjectUtil.resolveInjectionDependencies;

import java.lang.annotation.Annotation;

import org.jboss.errai.ioc.rebind.IOCProcessingContext;
import org.jboss.errai.codegen.framework.Statement;
import org.jboss.errai.codegen.framework.meta.MetaClass;
import org.jboss.errai.codegen.framework.meta.MetaConstructor;
import org.jboss.errai.codegen.framework.meta.MetaField;
import org.jboss.errai.codegen.framework.meta.MetaMethod;
import org.jboss.errai.codegen.framework.meta.MetaParameter;
import org.jboss.errai.codegen.framework.util.Refs;
import org.jboss.errai.codegen.framework.util.Stmt;

public class InjectionTask {
  protected final TaskType injectType;
  protected final Injector injector;

  protected MetaConstructor constructor;
  protected MetaField field;
  protected MetaMethod method;
  protected MetaClass type;
  protected MetaParameter parm;

  public InjectionTask(Injector injector, MetaField field) {
    this.injectType = !field.isPublic() ? TaskType.PrivateField : TaskType.Field;
    this.injector = injector;
    this.field = field;
  }

  public InjectionTask(Injector injector, MetaMethod method) {
    this.injectType = !method.isPublic() ? TaskType.PrivateMethod : TaskType.Method;
    this.injector = injector;
    this.method = method;
  }

  public InjectionTask(Injector injector, MetaParameter parm) {
    this.injectType = TaskType.Parameter;
    this.injector = injector;
    this.parm = parm;
  }

  public InjectionTask(Injector injector, MetaClass type) {
    this.injectType = TaskType.Type;
    this.injector = injector;
    this.type = type;
  }

  @SuppressWarnings({"unchecked"})
  public boolean doTask(InjectionContext ctx) {
    IOCProcessingContext processingContext = ctx.getProcessingContext();

    InjectableInstance<? extends Annotation> injectableInstance
            = new InjectableInstance(null, injectType, constructor, method, field, type, parm, injector, ctx);

    Injector inj;
    QualifyingMetadata qualifyingMetadata = processingContext.getQualifyingMetadataFactory()
            .createFrom(injectableInstance.getQualifiers());

    switch (injectType) {
      case Type:
        ctx.getQualifiedInjector(type, qualifyingMetadata);
        break;

      case PrivateField: {
        if (!ctx.isInjectableQualified(field.getType(), qualifyingMetadata)) {
          return false;
        }

        try {
          inj = ctx.getQualifiedInjector(field.getType(), qualifyingMetadata);
        }
        catch (InjectionFailure e) {
          e.setTarget(toString());
          throw e;
        }

        processingContext.append(
                Stmt.invokeStatic(processingContext.getBootstrapClass(), getPrivateFieldInjectorName(field),
                        Refs.get(injector.getVarName()), inj.getType(ctx, injectableInstance))
        );

        ctx.addExposedField(field);
        break;
      }

      case Field:
        if (!ctx.isInjectableQualified(field.getType(), qualifyingMetadata)) {
          return false;
        }

        try {
          inj = ctx.getQualifiedInjector(field.getType(), qualifyingMetadata);
        }
        catch (InjectionFailure e) {
          e.setTarget(toString());
          throw e;
        }

        processingContext.append(
                Stmt.loadVariable(injector.getVarName()).loadField(field.getName()).assignValue(inj.getType(ctx,
                        injectableInstance))
        );

        break;

      case PrivateMethod: {
        for (MetaParameter parm : method.getParameters()) {
          if (!ctx.isInjectableQualified(parm.getType(), qualifyingMetadata)) {
            return false;
          }
        }

        Statement[] smts = resolveInjectionDependencies(method.getParameters(), ctx, method);
        Statement[] parms = new Statement[smts.length];
        parms[0] = Refs.get(injector.getVarName());
        System.arraycopy(smts, 0, parms, 1, smts.length);

        processingContext.append(
                Stmt.invokeStatic(processingContext.getBootstrapClass(), getPrivateMethodName(method),
                        parms)
        );

        break;
      }

      case Method:
        for (MetaParameter parm : method.getParameters()) {
          if (!ctx.isInjectableQualified(parm.getType(), qualifyingMetadata)) {
            return false;
          }
        }

        processingContext.append(
                Stmt.loadVariable(injector.getVarName()).invoke(method,
                        resolveInjectionDependencies(method.getParameters(), ctx, method))
        );

        break;
    }

    return true;
  }

  public TaskType getInjectType() {
    return injectType;
  }

  public Injector getInjector() {
    return injector;
  }

  public MetaField getField() {
    return field;
  }

  public MetaMethod getMethod() {
    return method;
  }

  public void setMethod(MetaMethod method) {
    if (this.method == null)
      this.method = method;
  }

  public void setField(MetaField field) {
    if (this.field == null)
      this.field = field;
  }

  public String toString() {
    switch (injectType) {
      case Type:
        return type.getFullyQualifiedName();
      case Method:
        return method.getDeclaringClass().getFullyQualifiedName() + "." + method.getName() + "()::" + method
                .getReturnType().getFullyQualifiedName();
      case PrivateField:
      case Field:
        return field.getDeclaringClass().getFullyQualifiedName() + "." + field.getName() + "::" + field.getType()
                .getFullyQualifiedName();
    }

    return null;
  }
}
