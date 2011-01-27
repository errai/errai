/*
 * Copyright 2009 JBoss, a divison Red Hat, Inc
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
package org.jboss.errai.cdi.rebind;

import com.google.gwt.core.ext.typeinfo.JMethod;
import com.google.gwt.core.ext.typeinfo.JParameter;
import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.api.MessageCallback;
import org.jboss.errai.bus.client.framework.MessageBus;
import org.jboss.errai.cdi.client.CDIProtocol;
import org.jboss.errai.ioc.client.api.CodeDecorator;
import org.jboss.errai.ioc.rebind.ioc.Decorator;
import org.jboss.errai.ioc.rebind.ioc.InjectionContext;
import org.jboss.errai.ioc.rebind.ioc.InjectionPoint;

import javax.enterprise.event.Observes;

/**
 *
 * Generates the boiler plate for @Observes annotations use in GWT clients.<br/>
 * Basically creates a subscription for a CDI event type that invokes on the annotated method.
 *
 * @author: Heiko Braun <hbraun@redhat.com>
 * @date: Jul 27, 2010
 */
@CodeDecorator
public class ObservesDecorator extends Decorator<Observes> {
    public ObservesDecorator(Class<Observes> decoratesWith) {
        super(decoratesWith);
    }

    @Override
    public String generateDecorator(InjectionPoint<Observes> injectionPoint) {
        final InjectionContext ctx = injectionPoint.getInjectionContext();

        final JMethod method = injectionPoint.getMethod();
        final JParameter parm = injectionPoint.getParm();

        String parmClassName = parm.getType().getQualifiedSourceName();
        String varName = injectionPoint.getInjector().getVarName();


        // Get an instance of the message bus.
        final String messageBusInst = ctx.getInjector(ctx
                .getProcessingContext().loadClassType(MessageBus.class)).getType(ctx, injectionPoint);

        return messageBusInst + ".subscribe(\"cdi.event:" + parmClassName + "\", new " + MessageCallback.class.getName() + "() {\n" +
                "                    public void callback(" + Message.class.getName() + " message) {\n" +
                "                        java.lang.Object response = message.get(" + parmClassName + ".class, " + CDIProtocol.class.getName() + "." + CDIProtocol.OBJECT_REF.name() + ");\n" +
                "                        " + varName + "." + method.getName() + "((" + parmClassName + ") response);\n" +
                "                    }\n" +
                "                });";
    }
}