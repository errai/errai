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

package org.jboss.errai.ioc.client.container;

import java.lang.annotation.Annotation;

import javax.enterprise.context.Dependent;

import org.jboss.errai.ioc.client.api.ScopeContext;

/**
 * The {@link Context} implementation for all {@link Dependent} scoped beans.
 * Unlike other scopes, beans which have no explicit scope will be considered
 * dependent. Therefore some beans will be registered with this scope that do
 * not actually have the {@link Dependent} annotation.
 *
 * @author Max Barkley <mbarkley@redhat.com>
 */
@ScopeContext({Dependent.class})
public class DependentScopeContext extends AbstractContext {

  @Override
  public <T> T getInstance(final String factoryName) {
    final Factory<T> factory = this.<T>getFactory(factoryName);
    final Proxy<T> proxy = factory.createProxy(this);
    final T instance;
    if (proxy == null) {
      instance = getActiveNonProxiedInstance(factoryName);
    } else {
      instance = proxy.asBeanType();
    }
    return instance;
  }

  @Override
  public <T> T getActiveNonProxiedInstance(final String factoryName) {
    final Factory<T> factory = this.<T>getFactory(factoryName);
    final T instance = factory.createInstance(getContextManager());
    registerInstance(instance, factory);
    factory.invokePostConstructs(instance);

    return instance;
  }

  @Override
  public Class<? extends Annotation> getScope() {
    return Dependent.class;
  }

  @Override
  public boolean isActive() {
    return true;
  }

  @Override
  public boolean handlesScope(Class<? extends Annotation> scope) {
    return Dependent.class.equals(scope);
  }

}
