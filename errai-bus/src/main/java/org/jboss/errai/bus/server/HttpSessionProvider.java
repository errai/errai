/*
 * Copyright 2011 JBoss, by Red Hat, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.bus.server;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.jboss.errai.bus.client.api.QueueSession;
import org.jboss.errai.bus.client.api.SessionEndEvent;
import org.jboss.errai.bus.client.api.SessionEndListener;
import org.jboss.errai.bus.client.api.laundry.LaundryListProviderFactory;
import org.jboss.errai.bus.server.api.SessionProvider;
import org.jboss.errai.bus.server.util.SecureHashUtil;
import org.jboss.errai.bus.server.util.ServerLaundryList;

/**
 * The SessionProvider for HTTP-based queue sessions.
 */
public class HttpSessionProvider implements SessionProvider<HttpSession> {
  public static final String HTTP_SESS = "org.jboss.errai.QueueSessions";

  @Override
  public QueueSession createOrGetSession(final HttpSession externSessRef, final String remoteQueueID) {
    SessionsContainer sc = (SessionsContainer) externSessRef.getAttribute(HTTP_SESS);
    if (sc == null) {
      externSessRef.setAttribute(HTTP_SESS, sc = new SessionsContainer());
    }

    QueueSession qs = sc.getSession(remoteQueueID);
    if (qs == null) {
      qs = sc.createSession(externSessRef.getId(), remoteQueueID);
      qs.setAttribute(HttpSession.class.getName(), externSessRef);
    }

    return qs;
  }

  public static class SessionsContainer implements Serializable {
    private final Map<String, Object> sharedAttributes = new HashMap<String, Object>();
    private final Map<String, QueueSession> queueSessions = new HashMap<String, QueueSession>();

    public QueueSession createSession(final String httpSessionId, final String remoteQueueId) {
      final QueueSession qs = new HttpSessionWrapper(this, httpSessionId, remoteQueueId);
      queueSessions.put(remoteQueueId, qs);
      return qs;
    }

    public QueueSession getSession(final String remoteQueueId) {
      return queueSessions.get(remoteQueueId);
    }

    public void removeSession(final String remoteQueueId) {
      queueSessions.remove(remoteQueueId);
    }
  }

  public static class HttpSessionWrapper implements QueueSession, Serializable {
    private final SessionsContainer container;
    private final String parentSessionId;
    private final String sessionId;
    private final String remoteQueueID;
    private List<SessionEndListener> sessionEndListeners;

    public HttpSessionWrapper(final SessionsContainer container, final String httpSessionId,
                              final String remoteQueueID) {
      this.container = container;
      this.remoteQueueID = remoteQueueID;
      this.parentSessionId = httpSessionId;
      this.sessionId = SecureHashUtil.nextSecureHash("SHA-256",
              httpSessionId.getBytes(), remoteQueueID.getBytes());
    }

    @Override
    public String getSessionId() {
      return sessionId;
    }

    @Override
    public String getParentSessionId() {
      return parentSessionId;
    }

    @Override
    public boolean endSession() {
      container.removeSession(remoteQueueID);
      fireSessionEndListeners();
      return true;
    }

    @Override
    public void setAttribute(final String attribute, final Object value) {
      container.sharedAttributes.put(attribute, value);
    }

    @Override
    public <T> T getAttribute(final Class<T> type, final String attribute) {
      return (T) container.sharedAttributes.get(attribute);
    }

    @Override
    public Collection<String> getAttributeNames() {
      return container.sharedAttributes.keySet();
    }

    @Override
    public boolean hasAttribute(final String attribute) {
      return container.sharedAttributes.containsKey(attribute);
    }

    @Override
    public Object removeAttribute(final String attribute) {
      return container.sharedAttributes.remove(attribute);
    }

    @Override
    public void addSessionEndListener(final SessionEndListener listener) {
      synchronized (this) {
        if (sessionEndListeners == null) {
          sessionEndListeners = new ArrayList<SessionEndListener>();
        }
        sessionEndListeners.add(listener);
      }
    }

    private void fireSessionEndListeners() {
      ((ServerLaundryList) LaundryListProviderFactory.get().getLaundryList(this)).cleanAll();

      if (sessionEndListeners == null) return;
      SessionEndEvent event = new SessionEndEvent(this);

      for (SessionEndListener sessionEndListener : sessionEndListeners) {
        sessionEndListener.onSessionEnd(event);
      }
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof HttpSessionWrapper)) return false;

      HttpSessionWrapper that = (HttpSessionWrapper) o;

      if (remoteQueueID != null ? !remoteQueueID.equals(that.remoteQueueID) : that.remoteQueueID != null) return false;
      if (sessionId != null ? !sessionId.equals(that.sessionId) : that.sessionId != null) return false;

      return true;
    }

    @Override
    public int hashCode() {
      int result = (sessionId != null ? sessionId.hashCode() : 0);
      result = 31 * result + (remoteQueueID != null ? remoteQueueID.hashCode() : 0);
      return result;
    }

    @Override
    public String toString() {
      return "HttpSessionWrapper{" +
              ", sessionId='" + sessionId + '\'' +
              ", remoteQueueID='" + remoteQueueID + '\'' +
              '}';
    }
  }
}
