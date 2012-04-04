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
package org.jboss.errai.enterprise.rebind;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import javax.enterprise.event.Observes;
import javax.enterprise.util.TypeLiteral;

import org.jboss.errai.bus.client.api.Local;
import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.framework.MessageBus;
import org.jboss.errai.bus.client.framework.Subscription;
import org.jboss.errai.codegen.Context;
import org.jboss.errai.codegen.Parameter;
import org.jboss.errai.codegen.Statement;
import org.jboss.errai.codegen.builder.AnonymousClassStructureBuilder;
import org.jboss.errai.codegen.builder.BlockBuilder;
import org.jboss.errai.codegen.builder.impl.ObjectBuilder;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.MetaMethod;
import org.jboss.errai.codegen.meta.MetaParameter;
import org.jboss.errai.codegen.util.Bool;
import org.jboss.errai.codegen.util.GenUtil;
import org.jboss.errai.codegen.util.PrivateAccessType;
import org.jboss.errai.codegen.util.Refs;
import org.jboss.errai.codegen.util.Stmt;
import org.jboss.errai.enterprise.client.cdi.AbstractCDIEventCallback;
import org.jboss.errai.enterprise.client.cdi.CDIProtocol;
import org.jboss.errai.enterprise.client.cdi.api.CDI;
import org.jboss.errai.ioc.client.api.CodeDecorator;
import org.jboss.errai.ioc.client.container.DestructionCallback;
import org.jboss.errai.ioc.rebind.ioc.extension.IOCDecoratorExtension;
import org.jboss.errai.ioc.rebind.ioc.injector.InjectUtil;
import org.jboss.errai.ioc.rebind.ioc.injector.api.InjectableInstance;
import org.jboss.errai.ioc.util.RunAsyncWrapper;

import static org.jboss.errai.codegen.meta.MetaClassFactory.parameterizedAs;
import static org.jboss.errai.codegen.meta.MetaClassFactory.typeParametersOf;

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
  public List<? extends Statement> generateDecorator(InjectableInstance<Observes> instance) {
    final Context ctx = instance.getInjectionContext().getProcessingContext().getContext();
    final MetaMethod method = instance.getMethod();
    final MetaParameter parm = instance.getParm();

    if (!method.isPublic()) {
      instance.ensureMemberExposed(PrivateAccessType.Write);
    }

    final String parmClassName = parm.getType().getFullyQualifiedName();
    final List<Annotation> annotations = InjectUtil.extractQualifiers(instance);
    final Annotation[] qualifiers = annotations.toArray(new Annotation[annotations.size()]);
    final List<String> qualifierNames = CDI.getQualifiersPart(qualifiers);

    AnonymousClassStructureBuilder callBack = Stmt.newObject(AbstractCDIEventCallback.class).extend();

    BlockBuilder<AnonymousClassStructureBuilder> callBackBlock;
    if (qualifierNames != null) {
      callBackBlock = callBack.initialize();
      for (String qualifierName : qualifierNames) {
        callBackBlock.append(Stmt.loadClassMember("qualifierSet").invoke("add", qualifierName));
      }
      callBack = callBackBlock.finish();
    }

    callBackBlock = callBack.publicOverridesMethod("callback", Parameter.of(Message.class, "message", true))
            ._(Stmt.declareVariable("msgQualifiers", new TypeLiteral<Set<String>>() {
            },
                    Stmt.loadVariable("message").invoke("get", Set.class, CDIProtocol.Qualifiers)))
            ._(Stmt
                    .if_(Bool.or(
                            Stmt.loadClassMember("qualifierSet").invoke("equals", Refs.get("msgQualifiers")),
                            Bool.and(Bool.equals(Refs.get("msgQualifiers"), null),
                                    Stmt.loadClassMember("qualifierSet").invoke("isEmpty"))))
                    ._(RunAsyncWrapper.wrap(instance.callOrBind(Stmt.loadVariable("message")
                            .invoke("get", parm.getType().asClass(), CDIProtocol.BeanReference))))
                    .finish());

    // create the destruction callback to deregister the service when the bean is destroyed.
    final String subscrVar = InjectUtil.getUniqueVarName();

    Statement subscribeStatement =
            Stmt.declareVariable(Subscription.class).asFinal().named(subscrVar)
                    .initializeWith(Stmt.create(ctx).invokeStatic(CDI.class, "subscribe", parmClassName, callBackBlock.finish().finish()));

    final MetaClass destructionCallbackType =
            parameterizedAs(DestructionCallback.class, typeParametersOf(instance.getEnclosingType()));

    final BlockBuilder<AnonymousClassStructureBuilder> destroyMeth
            = ObjectBuilder.newInstanceOf(destructionCallbackType).extend()
            .publicOverridesMethod("destroy", Parameter.of(instance.getEnclosingType(), "obj", true))
            .append(Stmt.loadVariable(subscrVar).invoke("remove"));

    Statement descrCallback = Stmt.create().loadVariable("context").invoke("addDestructionCallback",
            Refs.get(instance.getInjector().getVarName()), destroyMeth.finish().finish());

    return Arrays.asList(subscribeStatement, descrCallback);
  }
}