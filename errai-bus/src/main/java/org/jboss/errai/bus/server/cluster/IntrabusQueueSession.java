package org.jboss.errai.bus.server.cluster;

import org.jboss.errai.bus.client.api.QueueSession;
import org.jboss.errai.bus.client.api.SessionEndListener;

import java.util.Collection;

/**
 * @author Mike Brock
 */
public class IntrabusQueueSession implements QueueSession {
  public static final QueueSession INSTANCE = new IntrabusQueueSession();

  private IntrabusQueueSession() {
  }

  @Override
  public String getSessionId() {
    return "INTRABUS_SESSION";
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
}
