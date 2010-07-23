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

package org.jboss.errai.ioc.rebind.ioc;

import com.google.gwt.core.ext.typeinfo.JClassType;
import org.jboss.errai.bus.client.framework.MessageBus;
import org.jboss.errai.bus.rebind.ProcessingContext;
import org.jboss.errai.bus.server.annotations.Service;

import java.util.List;

public class InjectorFactory {
    private final InjectionContext ctx;

    public InjectorFactory(ProcessingContext ctx) {
        this.ctx = new InjectionContext(ctx);

        /**
         * Create a decorator that is capable of registering services on injected classes.
         */
        this.ctx.registerDecorator(new Decorator<Service>(Service.class) {
            @Override
            public String generateDecorator(InjectionPoint<Service> decContext) {
                final InjectionContext ctx = decContext.getInjectionContext();

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
        });
    }

    public InjectionContext getInjectionContext() {
        return ctx;
    }

    public String generate(JClassType type) {
        return ctx.getInjector(type).getType(ctx, null);
    }

    public String generateSingleton(JClassType type) {
        Injector i = ctx.getInjector(type);
        if (i.isInjected()) {
            return i.getVarName();
        }
        else {
            return i.getType(ctx, null);
        }
    }

    public void addType(JClassType type) {
        ctx.registerInjector(new TypeInjector(type));
    }

    public void addInjector(Injector injector) {
        ctx.registerInjector(injector);
    }

    public String generateAllProviders() {
        List<Injector> injs = ctx.getInjectorsByType(ProviderInjector.class);
        for (Injector i : injs) {
            i.instantiateOnly(ctx, null);
        }
        return "";
    }

}
