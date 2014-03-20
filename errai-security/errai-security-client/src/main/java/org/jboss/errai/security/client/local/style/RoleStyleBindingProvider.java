package org.jboss.errai.security.client.local.style;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.jboss.errai.security.client.local.identity.ActiveUserProvider;
import org.jboss.errai.security.shared.api.annotation.RestrictedAccess;
import org.jboss.errai.security.shared.api.identity.Role;
import org.jboss.errai.security.shared.api.identity.User;
import org.jboss.errai.ui.shared.api.style.AnnotationStyleBindingExecutor;
import org.jboss.errai.ui.shared.api.style.StyleBindingsRegistry;

import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.user.client.Element;

/**
 * RoleStyleBindingProvider makes sure that client elements annotated by {@link RestrictedAccess} are made invisible for
 * users that do not have the role or roles specified.
 *
 * @see RestrictedAccess
 * @author edewit@redhat.com
 * @author Max Barkley <mbarkley@redhat.com>
 */
@Singleton
public class RoleStyleBindingProvider {

  private final ActiveUserProvider userProvider;

  @Inject
  public RoleStyleBindingProvider(final ActiveUserProvider userProvider) {
    this.userProvider = userProvider;
  }

  @PostConstruct
  public void init() {
    StyleBindingsRegistry.get().addStyleBinding(this, RestrictedAccess.class, new AnnotationStyleBindingExecutor() {
      @Override
      public void invokeBinding(final Element element, final Annotation annotation) {
        final User user = userProvider.getActiveUser();
        if (user == null || user.getRoles() == null || !hasRoles(user.getRoles(), ((RestrictedAccess) annotation).roles()))
          element.getStyle().setDisplay(Display.NONE);
        else
          element.getStyle().clearDisplay();
      }
    });
  }
  
  private boolean hasRoles(final Collection<Role> userRoles, final String[] requiredRoles) {
    final Set<String> userRolesByName = new HashSet<String>();
    for (final Role role : userRoles) {
      userRolesByName.add(role.getName());
    }

    for (int i = 0; i < requiredRoles.length; i++) {
      if (!userRolesByName.contains(requiredRoles[i]))
        return false;
    }
    
    return true;
  }

}
