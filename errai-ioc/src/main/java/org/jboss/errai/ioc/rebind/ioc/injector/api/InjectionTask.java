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

package org.jboss.errai.ioc.rebind.ioc.injector.api;

import static org.jboss.errai.codegen.util.PrivateAccessUtil.getPrivateFieldInjectorName;
import static org.jboss.errai.codegen.util.PrivateAccessUtil.getPrivateMethodName;

import java.lang.annotation.Annotation;

import org.jboss.errai.codegen.Statement;
import org.jboss.errai.codegen.exception.UnproxyableClassException;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.MetaConstructor;
import org.jboss.errai.codegen.meta.MetaField;
import org.jboss.errai.codegen.meta.MetaMethod;
import org.jboss.errai.codegen.meta.MetaParameter;
import org.jboss.errai.codegen.util.PrivateAccessType;
import org.jboss.errai.codegen.util.Refs;
import org.jboss.errai.codegen.util.Stmt;
import org.jboss.errai.ioc.rebind.ioc.bootstrapper.IOCProcessingContext;
import org.jboss.errai.ioc.rebind.ioc.exception.InjectionFailure;
import org.jboss.errai.ioc.rebind.ioc.exception.UnsatisfiedDependenciesException;
import org.jboss.errai.ioc.rebind.ioc.injector.InjectUtil;
import org.jboss.errai.ioc.rebind.ioc.injector.Injector;
import org.jboss.errai.ioc.rebind.ioc.metadata.QualifyingMetadata;

public class InjectionTask {
  protected final TaskType taskType;
  protected final Injector injector;

  protected final MetaConstructor constructor;
  protected final MetaField field;
  protected final MetaMethod method;
  protected final MetaClass type;
  protected final MetaParameter parm;

  public InjectionTask(Injector injector, MetaField field) {
    this.taskType = !field.isPublic() ? TaskType.PrivateField : TaskType.Field;
    this.injector = injector;
    this.field = field;
    this.constructor = null;
    this.method = null;
    this.parm = null;
    this.type = null;
  }

  public InjectionTask(Injector injector, MetaMethod method) {
    this.taskType = !method.isPublic() ? TaskType.PrivateMethod : TaskType.Method;
    this.injector = injector;
    this.method = method;
    this.constructor = null;
    this.field = null;
    this.parm = null;
    this.type = null;
  }

  public InjectionTask(Injector injector, MetaParameter parm) {
    this.taskType = TaskType.Parameter;
    this.injector = injector;
    this.parm = parm;
    this.field = null;
    this.type = null;

    if (parm.getDeclaringMember() instanceof MetaConstructor) {
      this.constructor = (MetaConstructor) parm.getDeclaringMember();
      this.method = null;
    }
    else {
      this.method = (MetaMethod) parm.getDeclaringMember();
      this.constructor = null;
    }
  }

  public InjectionTask(Injector injector, MetaClass type) {
    this.taskType = TaskType.Type;
    this.injector = injector;
    this.type = type;
    this.constructor = null;
    this.field = null;
    this.method = null;
    this.parm = null;
  }

