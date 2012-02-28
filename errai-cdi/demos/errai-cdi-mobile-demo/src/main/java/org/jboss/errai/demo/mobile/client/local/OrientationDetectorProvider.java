package org.jboss.errai.demo.mobile.client.local;

import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.jboss.errai.demo.mobile.client.shared.OrientationEvent;
import org.jboss.errai.ioc.client.api.IOCProvider;

import com.google.gwt.core.client.GWT;

@IOCProvider
@Singleton
public class OrientationDetectorProvider implements Provider<OrientationDetector> {

  @Inject Event<OrientationEvent> orientationEventSource;

  @Override
  public OrientationDetector get() {
    GWT.log("Creating orientation detector...");
    OrientationDetector detector = GWT.create(OrientationDetector.class);
    GWT.log("Created " + detector);
    detector.setOrientationEventSource(orientationEventSource);
    GWT.log("Added event source " + orientationEventSource);
    return detector;
  }

}
