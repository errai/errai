package org.jboss.errai.security.test.event.client.local;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;

import org.jboss.errai.security.shared.event.LoggedInEvent;
import org.jboss.errai.security.shared.event.LoggedOutEvent;

@ApplicationScoped
public class EventTestModule {

  private int loginEvents = 0;
  private int logoutEvents = 0;

  public void reset() {
    loginEvents = 0;
    logoutEvents = 0;
  }

  public void onLogin(@Observes LoggedInEvent event) {
    loginEvents += 1;
  }

  public void onLogout(@Observes LoggedOutEvent event) {
    logoutEvents += 1;
  }

  public int getLoginEvents() {
    return loginEvents;
  }

  public int getLogoutEvents() {
    return logoutEvents;
  }

}
