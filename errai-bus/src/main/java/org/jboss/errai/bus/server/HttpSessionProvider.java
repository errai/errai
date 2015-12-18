/*
 * Copyright (C) 2012 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.bus.server;

import java.io.ObjectStreamException;
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
import org.jboss.errai.common.client.api.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The SessionProvider for HTTP-based queue sessions.
 */
public class HttpSessionProvider implements SessionProvider<HttpSession> {

  private static final Logger log = LoggerFactory.getLogger(HttpSessionProvider.class);

  @Override
  public QueueSession createOrGetSession(final HttpSession externSessRef, final String remoteQueueID) {
    final SessionsContainer sc;
    if (externSessRef.getAttribute(SessionsContainer.class.getName()) != null) {
      sc = (SessionsContainer) externSessRef.getAttribute(SessionsContainer.class.getName());
    }
    else {
      sc = new SessionsContainer();
      externSessRef.setAttribute(SessionsContainer.class.getName(), sc);
    }

    QueueSession qs = sc.getSession(remoteQueueID);
    if (qs == null) {
      log.debug("queue session " + remoteQueueID + " started");
      qs = sc.createSession(externSessRef.getId(), remoteQueueID);
      qs.setAttribute(HttpSession.class.getName(), externSessRef);
      qs.addSessionEndListener(new SessionEndListener() {
        @Override
        public void onSessionEnd(SessionEndEvent event) {
          log.debug("queue session " + remoteQueueID + " ended");
          sc.removeSession(remoteQueueID);
        }
      });
    }

    return qs;
  }

  public static class SessionsContainer implements Serializable {
    private transient final Map<String, Object> sharedAttributes = new HashMap<String, Object>();
    private transient final Map<String, QueueSession> queueSessions = new HashMap<String, QueueSession>();

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

    /**
     * Just returns a fresh new SessionsContainer.
     */
    private Object readResolve() throws ObjectStreamException {
      // this is necessary because deserializing the private final fields gets us a couple of big fat nulls.
      return new SessionsContainer();
    }
  }

  private static class HttpSessionWrapper implements QueueSession, Serializable {
    private final SessionsContainer container;
    private final String parentSessionId;
    private final String sessionId;
    private final String remoteQueueID;
    private List<SessionEndListener> sessionEndListeners;

    public HttpSessionWrapper(final SessionsContainer container, final String httpSessionId,
                              final String remoteQueueID) {
      this.container = Assert.notNull(container);
      this.remoteQueueID = Assert.notNull(remoteQueueID);
      this.parentSessionId = Assert.notNull(httpSessionId);
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
    public boolean isValid() {
      return container.getSession(remoteQueueID) != null;
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
      final SessionEndEvent event = new SessionEndEvent(this);

      for (final SessionEndListener sessionEndListener : sessionEndListeners) {
        sessionEndListener.onSessionEnd(event);
      }
    }

    @Override
    public boolean equals(final Object o) {
      if (this == o) return true;
      if (!(o instanceof HttpSessionWrapper)) return false;

      final HttpSessionWrapper that = (HttpSessionWrapper) o;

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
              "sessionId='" + sessionId + '\'' +
              ", remoteQueueID='" + remoteQueueID + '\'' +
              '}';
    }
  }
}
