/*
 * Copyright (C) 2014 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.security.server;

import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.Dependent;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;

import org.jboss.errai.security.shared.api.RequiredRolesProvider;
import org.jboss.errai.security.shared.roles.SharedRequiredRolesExtractorImpl;

/**
 * Implements server-specific code for creating and destroying {@link RequiredRolesProvider} instances.
 *
 * @author Max Barkley <mbarkley@redhat.com>
 */
@Dependent
public class ServerRequiredRolesExtractorImpl extends SharedRequiredRolesExtractorImpl {

  @Inject
  private BeanManager beanManager;

  private final Map<RequiredRolesProvider, BeanDef> dependentProviders = new HashMap<RequiredRolesProvider, BeanDef>();

  @Override
  @SuppressWarnings("unchecked")
  protected RequiredRolesProvider getProviderInstance(final Class<? extends RequiredRolesProvider> providerType) {
    final Bean<?> resolvedBean = beanManager.resolve(beanManager.getBeans(providerType));

    final CreationalContext<?> creationalContext = beanManager.createCreationalContext(resolvedBean);
    RequiredRolesProvider provider = (RequiredRolesProvider) beanManager.getReference(resolvedBean, providerType,
            creationalContext);

    if (resolvedBean.getScope().equals(Dependent.class)) {
      dependentProviders.put(provider, new BeanDef((Bean<RequiredRolesProvider>) resolvedBean,
              (CreationalContext<RequiredRolesProvider>) creationalContext));
    }

    return provider;
  }

  @Override
  protected void destroyProviderInstance(final RequiredRolesProvider instance) {
    if (dependentProviders.containsKey(instance)) {
      final BeanDef beanDef = dependentProviders.remove(instance);
      beanDef.bean.destroy(instance, beanDef.creationalContext);
    }
  }

  private static class BeanDef {
    final Bean<RequiredRolesProvider> bean;
    final CreationalContext<RequiredRolesProvider> creationalContext;

    BeanDef(final Bean<RequiredRolesProvider> bean, final CreationalContext<RequiredRolesProvider> creationalContext) {
      this.bean = bean;
      this.creationalContext = creationalContext;
    }
  }

}
