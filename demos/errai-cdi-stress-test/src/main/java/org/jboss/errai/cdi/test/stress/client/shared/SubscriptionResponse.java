package org.jboss.errai.cdi.test.stress.client.shared;

import org.jboss.errai.bus.server.annotations.ExposeEntity;

/**
 * The event that comes from the server in response to a
 * {@link SubscriptionRequest} from the client.
 */
@ExposeEntity
public class SubscriptionResponse {
  
    private long serverTime;

    public SubscriptionResponse() {
    }

    public SubscriptionResponse(long serverTime) {
      this.serverTime = serverTime;
    }

    public long getServerTime() {
      return serverTime;
    }
    
    public void setServerTime(long serverTime) {
      this.serverTime = serverTime;
    }
    
}