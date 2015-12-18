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

package org.jboss.errai.ui.cordova;

import com.googlecode.gwtphonegap.client.PhoneGap;
import com.googlecode.gwtphonegap.client.accelerometer.Accelerometer;
import com.googlecode.gwtphonegap.client.camera.Camera;
import com.googlecode.gwtphonegap.client.capture.Capture;
import com.googlecode.gwtphonegap.client.compass.Compass;
import com.googlecode.gwtphonegap.client.connection.Connection;
import com.googlecode.gwtphonegap.client.contacts.Contacts;
import com.googlecode.gwtphonegap.client.device.Device;
import com.googlecode.gwtphonegap.client.event.Event;
import com.googlecode.gwtphonegap.client.file.File;
import com.googlecode.gwtphonegap.client.media.MediaModule;
import com.googlecode.gwtphonegap.client.notification.Notification;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * @author edewit@redhat.com
 */
@Singleton
public class CordovaAccessoriesProducer {

  @Inject
  CordovaProducer cordovaProducer;

  public PhoneGap getPhoneGap() {
    return cordovaProducer.getPhoneGap();
  }

  @Produces
  public Camera produceCamera() {
    return getPhoneGap().getCamera();
  }

  @Produces
  public Accelerometer produceAccelerometer() {
    return getPhoneGap().getAccelerometer();
  }

  @Produces
  public Contacts produceContacts() {
    return getPhoneGap().getContacts();
  }

  @Produces
  public Capture produceCapture() {
    return getPhoneGap().getCapture();
  }

  @Produces
  public Compass produceCompass() {
    return getPhoneGap().getCompass();
  }

  @Produces
  public Notification produceNotification() {
    return getPhoneGap().getNotification();
  }

  @Produces
  public File produceFile() {
    return getPhoneGap().getFile();
  }

  @Produces
  public Event produceEvent() {
    return getPhoneGap().getEvent();
  }

  @Produces
  public Device produceDevice() {
    return getPhoneGap().getDevice();
  }

  @Produces
  public Connection produceConnection() {
    return getPhoneGap().getConnection();
  }

  @Produces
  public MediaModule produceMedia() {
    return getPhoneGap().getMedia();
  }
}
