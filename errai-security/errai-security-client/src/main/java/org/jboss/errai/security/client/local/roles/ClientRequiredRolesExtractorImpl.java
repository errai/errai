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

package org.jboss.errai.security.client.local.roles;

import java.util.HashSet;
import java.util.Set;

import javax.enterprise.context.Dependent;

import org.jboss.errai.common.client.util.CreationalCallback;
import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.ioc.client.container.RefHolder;
import org.jboss.errai.ioc.client.container.async.AsyncBeanDef;
import org.jboss.errai.security.shared.api.RequiredRolesProvider;
import org.jboss.errai.security.shared.roles.SharedRequiredRolesExtractorImpl;

/**
 * Implements client-specific code for creating and destroying {@link RequiredRolesProvider} instances.
 *
 * @author Max Barkley <mbarkley@redhat.com>
 */
@Dependent
public class ClientRequiredRolesExtractorImpl extends SharedRequiredRolesExtractorImpl {

  private final Set<RequiredRolesProvider> dependentProviders = new HashSet<RequiredRolesProvider>();

  @Override
  @SuppressWarnings("unchecked")
  protected RequiredRolesProvider getProviderInstance(final Class<? extends RequiredRolesProvider> providerType) {
    final RefHolder<RequiredRolesProvider> holder = new RefHolder<RequiredRolesProvider>();
    final AsyncBeanDef<RequiredRolesProvider> beanDef = IOC.getAsyncBeanManager().lookupBean(
            (Class<RequiredRolesProvider>) providerType);

    beanDef.getInstance(new CreationalCallback<RequiredRolesProvider>() {

              @Override
              public void callback(final RequiredRolesProvider instance) {
                holder.set(instance);
              }
            });

    if (holder.get() == null)
      throw new RuntimeException("Can't load RequiredRoleProviders asynchronously.");

    if (beanDef.getScope().equals(Dependent.class)) {
      dependentProviders.add(holder.get());
    }

    return holder.get();
  }

  @Override
  protected void destroyProviderInstance(final RequiredRolesProvider instance) {
    if (dependentProviders.contains(instance)) {
      dependentProviders.remove(instance);
      IOC.getAsyncBeanManager().destroyBean(instance);
    }
  }
}
