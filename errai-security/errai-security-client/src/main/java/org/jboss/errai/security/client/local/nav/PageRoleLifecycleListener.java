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

package org.jboss.errai.security.client.local.nav;

import java.util.Set;

import org.jboss.errai.ioc.client.container.Factory;
import org.jboss.errai.ioc.client.lifecycle.api.Access;
import org.jboss.errai.ioc.client.lifecycle.api.LifecycleEvent;
import org.jboss.errai.ioc.client.lifecycle.api.LifecycleListener;
import org.jboss.errai.security.client.local.api.SecurityContext;
import org.jboss.errai.security.shared.api.Role;
import org.jboss.errai.security.shared.api.annotation.RestrictedAccess;
import org.jboss.errai.security.shared.spi.RequiredRolesExtractor;

import com.google.gwt.user.client.ui.IsWidget;

/**
 * Listens for page navigation events and redirects if the logged in user lacks
 * sufficient roles.
 *
 * @author Max Barkley <mbarkley@redhat.com>
 */
public class PageRoleLifecycleListener<W extends IsWidget> implements LifecycleListener<W> {

  private final RestrictedAccess annotation;
  private final RequiredRolesExtractor roleExtractor;

  public PageRoleLifecycleListener(final RestrictedAccess annotation, final RequiredRolesExtractor roleExtractor) {
    this.annotation = annotation;
    this.roleExtractor = roleExtractor;
  }

  @Override
  public void observeEvent(final LifecycleEvent<W> event) {
    // There is no good way to inject the context within the bootstrapper.
    final SecurityContext securityContext = SecurityContextHoldingSingleton.getSecurityContext();
    final Set<Role> roles = roleExtractor.extractAllRoles(annotation);

    if (!securityContext.isUserCacheValid() || !securityContext.hasCachedUser()
            || !securityContext.getCachedUser().getRoles().containsAll(roles)) {
      event.veto();

      if (!securityContext.hasCachedUser())
        securityContext.redirectToLoginPage(Factory.maybeUnwrapProxy(event.getInstance()).getClass());
      else
        securityContext.redirectToSecurityErrorPage(Factory.maybeUnwrapProxy(event.getInstance()).getClass());
    }
  }

  @Override
  public boolean isObserveableEventType(final Class<? extends LifecycleEvent<W>> eventType) {
    return eventType.equals(Access.class);
  }

}
