package org.jboss.as.websockets.frame;

import org.jboss.as.websockets.FrameType;

/**
 * @author Mike Brock
 */
public class PongFrame extends AbstractFrame {
  private static final PongFrame INSTANCE = new PongFrame();

  public PongFrame() {
    super(FrameType.Pong);
  }

  public static PongFrame get() {
    return INSTANCE;
  }
}
