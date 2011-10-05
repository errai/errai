package org.jboss.errai.cdi.test.stress.client.shared;

import org.jboss.errai.bus.server.annotations.ExposeEntity;

/**
 * Request object sent by the client when it wants a new stream of ticks from
 * the server.
 * <p>
 * For all numeric properties in this configuration request object, a value of
 * -1 means not to change the existing setting. For example, a
 * ConfigurationRequest with {@code messageInterval = -1},
 * {@code messageSize = -1}, and {@code payloadSize = 1000} means
 * "leave the current message interval and count as is, and change the message
 * size to 1000 bytes."
 */
@ExposeEntity
public class ConfigurationRequest {

  /**
   * Number of milliseconds between message bursts.
   */
  private int messageInterval = -1;
  
  /**
   * Number of messages per message burst.
   */
  private int messageCount = -1;
  
  /**
   * Number of extra payload bytes to attach to each message in a burst.
   */
  private int payloadSize = -1;
  
  /**
   * Creates a new ConfigurationRequest object with all properties set to -1.
   */
  public ConfigurationRequest() {
  }

  /**
   * Returns the requested number of milliseconds between message bursts.
   */
  public int getMessageInterval() {
    return messageInterval;
  }

  /**
   * Requests a new number of milliseconds between message bursts.
   */
  public void setMessageInterval(int messageInterval) {
    this.messageInterval = messageInterval;
  }

  /**
   * Returns the requested number of messages per message burst.
   */
  public int getMessageCount() {
    return messageCount;
  }

  /**
   * Requests a new number of messages per message burst.
   */
  public void setMessageCount(int messageCount) {
    this.messageCount = messageCount;
  }

  /**
   * Returns the requested size in bytes for each message in a burst.
   */
  public int getPayloadSize() {
    return payloadSize;
  }

  /**
   * Requests a new extra payload size in bytes for each message in a burst.
   */
  public void setPayloadSize(int payloadSize) {
    this.payloadSize = payloadSize;
  }

  @Override
  public String toString() {
    return "ConfigurationRequest [messageInterval=" + messageInterval
        + ", messageCount=" + messageCount + ", payloadSize=" + payloadSize
        + "]";
  }

}