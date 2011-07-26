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
import javax.enterprise.util.TypeLiteral;

import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.api.annotations.Local;
import org.jboss.errai.bus.client.framework.MessageBus;
import org.jboss.errai.enterprise.client.cdi.AbstractCDIEventCallback;
import org.jboss.errai.enterprise.client.cdi.CDIProtocol;
import org.jboss.errai.enterprise.client.cdi.api.CDI;
import org.jboss.errai.ioc.client.api.CodeDecorator;
import org.jboss.errai.ioc.rebind.ioc.IOCDecoratorExtension;
import org.jboss.errai.ioc.rebind.ioc.InjectUtil;
import org.jboss.errai.ioc.rebind.ioc.InjectableInstance;
import org.jboss.errai.ioc.rebind.ioc.codegen.Context;
import org.jboss.errai.ioc.rebind.ioc.codegen.Parameter;
import org.jboss.errai.ioc.rebind.ioc.codegen.Statement;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.BlockBuilder;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.impl.AnonymousClassStructureBuilderImpl;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaMethod;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaParameter;
import org.jboss.errai.ioc.rebind.ioc.codegen.util.Bool;
import org.jboss.errai.ioc.rebind.ioc.codegen.util.Refs;
import org.jboss.errai.ioc.rebind.ioc.codegen.util.Stmt;

/**
 * Generates the boiler plate for @Observes annotations use in GWT clients.<br/>
 * Basically creates a subscription for a CDI event type that invokes on the annotated method.
 *
 * @author Heiko Braun <hbraun@redhat.com>
 * @author Mike Brock <cbrock@redhat.com>
 * @author Christian Sadilek <csadilek@redhat.com>
 */
@CodeDecorator
public class ObservesExtension extends IOCDecoratorExtension<Observes> {

  public ObservesExtension(Class<Observes> decoratesWith) {
    super(decoratesWith);
  }

  @Override
  public Statement generateDecorator(InjectableInstance<Observes> instance) {
    final Context ctx = instance.getInjectionContext().getProcessingContext().getContext();
    final MetaMethod method = instance.getMethod();
    final MetaParameter parm = instance.getParm();

    final String parmClassName = parm.getType().getFullyQualifiedName();
    final Statement bus = instance.getInjectionContext().getInjector(MessageBus.class).getType(instance);
    final String subscribeMethodName = method.isAnnotationPresent(Local.class) ? "subscribeLocal" : "subscribe";

    final String subject = CDI.getSubjectNameByType(parmClassName);
    final Annotation[] qualifiers = InjectUtil.extractQualifiers(instance).toArray(new Annotation[0]);
    final Set<String> qualifierNames = CDI.getQualifiersPart(qualifiers);

    AnonymousClassStructureBuilderImpl callBack = Stmt.newObject(AbstractCDIEventCallback.class).extend();

    BlockBuilder<AnonymousClassStructureBuilderImpl> callBackBlock;
    if (qualifierNames != null) {
      callBackBlock = callBack.initialize();
      for (String qualifierName : qualifierNames) {
        callBackBlock.append(Stmt.loadClassMember("qualifiers").invoke("add", qualifierName));
      }
      callBack = callBackBlock.finish();
    }

    callBackBlock = callBack.publicOverridesMethod("callback", Parameter.of(Message.class, "message"))
        .append(Stmt.declareVariable("msgQualifiers", new TypeLiteral<Set<String>>() {}, 
            Stmt.loadVariable("message").invoke("get", Set.class, CDIProtocol.QUALIFIERS)))
        .append(Stmt
            .if_(Bool.or(
                Stmt.loadClassMember("qualifiers").invoke("equals", Refs.get("msgQualifiers")),
                Bool.and(Bool.equals(Refs.get("msgQualifiers"), null), 
                    Stmt.loadClassMember("qualifiers").invoke("isEmpty"))))
            .append(Stmt.loadVariable(instance.getInjector().getVarName()).invoke(method.getName(), 
                Stmt.loadVariable("message").invoke("get", parm.getType().asClass(), CDIProtocol.OBJECT_REF)))
            .finish());

    return Stmt.create(ctx).nestedCall(bus).invoke(subscribeMethodName, subject, callBackBlock.finish().finish());
  }
}