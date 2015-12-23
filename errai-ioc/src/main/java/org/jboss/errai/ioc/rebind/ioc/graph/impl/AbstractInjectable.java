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

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.ioc.rebind.ioc.graph.api.Qualifier;
import org.jboss.errai.ioc.rebind.ioc.graph.api.DependencyGraphBuilder.Dependency;
import org.jboss.errai.ioc.rebind.ioc.graph.api.DependencyGraphBuilder.InjectableType;
import org.jboss.errai.ioc.rebind.ioc.graph.api.Injectable;
import org.jboss.errai.ioc.rebind.ioc.injector.api.WiringElementType;

/**
 * This is used in the {@link DependencyGraphBuilderImpl} for representing
 * injectables in dependencies that have not yet been resolved.
 *
 * @author Max Barkley <mbarkley@redhat.com>
 */
class AbstractInjectable extends BaseInjectable {
  // TODO needs to be renamed and not be an Injectable

  final Collection<BaseInjectable> linked = new HashSet<BaseInjectable>();
  Injectable resolution;

  AbstractInjectable(final MetaClass type, final Qualifier qualifier) {
    super(type, qualifier, null);
  }

  @Override
  public Class<? extends Annotation> getScope() {
    return null;
  }

  @Override
  public InjectableType getInjectableType() {
    return InjectableType.Abstract;
  }

  @Override
  public Collection<Dependency> getDependencies() {
    if (resolution == null) {
      return Collections.emptyList();
    } else {
      return resolution.getDependencies();
    }
  }

  @Override
  public boolean requiresProxy() {
    if (resolution == null) {
      return false;
    } else {
      return resolution.requiresProxy();
    }
  }

  @Override
  public void setRequiresProxyTrue() {
    throw new RuntimeException("Should not be callled on " + AbstractInjectable.class.getSimpleName());
  }

  @Override
  public Collection<WiringElementType> getWiringElementTypes() {
    return Collections.emptyList();
  }

  @Override
  public boolean isContextual() {
    return resolution != null && resolution.isContextual();
  }

  @Override
  public boolean isExtension() {
    return false;
  }

  @Override
  public String getFactoryName() {
    throw new RuntimeException("Abstract injectables to not have a factory name.");
  }

  @Override
  public int hashContent() {
    throw new RuntimeException("This method should only be called for concrete injectables.");
  }

  @Override
  public boolean loadAsync() {
    throw new RuntimeException("This method should only be called for concrete injectables.");
  }
}
