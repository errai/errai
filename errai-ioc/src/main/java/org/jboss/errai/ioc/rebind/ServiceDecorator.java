/*
 * Copyright 2010 JBoss, a divison Red Hat, Inc
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

package org.jboss.errai.ioc.rebind;

import org.jboss.errai.bus.client.framework.MessageBus;
import org.jboss.errai.bus.server.annotations.Service;
import org.jboss.errai.ioc.client.api.CodeDecorator;
import org.jboss.errai.ioc.rebind.ioc.Decorator;
import org.jboss.errai.ioc.rebind.ioc.InjectionContext;
import org.jboss.errai.ioc.rebind.ioc.InjectionPoint;

@CodeDecorator
public class ServiceDecorator extends Decorator<Service> {
    public ServiceDecorator(Class<Service> decoratesWith) {
        super(decoratesWith);
    }

    @Override
    public String generateDecorator(InjectionPoint<Service> decContext) {
        final InjectionContext ctx = decContext.getInjectionContext();

        /**
         * Ensure the the container generates a stub to internally expose the field if it's private.
         */
        decContext.ensureFieldExposed();

        /**
         * Get an instance of the message bus.
         */
        final String inj = ctx.getInjector(decContext.getInjectionContext()
                .getProcessingContext().loadClassType(MessageBus.class)).getType(ctx, decContext);

        /**
         * Figure out the service name;
         */
        final String svcName = decContext.getAnnotation().value().equals("")
                ? decContext.getMemberName() : decContext.getAnnotation().value();

        return inj + ".subscribe(\"" + svcName + "\", " + decContext.getValueExpression() + ");\n";
    }
}