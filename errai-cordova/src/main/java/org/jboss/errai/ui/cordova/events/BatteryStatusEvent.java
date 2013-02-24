package org.jboss.errai.ui.cordova.events;

import org.jboss.errai.common.client.api.annotations.Portable;
import org.jboss.errai.marshalling.client.api.annotations.MapsTo;

/**
 * @author edewit@redhat.com
 */
@Portable
public class BatteryStatusEvent extends BatteryEvent {
  protected BatteryStatusEvent(@MapsTo("level") int level, @MapsTo("plugged") boolean plugged) {
    super(level, plugged);
  }
}
