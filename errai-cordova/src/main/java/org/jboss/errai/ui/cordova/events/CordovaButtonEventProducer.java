package org.jboss.errai.ui.cordova.events;

import com.googlecode.gwtphonegap.client.event.*;
import org.jboss.errai.ioc.client.api.AfterInitialization;

import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * @author edewit@redhat.com
 */
@Singleton
public class CordovaButtonEventProducer {

  @Inject
  com.googlecode.gwtphonegap.client.event.Event event;

  @Inject
  Event<BackButtonEvent> backButtonEventSource;

  @Inject
  Event<SearchButtonEvent> searchButtonEventSource;

  @Inject
  Event<MenuButtonEvent> menuButtonEventSource;

  @Inject
  Event<StartCallButtonEvent> startCallButtonEventSource;

  @Inject
  Event<EndCallButtonEvent> endCallButtonEventSource;

  @Inject
  Event<VolumeDownButtonEvent> volumeDownButtonEventSource;

  @Inject
  Event<VolumeUpButtonEvent> volumeUpButtonEventSource;

  @AfterInitialization
  public void init() {
    event.getBackButton().addBackButtonPressedHandler(new BackButtonPressedHandler() {
      @Override
      public void onBackButtonPressed(BackButtonPressedEvent event) {
        backButtonEventSource.fire(new BackButtonEvent());
      }
    });
    event.getSearchButton().addSearchButtonHandler(new SearchButtonPressedHandler() {
      @Override
      public void onSearchButtonPressed(SearchButtonPressedEvent event) {
        searchButtonEventSource.fire(new SearchButtonEvent());
      }
    });

    event.getMenuButton().addMenuButtonPressedHandler(new MenuButtonPressedHandler() {
      @Override
      public void onMenuButtonPressed(MenuButtonPressedEvent event) {
        menuButtonEventSource.fire(new MenuButtonEvent());
      }
    });

    event.getStartCallButtonHandler().addStartCallButtonHandler(new StartCallButtonPressedHandler() {
      @Override
      public void onStartCallButtonPressed(StartCallButtonPressedEvent event) {
        startCallButtonEventSource.fire(new StartCallButtonEvent());
      }
    });

    event.getEndCallButtonHandler().addEndCallButtonHandler(new EndCallButtonPressedHandler() {
      @Override
      public void onEndCallButtonPressed(EndCallButtonPressedEvent event) {
        endCallButtonEventSource.fire(new EndCallButtonEvent());
      }
    });

    event.getVolumneDownButtonPressedHandler().addVolumneDownButtonPressedHandler(new VolumeDownButtonPressedHandler() {
      @Override
      public void onVolumeDownButtonPressed(VolumeDownButtonPressedEvent event) {
        volumeDownButtonEventSource.fire(new VolumeDownButtonEvent());
      }
    });

    event.getVolumneUpButtonPressedHandler().addVolumneUpButtonPressedHandler(new VolumeUpButtonPressedHandler() {
      @Override
      public void onVolumeUpButtonPressed(VolumeUpButtonPressedEvent event) {
        volumeUpButtonEventSource.fire(new VolumeUpButtonEvent());
      }
    });
  }
}
