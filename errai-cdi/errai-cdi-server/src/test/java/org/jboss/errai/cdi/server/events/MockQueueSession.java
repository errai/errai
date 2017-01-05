package org.jboss.errai.cdi.server.events;

import java.util.Collection;

import org.jboss.errai.bus.client.api.QueueSession;
import org.jboss.errai.bus.client.api.SessionEndListener;

public class MockQueueSession implements QueueSession {

  @Override
  public String getSessionId() {
    return "bearista";
  }

  @Override
  public String getParentSessionId() {
    return null;
  }

  @Override
  public boolean endSession() {
    return false;
  }

  @Override
  public void setAttribute(String attribute, Object value) {
  }

  @Override
  public <T> T getAttribute(Class<T> type, String attribute) {
    return null;
  }

  @Override
  public Collection<String> getAttributeNames() {
    return null;
  }

  @Override
  public boolean hasAttribute(String attribute) {
    return false;
  }

  @Override
  public Object removeAttribute(String attribute) {
    return null;
  }

  @Override
  public void addSessionEndListener(SessionEndListener listener) {
  }

  @Override
  public boolean isValid() {
    return false;
  }

}
