package org.jboss.errai.bus.server.io;

import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.server.api.MessageQueue;

import java.io.IOException;

/**
 * The <tt>MessageDeliveryHandler</tt> defines the behavior of the message bus relative to the transport layer.
 *
 * @author Mike Brock
 */
public interface MessageDeliveryHandler {
  /**
   * This method is responsible for delivering a message into the transport layer. The contract between the message
   * bus and the transport layer is completely governed by the implementation of the method.
   *
   * @param queue
   *     the {@link MessageQueue} to deliver from.
   * @param message
   *     the {@link Message} to deliver.
   *
   * @return true if the message was successfully accepted.
   *
   * @throws IOException
   *     an IOException may be thrown if there is a problem interacting with the underlying transport.
   */
  public boolean deliver(MessageQueue queue, Message message) throws IOException;

  /**
   * Sends a NOOP (No-Operation) to the remote connected
   * @param queue
   * @throws IOException
   */
  public void noop(MessageQueue queue) throws IOException;
}
