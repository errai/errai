package org.jboss.as.websockets.frame;

import org.jboss.as.websockets.FrameType;

/**
 * Represents a binary WebSocket frame.
 *
 * @author Mike Brock
 */
public class BinaryFrame extends AbstractFrame {
  private final byte[] data;

  private BinaryFrame(byte[] data) {
    super(FrameType.Binary);
    this.data = data;
  }

  public static BinaryFrame from(byte[] data) {
    return new BinaryFrame(data);
  }

  public byte[] getByteArray() {
    return data;
  }
}
