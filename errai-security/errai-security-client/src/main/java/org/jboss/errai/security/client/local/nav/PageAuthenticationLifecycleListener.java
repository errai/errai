package org.jboss.errai.security.client.local.nav;

import org.jboss.errai.ioc.client.lifecycle.api.LifecycleEvent;
import org.jboss.errai.ioc.client.lifecycle.api.LifecycleListener;
import org.jboss.errai.security.client.local.identity.ActiveUserProviderImpl;
import org.jboss.errai.ui.nav.client.local.lifecycle.TransitionEvent;

import com.google.gwt.user.client.ui.IsWidget;

public class PageAuthenticationLifecycleListener<W extends IsWidget> implements LifecycleListener<W> {

  @Override
  public void observeEvent(final LifecycleEvent<W> event) {
    if (!ActiveUserProviderImpl.getInstance().hasActiveUser())
      event.veto();
  }

  @Override
  public boolean isObserveableEventType(final Class<? extends LifecycleEvent<W>> eventType) {
    return eventType.equals(TransitionEvent.class);
  }

}
