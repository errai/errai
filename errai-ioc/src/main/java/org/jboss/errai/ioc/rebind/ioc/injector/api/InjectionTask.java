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

import org.jboss.errai.codegen.Statement;
import org.jboss.errai.codegen.exception.UnproxyableClassException;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.MetaConstructor;
import org.jboss.errai.codegen.meta.MetaField;
import org.jboss.errai.codegen.meta.MetaMethod;
import org.jboss.errai.codegen.meta.MetaParameter;
import org.jboss.errai.codegen.util.PrivateAccessType;
import org.jboss.errai.codegen.util.Refs;
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

  public InjectionTask(final Injector injector, final MetaField field) {
    this.taskType = !field.isPublic() ? TaskType.PrivateField : TaskType.Field;
    this.injector = injector;
    this.field = field;
    this.constructor = null;
    this.method = null;
    this.parm = null;
    this.type = null;
  }

  public InjectionTask(final Injector injector, final MetaMethod method) {
    this.taskType = !method.isPublic() ? TaskType.PrivateMethod : TaskType.Method;
    this.injector = injector;
    this.method = method;
    this.constructor = null;
    this.field = null;
    this.parm = null;
    this.type = null;
  }

  public InjectionTask(final Injector injector, final MetaParameter parm) {
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

  public InjectionTask(final Injector injector, final MetaClass type) {
    this.taskType = TaskType.Type;
    this.injector = injector;
    this.type = type;
    this.constructor = null;
    this.field = null;
    this.method = null;
    this.parm = null;
  }

  @SuppressWarnings({"unchecked"})
  public boolean doTask(final InjectionContext ctx) {
    final IOCProcessingContext processingContext = ctx.getProcessingContext();

    final InjectableInstance injectableInstance = getInjectableInstance(ctx);

    final QualifyingMetadata qualifyingMetadata = processingContext.getQualifyingMetadataFactory()
            .createFrom(injectableInstance.getQualifiers());
    final Statement val;

    ctx.allowProxyCapture();

    switch (taskType) {
      case Type:
        ctx.getProcessingContext().handleDiscoveryOfType(injectableInstance, type);
        ctx.getQualifiedInjector(type, qualifyingMetadata);
        break;

      case PrivateField:
        ctx.addExposedField(field, PrivateAccessType.Write);

      case Field:
        ctx.getProcessingContext().handleDiscoveryOfType(injectableInstance, field.getType());
        try {
           val = InjectUtil.getInjectorOrProxy(ctx, getInjectableInstance(ctx), field.getType(), qualifyingMetadata);
         }
         catch (InjectionFailure e) {
           throw UnsatisfiedDependenciesException.createWithSingleFieldFailure(field, field.getDeclaringClass(),
                   field.getType(), e.getMessage());
         }
         catch (UnproxyableClassException e) {
           final String err = "your object graph may have cyclical dependencies and the cycle could not be proxied. " +
               "use of the @Dependent scope and @New qualifier may not " +
                   "produce properly initalized objects for: " + getInjector().getInjectedType().getFullyQualifiedName() + "\n" +
                   "\t Offending node: " + toString() + "\n" +
                   "\t Note          : this issue can be resolved by making "
                   + e.getUnproxyableClass().getFullyQualifiedName() + " proxyable. Introduce a default" +
               " no-arg constructor and make sure the class is non-final.";

           throw UnsatisfiedDependenciesException.createWithSingleFieldFailure(field, field.getDeclaringClass(),
                   field.getType(), err);
         }

         final Statement fieldAccessStmt;

         if (field.isStatic()) {
           throw new InjectionFailure("attempt to inject bean into a static field: "
                   + field.getDeclaringClass().getFullyQualifiedName() + "." + field.getName());
         }
         else {
           fieldAccessStmt = InjectUtil.setPublicOrPrivateFieldValue(ctx, Refs.get(injector.getInstanceVarName()), field, val);
         }

         processingContext.append(fieldAccessStmt);

        break;

      case PrivateMethod:
        ctx.addExposedMethod(method);

      case Method:
        for (final MetaParameter parm : method.getParameters()) {
          ctx.getProcessingContext().handleDiscoveryOfType(
              InjectableInstance.getParameterInjectedInstance(parm, injector, ctx),
              parm.getType());
        }

        final Statement[] args = InjectUtil.resolveInjectionDependencies(method.getParameters(), ctx, method);

        processingContext.append(
                InjectUtil.invokePublicOrPrivateMethod(ctx,
                        Refs.get(injector.getInstanceVarName()),
                        method,
                        args)
        );
        break;
    }

    ctx.closeProxyIfOpen();

    return true;
  }

  private InjectableInstance getInjectableInstance(final InjectionContext ctx) {
    return new InjectableInstance(null, taskType, constructor, method, field, type, parm, injector, ctx);
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