  @SuppressWarnings({"unchecked"})
  public boolean doTask(InjectionContext ctx) {
    IOCProcessingContext processingContext = ctx.getProcessingContext();

    InjectableInstance injectableInstance = getInjectableInstance(ctx);

    QualifyingMetadata qualifyingMetadata = processingContext.getQualifyingMetadataFactory()
            .createFrom(injectableInstance.getQualifiers());
    Statement val;

    ctx.allowProxyCapture();

    switch (taskType) {
      case Type:
        ctx.getQualifiedInjector(type, qualifyingMetadata);
        break;

      case PrivateField: {
        try {
          val = InjectUtil.getInjectorOrProxy(ctx, getInjectableInstance(ctx), field.getType(), qualifyingMetadata);
        }
        catch (InjectionFailure e) {
          throw UnsatisfiedDependenciesException.createWithSingleFieldFailure(field, field.getDeclaringClass(),
                  field.getType(), e.getMessage());
        }
        catch (UnproxyableClassException e) {
          String err = "your object graph may have cyclical dependencies and the cycle could not be proxied. use of the @Dependent scope and @New qualifier may not " +
                  "produce properly initalized objects for: " + getInjector().getInjectedType().getFullyQualifiedName() + "\n" +
                  "\t Offending node: " + toString() + "\n" +
                  "\t Note          : this issue can be resolved by making "
                  + e.getUnproxyableClass().getFullyQualifiedName() + " proxyable. Introduce a default no-arg constructor and make sure the class is non-final.";

          throw UnsatisfiedDependenciesException.createWithSingleFieldFailure(field, field.getDeclaringClass(),
                  field.getType(), err);
        }

        final Statement fieldAccessStmt;

        if (field.isStatic()) {
          throw new InjectionFailure("attempt to inject bean into a static field: "
                  + field.getDeclaringClass().getFullyQualifiedName() + "." + field.getName());
        }
        else {
          fieldAccessStmt = Stmt.invokeStatic(processingContext.getBootstrapClass(),
                  getPrivateFieldInjectorName(field), Refs.get(injector.getVarName()), val);
        }

        processingContext.append(fieldAccessStmt);

        ctx.addExposedField(field, PrivateAccessType.Write);
        break;
      }

      case Field:
        try {
          val = InjectUtil.getInjectorOrProxy(ctx, getInjectableInstance(ctx), field.getType(), qualifyingMetadata);
        }
        catch (UnproxyableClassException e) {
          return false;
        }
        processingContext.append(
                Stmt.loadVariable(injector.getVarName()).loadField(field.getName()).assignValue(val)
        );

        break;

      case PrivateMethod: {
        for (MetaParameter parm : method.getParameters()) {
          ctx.getProcessingContext().handleDiscoveryOfType(
                  new InjectableInstance(null, TaskType.Parameter, null, method, null, parm.getType(), parm, injector, ctx));
          if (!ctx.isInjectableQualified(parm.getType(), qualifyingMetadata)) {
            return false;
          }
        }

        Statement[] stmts = InjectUtil.resolveInjectionDependencies(method.getParameters(), ctx, method);
        Statement[] parms;

        parms = new Statement[stmts.length + 1];
        parms[0] = Refs.get(injector.getVarName());
        System.arraycopy(stmts, 0, parms, 1, stmts.length);

        injectableInstance.getInjectionContext().addExposedMethod(method);

        processingContext.append(
                Stmt.invokeStatic(processingContext.getBootstrapClass(), getPrivateMethodName(method),
                        parms)
        );

        break;
      }

      case Method:
        for (MetaParameter parm : method.getParameters()) {
          ctx.getProcessingContext().handleDiscoveryOfType(
                  new InjectableInstance(null, TaskType.Parameter, null, method, null, parm.getType(), parm, injector, ctx));

          if (!ctx.isInjectableQualified(parm.getType(), qualifyingMetadata)) {
            return false;
          }
        }

        processingContext.append(
                Stmt.loadVariable(injector.getVarName()).invoke(method,
                        InjectUtil.resolveInjectionDependencies(method.getParameters(), ctx, method))
        );

        break;
    }

    ctx.closeProxyIfOpen();

    return true;
  }

  private InjectableInstance getInjectableInstance(InjectionContext ctx) {
    InjectableInstance<? extends Annotation> injectableInstance
            = new InjectableInstance(null, taskType, constructor, method, field, type, parm, injector, ctx);

    switch (taskType) {
      case Method:
      case PrivateMethod:
        break;

      default:
        ctx.getProcessingContext().handleDiscoveryOfType(injectableInstance);
    }

    return injectableInstance;
  }

  public Injector getInjector() {
    return injector;
  }

  public String toString() {
    switch (taskType) {
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
