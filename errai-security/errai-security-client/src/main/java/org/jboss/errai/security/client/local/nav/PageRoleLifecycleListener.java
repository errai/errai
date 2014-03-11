package org.jboss.errai.security.client.local.nav;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jboss.errai.ioc.client.lifecycle.api.Access;
import org.jboss.errai.ioc.client.lifecycle.api.LifecycleEvent;
import org.jboss.errai.ioc.client.lifecycle.api.LifecycleListener;
import org.jboss.errai.security.client.local.identity.ActiveUserProvider;
import org.jboss.errai.security.client.local.identity.ActiveUserProviderImpl;
import org.jboss.errai.security.client.local.util.SecurityUtil;
import org.jboss.errai.security.shared.Role;
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
  
  private final Set<String> roles;
  
  public PageRoleLifecycleListener(final String... roles) {
    this.roles = new HashSet<String>();
    
    for (int i = 0; i < roles.length; i++) {
      this.roles.add(roles[i]);
    }
  }

  @Override
  public void observeEvent(final LifecycleEvent<W> event) {
    final ActiveUserProvider activeUserProvider = ActiveUserProviderImpl.getInstance();
    if (!activeUserProvider.isCacheValid() || !activeUserProvider.hasActiveUser()
            || !containsRoles(activeUserProvider.getActiveUser().getRoles(), roles)) {
      event.veto();

      final Class<? extends UniquePageRole> destination;
      if (!activeUserProvider.hasActiveUser())
        destination = LoginPage.class;
      else
        destination = SecurityError.class;
      
      SecurityUtil.navigateToPage(destination);
    }
  }

  @Override
  public boolean isObserveableEventType(final Class<? extends LifecycleEvent<W>> eventType) {
    return eventType.equals(Access.class);
  }
  
  private boolean containsRoles(final List<Role> userRoles, final Set<String> requiredRoles) {
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
