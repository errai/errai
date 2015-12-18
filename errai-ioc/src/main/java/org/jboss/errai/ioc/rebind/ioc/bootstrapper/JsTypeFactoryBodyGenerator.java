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

import static org.jboss.errai.codegen.util.Stmt.invokeStatic;

import java.util.Collections;
import java.util.List;

import org.jboss.errai.codegen.Statement;
import org.jboss.errai.codegen.builder.ClassStructureBuilder;
import org.jboss.errai.codegen.util.Stmt;
import org.jboss.errai.ioc.client.WindowInjectionContext;
import org.jboss.errai.ioc.rebind.ioc.graph.api.DependencyGraph;
import org.jboss.errai.ioc.rebind.ioc.graph.api.Injectable;
import org.jboss.errai.ioc.rebind.ioc.injector.api.InjectionContext;

/**
 * Generates factories that lookup types from the {@link WindowInjectionContext}
 * , allowing the injection of types between dynamic runtime modules.
 *
 * @see FactoryBodyGenerator
 * @see AbstractBodyGenerator
 * @author Max Barkley <mbarkley@redhat.com>
 */
public class JsTypeFactoryBodyGenerator extends AbstractBodyGenerator {

  @Override
  protected List<Statement> generateCreateInstanceStatements(ClassStructureBuilder<?> bodyBlockBuilder,
          Injectable injectable, DependencyGraph graph, InjectionContext injectionContext) {
    return Collections.<Statement> singletonList(
            Stmt.castTo(injectable.getInjectedType(), invokeStatic(WindowInjectionContext.class, "createOrGet")
                    .invoke("getBean", injectable.getInjectedType().getFullyQualifiedName())).returnValue());
  }

}
