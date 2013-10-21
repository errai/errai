package org.jboss.errai.ui.nav.client.shared;

import org.jboss.errai.marshalling.client.api.annotations.MapsTo;

/**
 * @author edewit@redhat.com
 */
public class NavigationEvent {
  private final PageRequest pageRequest;

  public NavigationEvent(@MapsTo("pageRequest") PageRequest pageRequest) {
    this.pageRequest = pageRequest;
  }

  public PageRequest getPageRequest() {
    return pageRequest;
  }
}