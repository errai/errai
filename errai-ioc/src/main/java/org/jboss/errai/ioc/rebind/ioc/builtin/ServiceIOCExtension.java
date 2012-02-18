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

import org.jboss.errai.bus.client.api.Local;
import org.jboss.errai.bus.client.framework.MessageBus;
import org.jboss.errai.bus.server.annotations.Service;
import org.jboss.errai.codegen.framework.Statement;
import org.jboss.errai.codegen.framework.util.Stmt;
import org.jboss.errai.ioc.client.api.CodeDecorator;
import org.jboss.errai.ioc.rebind.ioc.IOCDecoratorExtension;
import org.jboss.errai.ioc.rebind.ioc.InjectableInstance;
import org.jboss.errai.ioc.rebind.ioc.InjectionContext;

@CodeDecorator
public class ServiceIOCExtension extends IOCDecoratorExtension<Service> {
  public ServiceIOCExtension(Class<Service> decoratesWith) {
    super(decoratesWith);
  }

  @Override
  public Statement generateDecorator(InjectableInstance<Service> decContext) {
    final InjectionContext ctx = decContext.getInjectionContext();

    /**
     * Ensure the the container generates a stub to internally expose the field if it's private.
     */
    decContext.ensureMemberExposed();

    final Statement busHandle = ctx.getInjector(MessageBus.class).getType(ctx, decContext);

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

    if (local) {
     return Stmt.nestedCall(busHandle)
             .invoke("subscribeLocal", svcName, decContext.getValueStatement());
    }
    else {
      return Stmt.nestedCall(busHandle)
              .invoke("subscribe", svcName, decContext.getValueStatement());
    }
  }
}