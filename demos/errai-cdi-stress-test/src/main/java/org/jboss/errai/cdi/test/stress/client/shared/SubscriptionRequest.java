package org.jboss.errai.cdi.test.stress.client.shared;

import org.jboss.errai.bus.server.annotations.ExposeEntity;

/**
 * Request object sent by the client when it wants a new stream of ticks from
 * the server.
 */
@ExposeEntity
public class SubscriptionRequest {

  private long clientTimestamp;

  public SubscriptionRequest() {
  }

  public SubscriptionRequest(long clientTimestamp) {
    this.clientTimestamp = clientTimestamp;
  }

  public long getClientTimestamp() {
    return clientTimestamp;
  }

  public void setClientTimestamp(long clientTimestamp) {
    this.clientTimestamp = clientTimestamp;
  }

}