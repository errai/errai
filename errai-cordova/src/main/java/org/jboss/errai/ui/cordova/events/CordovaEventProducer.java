/*
 * Copyright (C) 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.ui.cordova.events;

import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.jboss.errai.ioc.client.api.AfterInitialization;

import com.googlecode.gwtphonegap.client.event.BatteryCriticalHandler;
import com.googlecode.gwtphonegap.client.event.BatteryLowHandler;
import com.googlecode.gwtphonegap.client.event.BatteryStatusHandler;
import com.googlecode.gwtphonegap.client.event.OffLineHandler;
import com.googlecode.gwtphonegap.client.event.OnlineHandler;
import com.googlecode.gwtphonegap.client.event.PauseHandler;
import com.googlecode.gwtphonegap.client.event.ResumeHandler;

/**
 * @author edewit@redhat.com
 */
@Singleton
public class CordovaEventProducer {

  @Inject
  com.googlecode.gwtphonegap.client.event.Event event;

  @Inject
  Event<OffLineEvent> offLineEventSource;

  @Inject
  Event<OnlineEvent> onlineEventSource;

  @Inject
  Event<ResumeEvent> resumeEventSource;

  @Inject
  Event<PauseEvent> pauseEventSource;

  @Inject
  Event<BatteryCriticalEvent> batteryCriticalEventSource;

  @Inject
  Event<BatteryLowEvent> batteryLowEventSource;

  @Inject
  Event<BatteryStatusEvent> batteryStatusEventSource;

  @AfterInitialization
  public void init() {

    event.getOffLineHandler().addOfflineHandler(new OffLineHandler() {
      @Override
      public void onOffLine(com.googlecode.gwtphonegap.client.event.OffLineEvent event) {
        offLineEventSource.fire(new OffLineEvent());
      }
    });

    event.getOnlineHandler().addOnlineHandler(new OnlineHandler() {
      @Override
      public void onOnlineEvent(com.googlecode.gwtphonegap.client.event.OnlineEvent event) {
        onlineEventSource.fire(new OnlineEvent());
      }
    });

    event.getResumeHandler().addResumeHandler(new ResumeHandler() {

      @Override
      public void onResumeEvent(com.googlecode.gwtphonegap.client.event.ResumeEvent event) {
        resumeEventSource.fire(new ResumeEvent());
      }
    });

    event.getPauseHandler().addPauseHandler(new PauseHandler() {
      @Override
      public void onPause(com.googlecode.gwtphonegap.client.event.PauseEvent event) {
        pauseEventSource.fire(new PauseEvent());
      }
    });

    event.getBatteryCriticalHandler().addBatteryCriticalHandler(new BatteryCriticalHandler() {
      @Override
      public void onBatteryCritical(com.googlecode.gwtphonegap.client.event.BatteryCriticalEvent event) {
        batteryCriticalEventSource.fire(new BatteryCriticalEvent(event.getLevel(), event.isPlugged()));
      }
    });

    event.getBatteryLowHandler().addBatteryLowHandler(new BatteryLowHandler() {
      @Override
      public void onBatteryLow(com.googlecode.gwtphonegap.client.event.BatteryLowEvent event) {
        batteryLowEventSource.fire(new BatteryLowEvent(event.getLevel(), event.isPlugged()));
      }
    });

    event.getBatteryStatusHandler().addBatteryStatusHandler(new BatteryStatusHandler() {
      @Override
      public void onBatteryStatus(com.googlecode.gwtphonegap.client.event.BatteryStatusEvent event) {
        batteryStatusEventSource.fire(new BatteryStatusEvent(event.getLevel(), event.isPlugged()));
      }
    });
  }
}
