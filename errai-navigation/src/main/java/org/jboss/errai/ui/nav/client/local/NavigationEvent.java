package org.jboss.errai.ui.nav.client.local;


/**
 * A CDI event that is fired when the user navigates to a different page within the app.
 *
 * @author edewit@redhat.com
 */
public class NavigationEvent {
  private final HistoryToken pageRequest;

  public NavigationEvent(HistoryToken pageRequest) {
    this.pageRequest = pageRequest;
  }

  public HistoryToken getHistoryToken() {
    return pageRequest;
  }
}
