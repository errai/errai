/*
 * Copyright 2012 JBoss, a division of Red Hat Hat, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.errai.demo.mobile.client.local;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.jboss.errai.demo.mobile.client.shared.AllClientOrientations;
import org.jboss.errai.demo.mobile.client.shared.OrientationEvent;
import org.jboss.errai.ioc.client.api.EntryPoint;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * Main application entry point.
 */
@EntryPoint
public class ClientMain {

  @Inject OrientationDetector orientationDetector;
  private WelcomeDialog welcomeDialog;

  @PostConstruct
  public void init() {
    welcomeDialog = new WelcomeDialog(new Runnable() {
      @Override
      public void run() {
        orientationDetector.setClientId(welcomeDialog.getNameBoxContents());
        RootPanel.get().remove(welcomeDialog);

        // TODO: could block startup using InitBallot/voteForInit()
        GWT.log("Starting to poll for readiness! Orientation detector: " + orientationDetector);
        // poll for readiness; when it's ready, start watching device orientation.
        Timer t = new Timer() {
          @Override
          public void run() {
            GWT.log("Orientation detector: " + orientationDetector);
            if (orientationDetector.isReady()) {
              orientationDetector.startFiringOrientationEvents();
            } else {
              schedule(100);
            }
          }
        };
        t.schedule(100);
      }
    });
    RootPanel.get().add(welcomeDialog);
  }

  public void visualizeOrientationEvent(OrientationEvent e) {
    Element rotateMe = Document.get().getElementById("rotateMe-" + e.getClientId());
    if (rotateMe == null) {
      // must be a new client! We will clone the template for this new client.
      Element template = Document.get().getElementById("rotateMeTemplate");
      rotateMe = (Element) template.cloneNode(true);
      rotateMe.setId("rotateMe-" + e.getClientId());
      template.getParentElement().appendChild(rotateMe);
    }
    String transform = "rotate(" + e.getX() + "deg)";

    // could use deferred binding for this, but it's probably overkill
    rotateMe.getStyle().setProperty("MozTransform", transform);
    rotateMe.getStyle().setProperty("WebkitTransform", transform);
    rotateMe.getStyle().setProperty("transform", transform);
    GWT.log("Transform: " + transform + "; rotateMe=" + rotateMe);
  }

  public void onAllClientOrientationsUpdate(@Observes AllClientOrientations aco) {
    for (OrientationEvent e : aco.getClientOrientations()) {
      visualizeOrientationEvent(e);
    }
  }
}
