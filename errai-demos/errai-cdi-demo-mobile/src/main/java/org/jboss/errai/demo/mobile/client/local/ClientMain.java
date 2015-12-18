/*
 * Copyright (C) 2013 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.demo.mobile.client.local;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.jboss.errai.demo.mobile.client.shared.AllClientOrientations;
import org.jboss.errai.demo.mobile.client.shared.ClientOrientationEvent;
import org.jboss.errai.demo.mobile.client.shared.Disconnected;
import org.jboss.errai.demo.mobile.client.shared.Ongoing;
import org.jboss.errai.ioc.client.api.EntryPoint;
import org.jboss.errai.orientation.client.local.OrientationDetector;
import org.jboss.errai.orientation.client.shared.OrientationEvent;

import com.google.gwt.animation.client.AnimationScheduler;
import com.google.gwt.animation.client.AnimationScheduler.AnimationCallback;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * Main application entry point.
 */
@EntryPoint
public class ClientMain {

  /**
   * Don't try to fire a CDI OrientationEvent event more than once every 175ms.
   */
  private static final long MIN_EVENT_INTERVAL = 175;

  /**
   * The time we last fired an OrientationEvent.
   */
  private long lastEventFireTime;

  @Inject
  OrientationDetector orientationDetector;

  @Inject
  @Ongoing
  private Event<ClientOrientationEvent> clientOrientationEvent;

  private WelcomeDialog welcomeDialog;
  private final Map<String, PerspectiveAnimator> animators = new HashMap<String, PerspectiveAnimator>();
  private final AnimationScheduler animScheduler = AnimationScheduler.get();

  @PostConstruct
  public void init() {
    welcomeDialog = new WelcomeDialog(new Runnable() {
      @Override
      public void run() {
        RootPanel.get("rootPanel").remove(welcomeDialog);
        orientationDetector.startFiringOrientationEvents();
      }
    });
    RootPanel.get("rootPanel").add(welcomeDialog);
    welcomeDialog.nameBox.setFocus(true);

    animScheduler.requestAnimationFrame(new AnimationCallback() {
      @Override
      public void execute(double timestamp) {
        for (PerspectiveAnimator animator : animators.values()) {
          animator.nextFrame();
        }
        animScheduler.requestAnimationFrame(this);
      }
    });
  }

  public void visualizeOrientationEvent(ClientOrientationEvent e) {
    Element rotateMe = Document.get().getElementById("rotateMe-" + e.getClientId());
    if (rotateMe == null) {
      // must be a new client! We will clone the template for this new client.
      Element template = Document.get().getElementById("rotateMeTemplate");
      rotateMe = (Element) template.cloneNode(true);
      rotateMe.setId("rotateMe-" + e.getClientId());
      rotateMe.getFirstChildElement().setInnerText(e.getClientId());
      template.getParentElement().appendChild(rotateMe);
    }

    PerspectiveAnimator animator = animators.get(e.getClientId());
    if (animator == null) {
      animator = new PerspectiveAnimator(rotateMe);
      animators.put(e.getClientId(), animator);
    }

    animator.updateTargets(e);
  }

  public void onAllClientOrientationsUpdate(@Observes AllClientOrientations aco) {
    for (ClientOrientationEvent e : aco.getClientOrientations()) {
      visualizeOrientationEvent(e);
    }
  }

  public void onOrientationEvent(@Observes OrientationEvent event) {
    long now = System.currentTimeMillis();

    if (now - lastEventFireTime < MIN_EVENT_INTERVAL) {
      return;
    }
    lastEventFireTime = now;

    clientOrientationEvent.fire(new ClientOrientationEvent(welcomeDialog.getName(), event));
  }

  public void onClientDisconnect(@Observes @Disconnected ClientOrientationEvent e) {
    Element rotateMe = Document.get().getElementById("rotateMe-" + e.getClientId());
    if (rotateMe != null) {
      rotateMe.getParentElement().removeChild(rotateMe);
    }
    animators.remove(e.getClientId());
  }
}
