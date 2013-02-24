package org.jboss.errai.ui.cordova.events;

/**
 * @author edewit@redhat.com
 */
public abstract class BatteryEvent {
  private final int level;
  private final boolean isPlugged;

  protected BatteryEvent(int level, boolean plugged) {
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
