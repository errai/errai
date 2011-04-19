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

import java.lang.annotation.Annotation;
import java.util.Set;

import javax.enterprise.event.Observes;

import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.api.MessageCallback;
import org.jboss.errai.bus.client.api.annotations.Local;
import org.jboss.errai.bus.client.framework.MessageBus;
import org.jboss.errai.cdi.client.CDIProtocol;
import org.jboss.errai.cdi.client.api.CDI;
import org.jboss.errai.ioc.client.api.CodeDecorator;
import org.jboss.errai.ioc.rebind.ioc.Decorator;
import org.jboss.errai.ioc.rebind.ioc.InjectUtil;
import org.jboss.errai.ioc.rebind.ioc.InjectionContext;
import org.jboss.errai.ioc.rebind.ioc.InjectionPoint;

import com.google.gwt.core.ext.typeinfo.JMethod;
import com.google.gwt.core.ext.typeinfo.JParameter;

/**
 *
 * Generates the boiler plate for @Observes annotations use in GWT clients.<br/>
 * Basically creates a subscription for a CDI event type that invokes on the annotated method.
 *
 * @author Heiko Braun
 * @author Mike Brock
 * @author Christian Sadilek <csadilek@redhat.com>
 *
 * @date Jul 27, 2010
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

        final String subscribeType = method.isAnnotationPresent(Local.class) ? "subscribeLocal" : "subscribe";
        
        final String subject = CDI.getSubjectNameByType(parmClassName);
        final Annotation[] qualifiers = InjectUtil.extractQualifiers(injectionPoint).toArray(new Annotation[0]);
        final Set<String> qualifierNames = CDI.getQualifiersPart(qualifiers);
        
        String expr = messageBusInst + "." + subscribeType + "(\"" + subject + "\", new " + MessageCallback.class.getName() + "() {\n" +
                "                    public void callback(" + Message.class.getName() + " message) {\n" +
                "						java.util.List<String> methodQualifiers = new java.util.ArrayList<String>() {{\n";
                						for(String qualifierName : qualifierNames) expr+=
                "							add(\""+qualifierName+"\");\n";
                						expr+=
            	"						}};\n" + 
            	
            	"						java.util.Set<String> qualifiers = message.get(java.util.Set.class,"+CDIProtocol.class.getName() + "." + CDIProtocol.QUALIFIERS.name()+");\n" +
            	"						for(String methodQualifier : methodQualifiers) {\n" +
            	"							if(!qualifiers.contains(methodQualifier)) return;\n" +
            	"						}\n" +
                
                "                        java.lang.Object response = message.get(" + parmClassName + ".class, " + CDIProtocol.class.getName() + "." + CDIProtocol.OBJECT_REF.name() + ");\n" +
                "                        " + varName + "." + method.getName() + "((" + parmClassName + ") response);\n" +
                "                    }\n" +
                "                });\n";

      //  expr += CDI.class.getName() + ".addRemoteEventType(\"" + parmClassName + "\");\n";

        return expr;
    };
}