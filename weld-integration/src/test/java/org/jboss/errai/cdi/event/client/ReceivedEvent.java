package org.jboss.errai.cdi.event.client;

import org.jboss.errai.bus.server.annotations.ExposeEntity;

@ExposeEntity
public class ReceivedEvent {
  private String receiver;
  private String event;

  public ReceivedEvent() {}

  public ReceivedEvent(String receiver, String event) {
    this.receiver = receiver;
    this.event = event;
  }

  public String getReceiver() {
    return receiver;
  }

  public void setReceiver(String receiver) {
    this.receiver = receiver;
  }

  public String getEvent() {
    return event;
  }

  public void setEvent(String event) {
    this.event = event;
  }
}
