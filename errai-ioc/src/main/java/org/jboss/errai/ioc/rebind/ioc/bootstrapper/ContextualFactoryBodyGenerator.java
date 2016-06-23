/**
 * Copyright (C) 2016 Red Hat, Inc. and/or its affiliates.
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

import static java.util.Collections.singletonList;
import static org.jboss.errai.codegen.util.Stmt.castTo;
import static org.jboss.errai.codegen.util.Stmt.loadLiteral;
import static org.jboss.errai.codegen.util.Stmt.loadVariable;

import java.util.List;

import org.jboss.errai.codegen.Statement;
import org.jboss.errai.codegen.builder.ClassStructureBuilder;
import org.jboss.errai.codegen.builder.ContextualStatementBuilder;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.ioc.client.api.ContextualTypeProvider;
import org.jboss.errai.ioc.rebind.ioc.graph.api.DependencyGraph;
import org.jboss.errai.ioc.rebind.ioc.graph.api.DependencyGraphBuilder.Dependency;
import org.jboss.errai.ioc.rebind.ioc.graph.api.DependencyGraphBuilder.DependencyType;
import org.jboss.errai.ioc.rebind.ioc.graph.api.Injectable;
import org.jboss.errai.ioc.rebind.ioc.injector.api.InjectionContext;

/**
 * Generate factories for contextual bean instances provided by a {@link ContextualTypeProvider}.
 *
 * @see FactoryBodyGenerator
 * @see AbstractBodyGenerator
 * @author Max Barkley <mbarkley@redhat.com>
 */
public class ContextualFactoryBodyGenerator extends AbstractBodyGenerator {

  @Override
  protected List<Statement> generateCreateInstanceStatements(final ClassStructureBuilder<?> bodyBlockBuilder,
          final Injectable injectable, final DependencyGraph graph, final InjectionContext injectionContext) {
    return singletonList(invokeContextualProvider(injectable).returnValue());
  }

  private ContextualStatementBuilder invokeContextualProvider(final Injectable depInjectable) {
    final Injectable providerInjectable = getProviderInjectable(depInjectable);
    final MetaClass providerType = providerInjectable.getInjectedType();
    if (providerType.isAssignableTo(ContextualTypeProvider.class)) {
      return invokeProviderStmt(providerInjectable, providerType);
    }
    else {
      throw new RuntimeException("Unrecognized contextual provider type " + providerType.getFullyQualifiedName());
    }
  }

  private ContextualStatementBuilder invokeProviderStmt(final Injectable providerInjectable, final MetaClass providerType) {
    final ContextualStatementBuilder injectedValue;
    injectedValue = castTo(providerType,
            loadVariable("contextManager").invoke("getInstance", loadLiteral(providerInjectable.getFactoryName())))
                    .invoke("provide", loadVariable("typeArgs"), loadVariable("qualifiers"));
    return injectedValue;
  }

  private Injectable getProviderInjectable(final Injectable depInjectable) {
    for (final Dependency dep : depInjectable.getDependencies()) {
      if (dep.getDependencyType().equals(DependencyType.ProducerMember)) {
        return dep.getInjectable();
      }
    }

    throw new RuntimeException();
  }

}
