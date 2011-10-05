package org.jboss.errai.cdi.test.stress.client.shared;

import org.jboss.errai.bus.server.annotations.ExposeEntity;

/**
 * The tick event that comes from the server.
 */
@ExposeEntity
public class TickEvent {
  
    private int id;
    private long serverTime;
    private String payload;

    public TickEvent() {
    }

    public TickEvent(int id, long serverTime, String payload) {
      this.id = id;
      this.serverTime = serverTime;
      this.payload = payload;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getServerTime() {
      return serverTime;
    }
    
    public void setServerTime(long serverTime) {
      this.serverTime = serverTime;
    }

    public String getPayload() {
      return payload;
    }

    public void setPayload(String payload) {
      this.payload = payload;
    }
    
}