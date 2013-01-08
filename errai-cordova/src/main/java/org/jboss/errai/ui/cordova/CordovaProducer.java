package org.jboss.errai.ui.cordova;

import com.google.gwt.core.client.GWT;
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
import javax.inject.Singleton;

/**
 * Wrapper around gwtphonegap so we can inject the phonegap capabilities in a more errai way
 */
@Singleton
public class CordovaProducer {
  private PhoneGap phoneGap;

  private PhoneGap getPhoneGap() {
    if (phoneGap == null) {
      phoneGap = GWT.create(PhoneGap.class);
      phoneGap.initializePhoneGap();
    }

    return phoneGap;
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