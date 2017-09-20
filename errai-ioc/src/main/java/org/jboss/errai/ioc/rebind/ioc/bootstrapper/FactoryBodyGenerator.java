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

import org.jboss.errai.codegen.builder.ClassStructureBuilder;
import org.jboss.errai.ioc.client.container.Factory;
import org.jboss.errai.ioc.rebind.ioc.graph.api.Injectable;
import org.jboss.errai.ioc.rebind.ioc.injector.api.InjectionContext;

/**
 * Used by the {@link FactoryGenerator} for generating the body of
 * {@link Factory} subclasses.
 *
 * @see AbstractBodyGenerator
 * @see TypeFactoryBodyGenerator
 * @author Max Barkley <mbarkley@redhat.com>
 */
public interface FactoryBodyGenerator {

  /**
   * Generates a {@link Factory} subclasses body into the given
   * {@link ClassStructureBuilder}. Must implement all the abstract methods of
   * {@link Factory}.
   * @param bodyBlockBuilder
   *          The {@link ClassStructureBuilder} for the {@link Factory} being
   *          generated.
   * @param injectable
   *          The {@link Injectable} for the bean of the {@link Factory} being
   *          generated.
   * @param injectionContext
*          The single {@link InjectionContext} shared by all
*          {@link FactoryBodyGenerator FactoryBodyGenerators}.
   */
  void generate(final ClassStructureBuilder<?> bodyBlockBuilder, final Injectable injectable, final InjectionContext injectionContext);
}
