package org.jboss.errai.security.client.local.identity;

import javax.enterprise.context.Dependent;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.jboss.errai.security.shared.api.identity.User;
import org.jboss.errai.security.shared.event.LoggedInEvent;
import org.jboss.errai.security.shared.event.LoggedOutEvent;

@Dependent
public class UserEventModule {
  
  @Inject
  private Event<LoggedInEvent> loggedInEvent;
  
  @Inject Event<LoggedOutEvent> loggedOutEvent;

  public void fireLoggedInEvent(final User user) {
    loggedInEvent.fire(new LoggedInEvent(user));
  }
  
  public void fireLoggedOutEvent() {
    loggedOutEvent.fire(new LoggedOutEvent());
  }

}
