package org.jboss.errai.cdi.client.event;

import org.jboss.errai.common.client.api.annotations.Portable;

@Portable
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
