package org.jboss.errai.example.client.local.events;

import org.jboss.errai.example.shared.Tag;

/**
 * @author edewit@redhat.com
 */
public class TagUpdateEvent {
  private final Tag tag;


  public TagUpdateEvent(Tag tag) {
    this.tag = tag;
  }

  public Tag getTag() {
    return tag;
  }
}
