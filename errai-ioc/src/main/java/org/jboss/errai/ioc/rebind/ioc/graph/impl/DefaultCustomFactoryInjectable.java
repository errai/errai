/**
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

package org.jboss.errai.ioc.rebind.ioc.graph.impl;

import java.lang.annotation.Annotation;
import java.util.Collection;

import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.ioc.rebind.ioc.bootstrapper.FactoryBodyGenerator;
import org.jboss.errai.ioc.rebind.ioc.bootstrapper.IOCProcessor;
import org.jboss.errai.ioc.rebind.ioc.graph.api.CustomFactoryInjectable;
import org.jboss.errai.ioc.rebind.ioc.graph.api.DependencyGraphBuilder.InjectableType;
import org.jboss.errai.ioc.rebind.ioc.graph.api.Qualifier;
import org.jboss.errai.ioc.rebind.ioc.injector.api.WiringElementType;

/**
 * @author Max Barkley <mbarkley@redhat.com>
 */
public class DefaultCustomFactoryInjectable extends InjectableImpl implements CustomFactoryInjectable {

  private final FactoryBodyGenerator generator;

  public DefaultCustomFactoryInjectable(final MetaClass type, final Qualifier qualifier, final String factoryName,
          final Class<? extends Annotation> literalScope, final Collection<WiringElementType> wiringTypes,
          final FactoryBodyGenerator generator) {
    super(type, qualifier, IOCProcessor.ANY, factoryName, literalScope, InjectableType.ExtensionProvided, wiringTypes);
    this.generator = generator;
  }

  public DefaultCustomFactoryInjectable(final InjectableHandle handle, final String factoryName,
          final Class<? extends Annotation> literalScope, final Collection<WiringElementType> wiringTypes,
          final FactoryBodyGenerator generator) {
    this(handle.getType(), handle.getQualifier(), factoryName, literalScope, wiringTypes, generator);
  }


  @Override
  public FactoryBodyGenerator getGenerator() {
    return generator;
  }

}
