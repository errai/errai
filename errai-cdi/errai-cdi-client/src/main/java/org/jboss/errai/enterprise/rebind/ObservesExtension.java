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

import static org.jboss.errai.codegen.meta.MetaClassFactory.parameterizedAs;
import static org.jboss.errai.codegen.meta.MetaClassFactory.typeParametersOf;

import org.jboss.errai.bus.client.ErraiBus;
import org.jboss.errai.bus.client.api.Subscription;
import org.jboss.errai.codegen.Context;
import org.jboss.errai.codegen.Parameter;
import org.jboss.errai.codegen.Statement;
import org.jboss.errai.codegen.builder.AnonymousClassStructureBuilder;
import org.jboss.errai.codegen.builder.BlockBuilder;
import org.jboss.errai.codegen.builder.impl.ObjectBuilder;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.MetaMethod;
import org.jboss.errai.codegen.meta.MetaParameter;
import org.jboss.errai.codegen.util.PrivateAccessType;
import org.jboss.errai.codegen.util.Refs;
import org.jboss.errai.codegen.util.Stmt;
import org.jboss.errai.config.rebind.EnvUtil;
import org.jboss.errai.enterprise.client.cdi.AbstractCDIEventCallback;
import org.jboss.errai.enterprise.client.cdi.api.CDI;
import org.jboss.errai.ioc.client.api.CodeDecorator;
import org.jboss.errai.ioc.client.container.DestructionCallback;
import org.jboss.errai.ioc.rebind.ioc.extension.IOCDecoratorExtension;
import org.jboss.errai.ioc.rebind.ioc.injector.InjectUtil;
import org.jboss.errai.ioc.rebind.ioc.injector.api.InjectableInstance;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.Any;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
  public ObservesExtension(final Class<Observes> decoratesWith) {
    super(decoratesWith);
  }

  @Override
  public List<? extends Statement> generateDecorator(final InjectableInstance<Observes> instance) {
    final Context ctx = instance.getInjectionContext().getProcessingContext().getContext();
    final MetaMethod method = instance.getMethod();
    final MetaParameter parm = instance.getParm();

    if (!method.isPublic()) {
      instance.ensureMemberExposed(PrivateAccessType.Write);
    }

    final String parmClassName = parm.getType().getFullyQualifiedName();
    final List<Annotation> annotations = InjectUtil.extractQualifiers(instance);
    final Annotation[] qualifiers = annotations.toArray(new Annotation[annotations.size()]);
    final Set<String> qualifierNames = new HashSet<String>(CDI.getQualifiersPart(qualifiers));

    if (qualifierNames.contains(Any.class.getName())) {
      qualifierNames.remove(Any.class.getName());
    }

    final MetaClass callBackType = parameterizedAs(AbstractCDIEventCallback.class, typeParametersOf(parm.getType()));

    AnonymousClassStructureBuilder callBack = Stmt.newObject(callBackType).extend();

    BlockBuilder<AnonymousClassStructureBuilder> callBackBlock;

    if (!qualifierNames.isEmpty()) {
      callBackBlock = callBack.initialize();
      for (final String qualifierName : qualifierNames) {
        callBackBlock.append(Stmt.loadClassMember("qualifierSet").invoke("add", qualifierName));
      }
      callBack = callBackBlock.finish();
    }

    callBackBlock = callBack.publicOverridesMethod("fireEvent", Parameter.finalOf(parm, "event"))
        ._(instance.callOrBind(Refs.get("event")))
        .finish()
        .publicOverridesMethod("toString")
        ._(Stmt.load("Observer: " + parmClassName + " " + Arrays.toString(qualifiers)).returnValue());

    final List<Statement> statements = new ArrayList<Statement>();

    // create the destruction callback to deregister the service when the bean is destroyed.
    final String subscrVar = InjectUtil.getUniqueVarName();

    final String subscribeMethod;
    if (EnvUtil.isPortableType(parm.getType().asClass()) && !EnvUtil.isLocalEventType(parm.getType().asClass())) {
      subscribeMethod = "subscribe";
    }
    else {
      subscribeMethod = "subscribeLocal";
    }

    final Statement subscribeStatement =
        Stmt.declareVariable(Subscription.class).asFinal().named(subscrVar)
            .initializeWith(Stmt.create(ctx).invokeStatic(CDI.class, subscribeMethod, parmClassName,
                callBackBlock.finish().finish()));

    statements.add(subscribeStatement);

    // create the destruction callback to deregister the service when the bean is destroyed.

    final MetaClass destructionCallbackType =
        parameterizedAs(DestructionCallback.class, typeParametersOf(instance.getEnclosingType()));

    final BlockBuilder<AnonymousClassStructureBuilder> destroyMeth
        = ObjectBuilder.newInstanceOf(destructionCallbackType).extend()
        .publicOverridesMethod("destroy", Parameter.finalOf(instance.getEnclosingType(), "obj"))
        .append(Stmt.loadVariable(subscrVar).invoke("remove")).append(Stmt.codeComment("WEEEEE!"));

    for (final Class<?> cls : EnvUtil.getAllPortableConcreteSubtypes(parm.getType().asClass())) {
      if (!EnvUtil.isLocalEventType(cls)) {
        final String subscrHandle = InjectUtil.getUniqueVarName();
        statements.add(Stmt.declareVariable(Subscription.class).asFinal().named(subscrHandle)
            .initializeWith(Stmt.invokeStatic(ErraiBus.class, "get").invoke("subscribe",
                CDI.getSubjectNameByType(cls.getName()),
                Stmt.loadStatic(CDI.class, "ROUTING_CALLBACK"))));
        destroyMeth.append(Stmt.loadVariable(subscrHandle).invoke("remove"));
      }
    }

    final Statement destructionCallback = Stmt.create().loadVariable("context").invoke("addDestructionCallback",
        Refs.get(instance.getInjector().getInstanceVarName()), destroyMeth.finish().finish());

    statements.add(destructionCallback);

    return statements;
  }
}