package org.jboss.errai.ui.nav.client.local;

import org.jboss.errai.common.client.util.CreationalCallback;
import org.jboss.errai.ioc.client.container.IOC;

/**
 * This class works like a {@link TransitionTo} but where the target is a {@link UniquePageRole}. By
 * injecting and instance of this class you declare a compile-time dependency on the existence of a
 * {@link Page} with the {@link UniquePageRole} of type {@code U}.
 *
 * @author Max Barkley <mbarkley@redhat.com>
 * @param <U>
 *          The type of {@link UniquePageRole} that this transition navigates to.
 */
public final class TransitionToRole<U extends UniquePageRole> {

  private Class<U> uniquePageRole;

  public TransitionToRole(final Class<U> uniquePageRole) {
    this.uniquePageRole = uniquePageRole;
  }

  public void go() {
    IOC.getAsyncBeanManager().lookupBean(Navigation.class).getInstance(new CreationalCallback<Navigation>() {

      @Override
      public void callback(final Navigation navigation) {
        navigation.goToWithRole(uniquePageRole);
      }
    });
  }

  public Class<U> toUniquePageRole() {
    return uniquePageRole;
  }
}
