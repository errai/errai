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
