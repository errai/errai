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
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.event.Observes;
import javax.enterprise.util.TypeLiteral;

import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.api.MessageCallback;
import org.jboss.errai.bus.client.api.annotations.Local;
import org.jboss.errai.bus.client.framework.MessageBus;
import org.jboss.errai.cdi.client.CDIProtocol;
import org.jboss.errai.cdi.client.api.CDI;
import org.jboss.errai.ioc.client.api.CodeDecorator;
import org.jboss.errai.ioc.rebind.ioc.IOCDecoratorExtension;
import org.jboss.errai.ioc.rebind.ioc.InjectUtil;
import org.jboss.errai.ioc.rebind.ioc.InjectionPoint;
import org.jboss.errai.ioc.rebind.ioc.codegen.BooleanOperator;
import org.jboss.errai.ioc.rebind.ioc.codegen.Cast;
import org.jboss.errai.ioc.rebind.ioc.codegen.Context;
import org.jboss.errai.ioc.rebind.ioc.codegen.Parameter;
import org.jboss.errai.ioc.rebind.ioc.codegen.Statement;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.BlockBuilder;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.impl.ExtendsClassStructureBuilderImpl;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.impl.ObjectBuilder;
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
  public Statement generateDecorator(InjectionPoint<Observes> injectionPoint) {
    final Context ctx = injectionPoint.getInjectionContext().getProcessingContext().getContext();
    final MetaMethod method = injectionPoint.getMethod();
    final MetaParameter parm = injectionPoint.getParm();

    final String parmClassName = parm.getType().getFullyQualifiedName();
    final Statement messageBusInst = injectionPoint.getInjectionContext().getInjector(MessageBus.class).getType(injectionPoint);
    final String subscribeMethodName = method.isAnnotationPresent(Local.class) ? "subscribeLocal" : "subscribe";

    final String subject = CDI.getSubjectNameByType(parmClassName);
    final Annotation[] qualifiers = InjectUtil.extractQualifiers(injectionPoint).toArray(new Annotation[0]);
    final Set<String> qualifierNames = CDI.getQualifiersPart(qualifiers);

    BlockBuilder<ExtendsClassStructureBuilderImpl> callBackBlock = ObjectBuilder
        .newInstanceOf(MessageCallback.class, ctx)
        .extend()
        .publicOverridesMethod("callback", Parameter.of(Message.class, "message"))
        .append(
            Stmt.create().declareVariable("methodQualifiers", new TypeLiteral<Set<String>>() {},
                Stmt.create().newObject(new TypeLiteral<HashSet<String>>() {})));

    if (qualifierNames != null) {
      for (String qualifierName : qualifierNames) {
        callBackBlock.append(Stmt.create().loadVariable("methodQualifiers").invoke("add", qualifierName));
      }
    }
    callBackBlock.append(Stmt.create().declareVariable("qualifiers",new TypeLiteral<Set<String>>() {},
        Stmt.create().loadVariable("message")
            .invoke("get", Set.class, CDIProtocol.class.getName() + "." + CDIProtocol.QUALIFIERS.name())));

    callBackBlock.append(Stmt
        .create()
        .loadVariable("methodQualifiers")
        .invoke("equals", Cast.to(HashSet.class, Refs.get("qualifiers")))
        .if_(
            BooleanOperator.Or,
            Bool.and(Bool.equals(Refs.get("qualifiers"), null),
                Stmt.create().loadVariable("methodQualifiers").invoke("isEmpty")))
        .append(
            Stmt.create().declareVariable(
                "response",
                Object.class,
                Stmt.create()
                    .loadVariable("message")
                    .invoke("get", parm.getType().asClass(), CDIProtocol.OBJECT_REF)))
        .append(
            Stmt.create().loadVariable(injectionPoint.getInjector().getVarName())
                .invoke(method.getName(), Cast.to(parm.getType(), Refs.get("response")))).finish());

    Statement messageCallback = callBackBlock.finish().finish();
    return Stmt.create(ctx).nestedCall(messageBusInst).invoke(subscribeMethodName, subject, messageCallback);
  };
}