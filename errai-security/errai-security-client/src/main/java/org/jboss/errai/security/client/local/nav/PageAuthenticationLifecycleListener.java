package org.jboss.errai.security.client.local.nav;

import org.jboss.errai.ioc.client.lifecycle.api.Access;
import org.jboss.errai.ioc.client.lifecycle.api.LifecycleEvent;
import org.jboss.errai.ioc.client.lifecycle.api.LifecycleListener;
import org.jboss.errai.security.client.local.identity.ActiveUserProvider;
import org.jboss.errai.security.client.local.identity.ActiveUserProviderImpl;
import org.jboss.errai.ui.nav.client.local.api.LoginPage;

import com.google.gwt.user.client.ui.IsWidget;

public class PageAuthenticationLifecycleListener<W extends IsWidget> implements LifecycleListener<W> {

  @Override
  public void observeEvent(final LifecycleEvent<W> event) {
    final ActiveUserProvider userProvider = ActiveUserProviderImpl.getInstance();
    if (!userProvider.isCacheValid() || !userProvider.hasActiveUser()) {
      event.veto();
      SecurityNavigationUtil.navigateToPage(LoginPage.class);
    }
  }

  @Override
  public boolean isObserveableEventType(final Class<? extends LifecycleEvent<W>> eventType) {
    return eventType.equals(Access.class);
  }

}
