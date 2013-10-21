package org.jboss.errai.ui.cordova.events;

import org.jboss.errai.common.client.api.annotations.MapsTo;
import org.jboss.errai.common.client.api.annotations.Portable;

/**
 * @author edewit@redhat.com
 */
@Portable
public class BatteryLowEvent extends BatteryEvent {

  protected BatteryLowEvent(@MapsTo("level") int level, @MapsTo("plugged") boolean plugged) {
    super(level, plugged);
  }
}
