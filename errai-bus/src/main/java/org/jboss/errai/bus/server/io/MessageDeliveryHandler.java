package org.jboss.errai.bus.server.io;

import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.server.api.MessageQueue;

import java.io.IOException;

/**
 * @author Mike Brock
 */
public interface MessageDeliveryHandler {
  public boolean deliver(MessageQueue queue, Message message) throws IOException;
  public boolean deliverRaw(MessageQueue queue, String rawMessage) throws IOException;
  public void noop(MessageQueue queue) throws IOException;
}
