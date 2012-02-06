package org.jboss.errai.cdi.test.stress.client.shared;

import org.jboss.errai.common.client.api.annotations.Portable;

/**
 * The tick event that comes from the server.
 */
@Portable
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
    
    @Override
    public String toString() {
      int payloadLength = payload == null ? 0 : payload.length();
      return "Tick " + id + " at " + serverTime +
          " with " + payloadLength + " byte payload";
    }
}