/*
 * Copyright (C) 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
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
import static org.jboss.errai.codegen.util.Stmt.castTo;
import static org.jboss.errai.codegen.util.Stmt.loadVariable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Provider;

import org.jboss.errai.codegen.Statement;
import org.jboss.errai.codegen.builder.ClassStructureBuilder;
import org.jboss.errai.ioc.client.api.IOCProvider;
import org.jboss.errai.ioc.rebind.ioc.graph.api.DependencyGraph;
import org.jboss.errai.ioc.rebind.ioc.graph.api.Injectable;
import org.jboss.errai.ioc.rebind.ioc.graph.api.DependencyGraphBuilder.Dependency;
import org.jboss.errai.ioc.rebind.ioc.graph.api.DependencyGraphBuilder.DependencyType;
import org.jboss.errai.ioc.rebind.ioc.injector.api.InjectionContext;

import com.google.common.collect.Multimap;

/**
 * Create factories for beans from {@link Provider providers} annotated with
 * {@link IOCProvider}.
 *
 * @see FactoryBodyGenerator
 * @see AbstractBodyGenerator
 * @author Max Barkley <mbarkley@redhat.com>
 */
public class ProviderFactoryBodyGenerator extends AbstractBodyGenerator {

  @Override
  protected List<Statement> generateCreateInstanceStatements(final ClassStructureBuilder<?> bodyBlockBuilder,
          final Injectable injectable, final DependencyGraph graph, InjectionContext injectionContext) {
    final Multimap<DependencyType, Dependency> dependenciesByType = separateByType(injectable.getDependencies());
    assert dependenciesByType.size() == 1 : "The factory " + injectable.getFactoryName() + " is a Provider and should have exactly one dependency";
    final Collection<Dependency> providerInstanceDeps = dependenciesByType.get(DependencyType.ProducerMember);
    assert providerInstanceDeps.size() == 1 : "The factory " + injectable.getFactoryName()
            + " is a Provider but does not have a " + DependencyType.ProducerMember.toString() + " depenency.";

    final Dependency providerDep = providerInstanceDeps.iterator().next();
    final List<Statement> createInstanceStatements = getAndInvokeProvider(injectable, providerDep);

    return createInstanceStatements;
  }

  private List<Statement> getAndInvokeProvider(final Injectable injectable, final Dependency providerDep) {
    final Injectable providerInjectable = providerDep.getInjectable();
    final List<Statement> statement = new ArrayList<Statement>(1);

    statement.add(castTo(parameterizedAs(Provider.class, typeParametersOf(injectable.getInjectedType())),
            loadVariable("contextManager").invoke("getInstance", providerInjectable.getFactoryName()))
                    .invoke("get").returnValue());

    return statement;
  }

}
