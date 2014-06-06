package org.jboss.errai.bus.server.websocket.test.jsr356.cdi.adapter;

import javax.enterprise.context.RequestScoped;

/**
 * @author Michel Werren
 */
@RequestScoped
public class RequestScopedBean {

  private long timestamp;

  public long getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(long timestamp) {
    this.timestamp = timestamp;
  }
}
