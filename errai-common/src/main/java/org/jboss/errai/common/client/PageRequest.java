package org.jboss.errai.common.client;

import java.util.Map;

import org.jboss.errai.common.client.api.annotations.MapsTo;
import org.jboss.errai.common.client.api.annotations.Portable;

/**
 * @author edewit@redhat.com
 */
@Portable
public class PageRequest {
  private final String pageName;
  private final Map<String, Object> state;

  public PageRequest(@MapsTo("pageName") String pageName, @MapsTo("state") Map<String, Object> state) {
    this.pageName = pageName;
    this.state = state;
  }

  public String getPageName() {
    return pageName;
  }

  public Map<String, Object> getState() {
    return state;
  }
}