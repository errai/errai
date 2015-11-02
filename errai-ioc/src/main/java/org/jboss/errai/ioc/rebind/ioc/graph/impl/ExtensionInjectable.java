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

package org.jboss.errai.ioc.rebind.ioc.graph.impl;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.ioc.rebind.ioc.graph.api.Qualifier;
import org.jboss.errai.ioc.rebind.ioc.graph.api.DependencyGraphBuilder;
import org.jboss.errai.ioc.rebind.ioc.graph.api.DependencyGraphBuilder.InjectableType;
import org.jboss.errai.ioc.rebind.ioc.graph.api.ProvidedInjectable.InjectionSite;
import org.jboss.errai.ioc.rebind.ioc.injector.api.WiringElementType;

/**
 * @see DependencyGraphBuilder#addExtensionInjectable(MetaClass, Qualifier, Class, WiringElementType...)
 * @author Max Barkley <mbarkley@redhat.com>
 */
class ExtensionInjectable extends ConcreteInjectable {

  final Collection<InjectionSite> injectionSites = new ArrayList<InjectionSite>();

  ExtensionInjectable(final MetaClass type, final Qualifier qualifier, final String factoryName,
          final Class<? extends Annotation> literalScope, final InjectableType injectorType,
          final Collection<WiringElementType> wiringTypes) {
    super(type, qualifier, factoryName, literalScope, injectorType, wiringTypes);
  }

  public Collection<InjectionSite> getInjectionSites() {
    return Collections.unmodifiableCollection(injectionSites);
  }

  public String getFactoryNameForInjectionSite(final InjectionSite site) {
    return getFactoryName() + "__for__" + site.getUniqueName();
  }

  @Override
  public boolean isExtension() {
    return true;
  }

}