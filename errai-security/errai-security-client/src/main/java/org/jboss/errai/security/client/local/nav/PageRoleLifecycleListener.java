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

import org.jboss.errai.ioc.client.lifecycle.api.Access;
import org.jboss.errai.ioc.client.lifecycle.api.LifecycleEvent;
import org.jboss.errai.ioc.client.lifecycle.api.LifecycleListener;
import org.jboss.errai.security.client.local.api.SecurityContext;
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

  private final String[] roles;

  public PageRoleLifecycleListener(final String... rolesRequiredByPage) {
    this.roles = rolesRequiredByPage;
  }

  @Override
  public void observeEvent(final LifecycleEvent<W> event) {
    // There is no good way to inject the context within the bootstrapper.
    final SecurityContext securityContext = SecurityContextHoldingSingleton.getSecurityContext();

    if (!securityContext.isUserCacheValid() || !securityContext.hasCachedUser()
            || !securityContext.getCachedUser().hasAllRoles(roles)) {
      event.veto();

      final Class<? extends UniquePageRole> destination;
      if (!securityContext.hasCachedUser())
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

}
