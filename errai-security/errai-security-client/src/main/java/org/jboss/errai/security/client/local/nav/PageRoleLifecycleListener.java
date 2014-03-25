/**
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.errai.security.client.local.nav;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.jboss.errai.ioc.client.lifecycle.api.Access;
import org.jboss.errai.ioc.client.lifecycle.api.LifecycleEvent;
import org.jboss.errai.ioc.client.lifecycle.api.LifecycleListener;
import org.jboss.errai.security.client.local.context.ActiveUserCache;
import org.jboss.errai.security.client.local.context.SecurityContext;
import org.jboss.errai.security.shared.api.identity.Role;
import org.jboss.errai.ui.nav.client.local.UniquePageRole;
import org.jboss.errai.ui.nav.client.local.api.LoginPage;
import org.jboss.errai.ui.nav.client.local.api.SecurityError;

import com.google.gwt.user.client.ui.IsWidget;

/**
 * Listens for page navigation events and redirects if the logged in user lacks
 * sufficient roles.
 * 
 * @author Max Barkley <mbarkley@redhat.com>
 */
public class PageRoleLifecycleListener<W extends IsWidget> implements LifecycleListener<W> {
  
  private Set<String> roles;
  
  public PageRoleLifecycleListener(final String... roles) {
    this.roles = new HashSet<String>();

    for (int i = 0; i < roles.length; i++) {
      this.roles.add(roles[i]);
    }
  }

  @Override
  public void observeEvent(final LifecycleEvent<W> event) {
    // There is no good way to inject the context within the bootstrapper.
    final SecurityContext securityContext = SecurityContextHoldingSingleton.getSecurityContext();
    final ActiveUserCache userCache = securityContext.getActiveUserCache();

    if (!userCache.isValid() || !userCache.hasUser()
            || !containsRoles(userCache.getUser().getRoles(), roles)) {
      event.veto();

      final Class<? extends UniquePageRole> destination;
      if (!userCache.hasUser())
        destination = LoginPage.class;
      else
        destination = SecurityError.class;
      
      securityContext.navigateToPage(destination);
    }
  }

  @Override
  public boolean isObserveableEventType(final Class<? extends LifecycleEvent<W>> eventType) {
    return eventType.equals(Access.class);
  }
  
  private boolean containsRoles(final Collection<Role> userRoles, final Set<String> requiredRoles) {
    if (userRoles == null && requiredRoles != null && !requiredRoles.isEmpty())
      return false;

    final Set<String> userRolesByName = new HashSet<String>();
    for (final Role role : userRoles)
      userRolesByName.add(role.getName());
    
    for (final String role : requiredRoles) {
      if (!userRolesByName.contains(role))
        return false;
    }
    
    return true;
  }

}
