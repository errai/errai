/*
 * Copyright 2015 JBoss, by Red Hat, Inc
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

package org.jboss.errai.ioc.rebind.ioc.bootstrapper;

import static org.jboss.errai.codegen.meta.MetaClassFactory.parameterizedAs;
import static org.jboss.errai.codegen.meta.MetaClassFactory.typeParametersOf;
import static org.jboss.errai.codegen.util.Bool.instanceOf;
import static org.jboss.errai.codegen.util.PrivateAccessUtil.addPrivateAccessStubs;
import static org.jboss.errai.codegen.util.PrivateAccessUtil.getPrivateFieldAccessorName;
import static org.jboss.errai.codegen.util.PrivateAccessUtil.getPrivateMethodName;
import static org.jboss.errai.codegen.util.Stmt.castTo;
import static org.jboss.errai.codegen.util.Stmt.declareFinalVariable;
import static org.jboss.errai.codegen.util.Stmt.declareVariable;
import static org.jboss.errai.codegen.util.Stmt.if_;
import static org.jboss.errai.codegen.util.Stmt.loadVariable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.enterprise.inject.Disposes;

import org.jboss.errai.codegen.Statement;
import org.jboss.errai.codegen.builder.ClassStructureBuilder;
import org.jboss.errai.codegen.builder.ContextualStatementBuilder;
import org.jboss.errai.codegen.meta.MetaClassMember;
import org.jboss.errai.codegen.meta.MetaField;
import org.jboss.errai.codegen.meta.MetaMethod;
import org.jboss.errai.codegen.meta.MetaParameter;
import org.jboss.errai.codegen.meta.impl.build.BuildMetaClass;
import org.jboss.errai.codegen.util.PrivateAccessType;
import org.jboss.errai.ioc.client.container.Proxy;
import org.jboss.errai.ioc.rebind.ioc.graph.api.DependencyGraph;
import org.jboss.errai.ioc.rebind.ioc.graph.api.DependencyGraphBuilder.Dependency;
import org.jboss.errai.ioc.rebind.ioc.graph.api.DependencyGraphBuilder.DependencyType;
import org.jboss.errai.ioc.rebind.ioc.graph.api.DependencyGraphBuilder.DisposerMethodDependency;
import org.jboss.errai.ioc.rebind.ioc.graph.api.DependencyGraphBuilder.ParamDependency;
import org.jboss.errai.ioc.rebind.ioc.graph.api.DependencyGraphBuilder.ProducerInstanceDependency;
import org.jboss.errai.ioc.rebind.ioc.graph.api.Injectable;
import org.jboss.errai.ioc.rebind.ioc.injector.api.InjectionContext;
import org.jboss.errai.ioc.rebind.ioc.injector.api.WiringElementType;

import com.google.common.collect.Multimap;

/**
 * Generates factories for beans of producer methods or fields.
 *
 * @see FactoryBodyGenerator
 * @see AbstractBodyGenerator
 * @author Max Barkley <mbarkley@redhat.com>
 */
public class ProducerFactoryBodyGenerator extends AbstractBodyGenerator {

  @Override
  protected List<Statement> generateCreateInstanceStatements(final ClassStructureBuilder<?> bodyBlockBuilder,
          final Injectable injectable, final DependencyGraph graph, final InjectionContext injectionContext) {
    final Multimap<DependencyType, Dependency> depsByType = separateByType(injectable.getDependencies());
    if (depsByType.get(DependencyType.ProducerMember).size() != 1) {
      throw new RuntimeException("A produced type must have exactly 1 producing instance but "
              + depsByType.get(DependencyType.ProducerMember).size() + " were found.");
    }
    final ProducerInstanceDependency producerInstanceDep = (ProducerInstanceDependency) depsByType.get(DependencyType.ProducerMember).iterator().next();
    final Injectable producerInjectable = producerInstanceDep.getInjectable();
    final MetaClassMember producingMember = producerInstanceDep.getProducingMember();

    if (producingMember instanceof MetaField) {
      return fieldCreateInstanceStatements((MetaField) producingMember, producerInjectable, injectable, bodyBlockBuilder);
    } else if (producingMember instanceof MetaMethod) {
      return methodCreateInstanceStatements((MetaMethod) producingMember, producerInjectable, injectable,
              depsByType.get(DependencyType.ProducerParameter), bodyBlockBuilder);
    } else {
      throw new RuntimeException("Unrecognized producing member: " + producingMember);
    }
  }

