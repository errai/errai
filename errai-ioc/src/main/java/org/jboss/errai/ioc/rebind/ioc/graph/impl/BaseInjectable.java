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

package org.jboss.errai.ioc.rebind.ioc.graph.impl;

import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.ioc.rebind.ioc.graph.api.Injectable;
import org.jboss.errai.ioc.rebind.ioc.graph.api.Qualifier;

/**
 * Common base class for {@link ConcreteInjectable} and
 * {@link AbstractInjectable} so that they can both be stored as links in
 * abstract injectables.
 *
 * @author Max Barkley <mbarkley@redhat.com>
 */
abstract class BaseInjectable implements Injectable {

  final MetaClass type;
  Qualifier qualifier;
  final String factoryName;

  BaseInjectable(final MetaClass type, final Qualifier qualifier, final String factoryName) {
    this.type = type;
    this.qualifier = qualifier;
    this.factoryName = factoryName;
  }

  @Override
  public String getBeanName() {
    return qualifier.getName();
  }

  @Override
  public MetaClass getInjectedType() {
    return type;
  }

  @Override
  public String toString() {
    return "[class=" + getInjectedType() + ", injectorType=" + getInjectableType() + ", qualifier="
            + getQualifier().toString() + "]";
  }

  @Override
  public Qualifier getQualifier() {
    return qualifier;
  }

  @Override
  public String getFactoryName() {
    return factoryName;
  }

  @Override
  public InjectableHandle getHandle() {
    return new InjectableHandle(type, qualifier);
  }
}
