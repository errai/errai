package org.jboss.errai.ui.cordova.events;

import org.jboss.errai.common.client.api.annotations.Portable;
import org.jboss.errai.marshalling.client.api.annotations.MapsTo;

/**
 * @author edewit@redhat.com
 */
@Portable
public abstract class BatteryEvent {
  private final int level;
  private final boolean isPlugged;

  protected BatteryEvent(@MapsTo("level") int level, @MapsTo("plugged") boolean plugged) {
    this.level = level;
    isPlugged = plugged;
  }

  public int getLevel() {
    return level;
  }

  public boolean isPlugged() {
    return isPlugged;
  }

  @Override
  public String toString() {
    return "BatteryEvent{" +
            "level=" + level +
            ", isPlugged=" + isPlugged +
            '}';
  }
}
