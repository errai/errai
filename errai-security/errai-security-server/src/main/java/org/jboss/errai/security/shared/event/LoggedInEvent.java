package org.jboss.errai.security.shared.event;

import org.jboss.errai.common.client.api.annotations.LocalEvent;
import org.jboss.errai.common.client.api.annotations.MapsTo;
import org.jboss.errai.security.shared.api.identity.User;

/**
 * LoggedInEvent fired when a user logs-in.
 *
 * @author edewit@redhat.com
 */
@LocalEvent
public class LoggedInEvent {
  private final User user;

  public LoggedInEvent(@MapsTo("user") User user) {
    this.user = user;
  }

  public User getUser() {
    return user;
  }
}
