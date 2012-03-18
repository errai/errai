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

package org.jboss.errai.ioc.rebind.ioc.builtin;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;

import org.jboss.errai.bus.client.api.Local;
import org.jboss.errai.bus.client.framework.MessageBus;
import org.jboss.errai.bus.client.framework.Subscription;
import org.jboss.errai.bus.server.annotations.Service;
import org.jboss.errai.codegen.framework.Parameter;
import org.jboss.errai.codegen.framework.Statement;
import org.jboss.errai.codegen.framework.builder.AnonymousClassStructureBuilder;
import org.jboss.errai.codegen.framework.builder.BlockBuilder;
import org.jboss.errai.codegen.framework.builder.impl.ObjectBuilder;
import org.jboss.errai.codegen.framework.meta.MetaClass;
import org.jboss.errai.codegen.framework.util.Refs;
import org.jboss.errai.codegen.framework.util.Stmt;
import org.jboss.errai.ioc.client.api.CodeDecorator;
import org.jboss.errai.ioc.client.container.DestructionCallback;
import org.jboss.errai.ioc.rebind.ioc.extension.IOCDecoratorExtension;
import org.jboss.errai.ioc.rebind.ioc.injector.InjectUtil;
import org.jboss.errai.ioc.rebind.ioc.injector.api.InjectableInstance;
import org.jboss.errai.ioc.rebind.ioc.injector.api.InjectionContext;

import static org.jboss.errai.codegen.framework.meta.MetaClassFactory.parameterizedAs;
import static org.jboss.errai.codegen.framework.meta.MetaClassFactory.typeParametersOf;

@CodeDecorator
public class ServiceIOCExtension extends IOCDecoratorExtension<Service> {
  public ServiceIOCExtension(Class<Service> decoratesWith) {
    super(decoratesWith);
  }

  @Override
  public List<? extends Statement> generateDecorator(InjectableInstance<Service> decContext) {
    final InjectionContext ctx = decContext.getInjectionContext();

    /**
     * Ensure the the container generates a stub to internally expose the field if it's private.
     */
    decContext.ensureMemberExposed();

    final Statement busHandle = ctx.getInjector(MessageBus.class).getBeanInstance(ctx, decContext);

    /**
     * Figure out the service name;
     */
    final String svcName = decContext.getAnnotation().value().equals("")
            ? decContext.getMemberName() : decContext.getAnnotation().value();

    boolean local = false;
    for (Annotation a : decContext.getQualifiers()) {
      if (Local.class.equals(a.annotationType())) {
        local = true;
      }
    }

    final String varName = InjectUtil.getUniqueVarName();

    Statement subscribeStatement;

    if (local) {
      subscribeStatement = Stmt.nestedCall(busHandle)
              .invoke("subscribeLocal", svcName, decContext.getValueStatement());
    }
    else {
      subscribeStatement = Stmt.nestedCall(busHandle)
              .invoke("subscribe", svcName, decContext.getValueStatement());
    }

    Statement declareVar = Stmt.declareVariable(Subscription.class).asFinal().named(varName)
            .initializeWith(subscribeStatement);

    final MetaClass destructionCallbackType =
            parameterizedAs(DestructionCallback.class, typeParametersOf(decContext.getEnclosingType()));

    // register a destructor to unregister the service when the bean is destroyed.
    final BlockBuilder<AnonymousClassStructureBuilder> destroyMeth
            = ObjectBuilder.newInstanceOf(destructionCallbackType).extend()
            .publicOverridesMethod("destroy", Parameter.of(decContext.getEnclosingType(), "obj", true))
            .append(Stmt.loadVariable(varName).invoke("remove"));

    Statement descrCallback = Stmt.create().loadVariable("context").invoke("addDestructionCallback",
            Refs.get(decContext.getInjector().getVarName()), destroyMeth.finish().finish());


    return Arrays.asList(declareVar, descrCallback);
  }
}