  private List<Statement> methodCreateInstanceStatements(final MetaMethod producingMember, final Injectable producerInjectable,
          final Injectable producedInjectable, final Collection<Dependency> paramDeps, final ClassStructureBuilder<?> bodyBlockBuilder) {
    final List<Statement> stmts = new ArrayList<Statement>();
    addPrivateAccessStubs("jsni", bodyBlockBuilder, producingMember);

    if (!producingMember.isStatic()) {
      final Statement producerInstanceValue = loadVariable("contextManager").invoke("getInstance", producerInjectable.getFactoryName());
      stmts.add(declareVariable("producerInstance", producerInjectable.getInjectedType(), producerInstanceValue));

      // TODO figure out if proxied at compile time to simplify this code
      stmts.add(
              if_(instanceOf(
                      loadVariable(
                              "producerInstance"),
                      Proxy.class))._(loadVariable("producerInstance").assignValue(
                              castTo(parameterizedAs(Proxy.class, typeParametersOf(producerInjectable.getInjectedType())),
                                      loadVariable("producerInstance")).invoke("unwrappedInstance")))
              .finish());
    }

    stmts.add(declareFinalVariable("instance", producedInjectable.getInjectedType(), loadVariable("this")
            .invoke(getPrivateMethodName(producingMember), getProducerInvocationParams(producingMember, paramDeps))));
    if (!producingMember.isStatic()) {
      stmts.add(setProducerInstanceReference());
      if (producerInjectable.getWiringElementTypes().contains(WiringElementType.DependentBean)) {
        stmts.add(loadVariable("this").invoke("registerDependentScopedReference", loadVariable("instance"), loadVariable("producerInstance")));
      }
    }

    stmts.add(loadVariable("instance").returnValue());

    return stmts;
  }

  private Object[] getProducerInvocationParams(final MetaMethod producingMember, final Collection<Dependency> paramDeps) {
    // TODO validate params
    final int offset;
    final Object[] params;
    if (producingMember.isStatic()) {
      offset = 0;
      params = new Object[producingMember.getParameters().length];
    } else {
      offset = 1;
      params = new Object[producingMember.getParameters().length+1];
      params[0] = loadVariable("producerInstance");
    }

    for (final Dependency dep : paramDeps) {
      final ParamDependency paramDep = (ParamDependency) dep;
      params[paramDep.getParamIndex() + offset] = castTo(paramDep.getInjectable().getInjectedType(),
              loadVariable("contextManager").invoke("getInstance", paramDep.getInjectable().getFactoryName()));
    }

    return params;
  }

  private List<Statement> fieldCreateInstanceStatements(final MetaField producingMember, final Injectable producerInjectable,
          final Injectable producedInjectable, final ClassStructureBuilder<?> bodyBlockBuilder) {
    final List<Statement> stmts = new ArrayList<Statement>();
    addPrivateAccessStubs(PrivateAccessType.Read, "jsni", bodyBlockBuilder, producingMember);

    if (!producingMember.isStatic()) {
      final Statement producerInstanceValue = loadVariable("contextManager").invoke("getInstance", producerInjectable.getFactoryName());
      stmts.add(declareVariable("producerInstance", producerInjectable.getInjectedType(), producerInstanceValue));
      if (producerInjectable.getWiringElementTypes().contains(WiringElementType.DependentBean)) {
        stmts.add(loadVariable("this").invoke("registerDependentScopedReference", loadVariable("producerInstance")));
      }

      // TODO figure out if proxied at compile time to simplify this code
      stmts.add(
              if_(instanceOf(
                      loadVariable(
                              "producerInstance"),
                      Proxy.class))._(loadVariable("producerInstance").assignValue(
                              castTo(parameterizedAs(Proxy.class, typeParametersOf(producerInjectable.getInjectedType())),
                                      loadVariable("producerInstance")).invoke("unwrappedInstance")))
              .finish());
    }

    final Object[] params = (producingMember.isStatic()) ? new Object[0] : new Object[] { loadVariable("producerInstance") };
    stmts.add(declareFinalVariable("instance", producedInjectable.getInjectedType(),
            loadVariable("this").invoke(getPrivateFieldAccessorName(producingMember), params)));

    if (!producingMember.isStatic()) {
      stmts.add(setProducerInstanceReference());
    }

    stmts.add(loadVariable("instance").returnValue());

    return stmts;
  }

