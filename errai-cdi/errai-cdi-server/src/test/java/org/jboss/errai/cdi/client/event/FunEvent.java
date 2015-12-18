package org.jboss.errai.cdi.client.event;

import org.jboss.errai.common.client.api.annotations.MapsTo;
import org.jboss.errai.common.client.api.annotations.Portable;

/**
 * @author Mike Brock
 */
@Portable
public class FunEvent {
  private final String text;

  public FunEvent(@MapsTo("text") String text) {
    this.text = text;
  }

  public String getText() {
    return text;
  }
}
