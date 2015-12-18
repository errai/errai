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

import com.google.gwt.dom.client.Element;
import org.jboss.errai.orientation.client.shared.OrientationEvent;

public class PerspectiveAnimator {

  /**
   * Number of degrees along each axis that the animation can progress in each frame.
   */
  private static final double MAX_CHANGE_PER_FRAME = 5;

  private final Element element;

  private double x;
  private double y;
  private double z;

  private double targetX;
  private double targetY;
  private double targetZ;

  public PerspectiveAnimator(Element element) {
    this.element = element;
  }

  public void updateTargets(OrientationEvent e) {
    targetX = e.getX();
    targetY = e.getY();
    targetZ = e.getZ();
  }

  public void nextFrame() {
    x += Math.min(MAX_CHANGE_PER_FRAME, targetX - x);
    y += Math.min(MAX_CHANGE_PER_FRAME, targetY - y);
    z += Math.min(MAX_CHANGE_PER_FRAME, targetZ - z);

    String transform = "rotateX(" + (x - 90.0) + "deg) " + "rotateY(" + (-y) + "deg)";

    // rotating the main rectangle with the compass direction is annoying,
    // because then what you see on screen doesn't relate to what how you see
    // your phone in front of you. So we rotate a compass rose instead:
    String compassTransform = "rotate( " + (360.0 - z) + "deg)";

    // could use deferred binding for this, but it's probably overkill
    element.getStyle().setProperty("MozTransform", transform);
    element.getStyle().setProperty("WebkitTransform", transform);
    element.getStyle().setProperty("transform", transform);

    // XXX this is fragile because it assumes a certain layout of the div contents
    Element rotateMeCompass = element.getFirstChildElement().getNextSiblingElement();

    rotateMeCompass.getStyle().setProperty("MozTransform", compassTransform);
    rotateMeCompass.getStyle().setProperty("WebkitTransform", compassTransform);
    rotateMeCompass.getStyle().setProperty("transform", compassTransform);
  }
}
