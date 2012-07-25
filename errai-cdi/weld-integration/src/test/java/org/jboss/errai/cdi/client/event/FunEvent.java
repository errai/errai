package org.jboss.errai.cdi.client.event;

import org.jboss.errai.common.client.api.annotations.Portable;
import org.jboss.errai.marshalling.client.api.annotations.MapsTo;

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