  private Statement setProducerInstanceReference() {
    return InjectUtil.constructSetReference("producerInstance", loadVariable("producerInstance"));
  }

  @Override
  protected List<Statement> generateDestroyInstanceStatements(final ClassStructureBuilder<?> bodyBlockBuilder,
          final Injectable injectable, final DependencyGraph graph, final InjectionContext injectionContext) {
    final List<Statement> destroyInstanceStmts = new ArrayList<Statement>();
    final Multimap<DependencyType, Dependency> depsByType = separateByType(injectable.getDependencies());
    final Collection<Dependency> producerMemberDeps = depsByType.get(DependencyType.ProducerMember);

    if (producerMemberDeps.size() != 1) {
      throw new RuntimeException("A produced type must have exactly 1 producing instance but "
              + producerMemberDeps.size() + " were found.");
    }

    final Collection<Dependency> disposerMethodDeps = depsByType.get(DependencyType.DisposerMethod);
    if (disposerMethodDeps.size() > 1) {
      // TODO error message with matching disposer names.
      throw new RuntimeException();
    } else if (disposerMethodDeps.size() == 1) {
      final DisposerMethodDependency disposerDep = (DisposerMethodDependency) disposerMethodDeps.iterator().next();
      final MetaMethod disposer = disposerDep.getDisposerMethod();

      addPrivateAccessStubs("jsni", bodyBlockBuilder, disposer);
      destroyInstanceStmts.add(loadVariable("this").invoke(getPrivateMethodName(disposer),
              getDisposerParams(disposer, depsByType.get(DependencyType.DisposerParameter), bodyBlockBuilder.getClassDefinition())));
    }

    return destroyInstanceStmts;
  }

  private Object[] getDisposerParams(final MetaMethod disposer, final Collection<Dependency> disposerParams, final BuildMetaClass factory) {
    final int offset;
    final Object[] params;
    if (disposer.isStatic()) {
      offset = 0;
      params = new Object[disposer.getParameters().length];
    } else {
      offset = 1;
      params = new Object[disposer.getParameters().length+1];
      params[0] = castTo(disposer.getDeclaringClass(), InjectUtil.constructGetReference("producerInstance", disposer.getDeclaringClass().asClass()));
    }

    for (final Dependency dep : disposerParams) {
      final ParamDependency paramDep = (ParamDependency) dep;
      final ContextualStatementBuilder paramInstance = castTo(paramDep.getInjectable().getInjectedType(),
              loadVariable("contextManager").invoke("getInstance", paramDep.getInjectable().getFactoryName()));
      final ContextualStatementBuilder paramExpression;
      if (paramDep.getInjectable().getWiringElementTypes().contains(WiringElementType.DependentBean)) {
        paramExpression = loadVariable("this").invoke("registerDependentScopedReference", loadVariable("instance"), paramInstance);
      } else {
        paramExpression = paramInstance;
      }

      params[paramDep.getParamIndex()+offset] = paramExpression;
    }

    final MetaParameter disposesParam = disposer.getParametersAnnotatedWith(Disposes.class).get(0);
    params[disposesParam.getIndex()+offset] = loadVariable("instance");

    return params;
  }

}
