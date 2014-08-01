package org.jboss.errai.ui.cordova.events;

import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.jboss.errai.ioc.client.api.AfterInitialization;

import com.googlecode.gwtphonegap.client.event.BackButtonPressedEvent;
import com.googlecode.gwtphonegap.client.event.BackButtonPressedHandler;
import com.googlecode.gwtphonegap.client.event.EndCallButtonPressedEvent;
import com.googlecode.gwtphonegap.client.event.EndCallButtonPressedHandler;
import com.googlecode.gwtphonegap.client.event.MenuButtonPressedEvent;
import com.googlecode.gwtphonegap.client.event.MenuButtonPressedHandler;
import com.googlecode.gwtphonegap.client.event.SearchButtonPressedEvent;
import com.googlecode.gwtphonegap.client.event.SearchButtonPressedHandler;
import com.googlecode.gwtphonegap.client.event.StartCallButtonPressedEvent;
import com.googlecode.gwtphonegap.client.event.StartCallButtonPressedHandler;
import com.googlecode.gwtphonegap.client.event.VolumeDownButtonPressedEvent;
import com.googlecode.gwtphonegap.client.event.VolumeDownButtonPressedHandler;
import com.googlecode.gwtphonegap.client.event.VolumeUpButtonPressedEvent;
import com.googlecode.gwtphonegap.client.event.VolumeUpButtonPressedHandler;

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

    event.getVolumeDownButtonPressedHandler().addVolumeDownButtonPressedHandler(new VolumeDownButtonPressedHandler() {
      @Override
      public void onVolumeDownButtonPressed(VolumeDownButtonPressedEvent event) {
        volumeDownButtonEventSource.fire(new VolumeDownButtonEvent());
      }
    });

    event.getVolumeUpButtonPressedHandler().addVolumeUpButtonPressedHandler(new VolumeUpButtonPressedHandler() {
      @Override
      public void onVolumeUpButtonPressed(VolumeUpButtonPressedEvent event) {
        volumeUpButtonEventSource.fire(new VolumeUpButtonEvent());
      }
    });
  }
}
