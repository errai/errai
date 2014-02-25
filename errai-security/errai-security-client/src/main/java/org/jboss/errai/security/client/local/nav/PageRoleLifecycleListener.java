package org.jboss.errai.security.client.local.nav;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jboss.errai.common.client.util.CreationalCallback;
import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.ioc.client.lifecycle.api.LifecycleEvent;
import org.jboss.errai.ioc.client.lifecycle.api.LifecycleListener;
import org.jboss.errai.security.client.local.identity.ActiveUserProvider;
import org.jboss.errai.security.client.local.identity.ActiveUserProviderImpl;
import org.jboss.errai.security.shared.Role;
import org.jboss.errai.ui.nav.client.local.Navigation;
import org.jboss.errai.ui.nav.client.local.api.SecurityError;
import org.jboss.errai.ui.nav.client.local.lifecycle.TransitionEvent;

import com.google.gwt.user.client.ui.IsWidget;

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
    if (!activeUserProvider.hasActiveUser() || !containsRoles(activeUserProvider.getActiveUser().getRoles(), roles)) {
      event.veto();
      IOC.getAsyncBeanManager().lookupBean(Navigation.class).getInstance(new CreationalCallback<Navigation>() {
        
        @Override
        public void callback(final Navigation nav) {
          nav.goToWithRole(SecurityError.class);
        }
      });
    }
  }

  @Override
  public boolean isObserveableEventType(final Class<? extends LifecycleEvent<W>> eventType) {
    return eventType.equals(TransitionEvent.class);
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
