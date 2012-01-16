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

import org.jboss.errai.bus.client.api.base.LaundryListProviderFactory;
import org.jboss.errai.bus.server.api.QueueSession;
import org.jboss.errai.bus.server.api.SessionEndEvent;
import org.jboss.errai.bus.server.api.SessionEndListener;
import org.jboss.errai.bus.server.api.SessionProvider;
import org.jboss.errai.bus.server.util.SecureHashUtil;
import org.jboss.errai.bus.server.util.ServerLaundryList;

/**
 * <tt>HttpSessionProvider</tt> implements <tt>SessionProvider</tt> as an <tt>HttpSession</tt>. It provides a getter
 * function to obtain the current session.
 */
public class HttpSessionProvider implements SessionProvider<HttpSession> {
  public static final String HTTP_SESS = "org.jboss.errai.QueueSessions";

  /**
   * Gets an instance of <tt>QueueSession</tt> using the external session reference given. If there is no available
   * session, a <tt>QueueSession</tt> is created
   *
   * @param externSessRef - the external session reference
   * @return an instance of <tt>QueueSession</tt>
   */
  public QueueSession getSession(HttpSession externSessRef, String remoteQueueID) {
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

  public static class SessionsContainer {
    /**
     * Share these attributes across all the sub-sessions
     */
    private Map<String, Object> sharedAttributes = new HashMap<String, Object>();
    private Map<String, QueueSession> queueSessions = new HashMap<String, QueueSession>();

    public QueueSession createSession(String httpSessionId, String remoteQueueId) {
      QueueSession qs = new HttpSessionWrapper(this, remoteQueueId, httpSessionId);
      queueSessions.put(remoteQueueId, qs);
      return qs;
    }

    public QueueSession getSession(String remoteQueueId) {
      return queueSessions.get(remoteQueueId);
    }

    public void remoteSession(String remoteQueueId) {
      queueSessions.remove(remoteQueueId);
    }
  }

  /**
   * <tt>HttpSessionWrapper</tt> provides an implementation of <tt>QueueSession</tt>. When trying to obtain a session,
   * If the reference does not have an HttpSession already, a new session is created using this wrapper class
   */
  public static class HttpSessionWrapper implements QueueSession, Serializable {
    private SessionsContainer container;
    private String sessionId;
    private String remoteQueueID;
    private boolean valid;
    private List<SessionEndListener> sessionEndListeners;

    public HttpSessionWrapper(SessionsContainer container, String httpSessionId, String remoteQueueID) {
      this.container = container;
      this.remoteQueueID = remoteQueueID;
      this.sessionId = SecureHashUtil.nextSecureHash("SHA-256", httpSessionId);
    }

    public String getSessionId() {
      return sessionId;
    }

    public boolean isValid() {
      return valid;
    }

    public boolean endSession() {
      valid = false;
      container.remoteSession(remoteQueueID);
      fireSessionEndListeners();
      return true;
    }

    public void setAttribute(String attribute, Object value) {
      container.sharedAttributes.put(attribute, value);
    }

    public <T> T getAttribute(Class<T> type, String attribute) {
      return (T) container.sharedAttributes.get(attribute);
    }

    public Collection<String> getAttributeNames() {
      return container.sharedAttributes.keySet();
    }

    public boolean hasAttribute(String attribute) {
      return container.sharedAttributes.containsKey(attribute);
    }

    public boolean removeAttribute(String attribute) {
      return container.sharedAttributes.remove(attribute) != null;
    }

    public void addSessionEndListener(SessionEndListener listener) {
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
  }
}
