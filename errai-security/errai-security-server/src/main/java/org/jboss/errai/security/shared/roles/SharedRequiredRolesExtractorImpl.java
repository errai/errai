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

package org.jboss.errai.security.shared.roles;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.jboss.errai.security.shared.api.RequiredRolesProvider;
import org.jboss.errai.security.shared.api.Role;
import org.jboss.errai.security.shared.api.RoleImpl;
import org.jboss.errai.security.shared.api.annotation.RestrictedAccess;
import org.jboss.errai.security.shared.spi.RequiredRolesExtractor;

/**
 * Implements basic role extracting functionality common to client and server implementations of
 * {@link RequiredRolesExtractor}.
 *
 * @author Max Barkley <mbarkley@redhat.com>
 */
public abstract class SharedRequiredRolesExtractorImpl implements RequiredRolesExtractor {

  @Override
  public Set<Role> extractAllRoles(final RestrictedAccess annotation) {
    final Set<Role> simpleRoles = extractSimpleRoles(annotation);
    final Set<Role> providedRoles = extractProvidedRoles(annotation);

    final Set<Role> allRoles = new HashSet<Role>(simpleRoles.size() + providedRoles.size());
    allRoles.addAll(simpleRoles);
    allRoles.addAll(providedRoles);

    return allRoles;
  }

  @Override
  public Set<Role> extractSimpleRoles(final RestrictedAccess annotation) {
    final Set<Role> simpleRoles = new HashSet<Role>(annotation.roles().length);

    for (final String roleName : annotation.roles()) {
      //XXX Maybe we should cache and reuse RoleImpls?
      simpleRoles.add(new RoleImpl(roleName));
    }

    return simpleRoles;
  }

  @Override
  public Set<Role> extractProvidedRoles(final RestrictedAccess annotation) {
    if (annotation.providers().length == 0) {
      return Collections.<Role>emptySet();
    }
    else {
      final RequiredRolesProvider[] providers = getProviderInstances(annotation.providers());
      final Set<Role> providedRoles = new HashSet<Role>();

      for (final RequiredRolesProvider provider : providers) {
        providedRoles.addAll(provider.getRoles());
        destroyProviderInstance(provider);
      }

      return providedRoles;
    }
  }

  private RequiredRolesProvider[] getProviderInstances(final Class<? extends RequiredRolesProvider>[] providerTypes) {
    final RequiredRolesProvider[] providerInstances = new RequiredRolesProvider[providerTypes.length];

    for (int i = 0; i < providerTypes.length; i++) {
      providerInstances[i] = getProviderInstance(providerTypes[i]);
    }

    return providerInstances;
  }

  protected abstract RequiredRolesProvider getProviderInstance(Class<? extends RequiredRolesProvider> providerType);

  protected abstract void destroyProviderInstance(RequiredRolesProvider instance);
}
