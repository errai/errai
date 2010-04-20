package org.jboss.errai.bus.server;

import javax.servlet.http.HttpSession;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * <tt>HttpSessionProvider</tt> implements <tt>SessionProvider</tt> as an <tt>HttpSession</tt>. It provides a getter
 * function to obtain the current session.
 */
public class HttpSessionProvider implements SessionProvider<HttpSession> {
    private static final String HTTP_SESS = "org.jboss.errai.QueueSessions";

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
            sc.addSession(remoteQueueID, qs = new HttpSessionWrapper(externSessRef.getId(), remoteQueueID));
        }

        return qs;
    }

    public static class SessionsContainer {
        private Map<String, QueueSession> queueSessions = new HashMap<String, QueueSession>();

        public void addSession(String remoteQueueId, QueueSession session) {
            queueSessions.put(remoteQueueId, session);
        }

        public QueueSession getSession(String remoteQueueId) {
            return queueSessions.get(remoteQueueId);
        }
    }

    /**
     * <tt>HttpSessionWrapper</tt> provides an implementation of <tt>QueueSession</tt>. When trying to obtain a session,
     * If the reference does not have an HttpSession already, a new session is created using this wrapper class
     */
    public static class HttpSessionWrapper implements QueueSession, Serializable {
        private Map<String, Object> attributes = new HashMap<String, Object>();
        private String sessionId;
        private boolean valid;

        public HttpSessionWrapper(String sessionId, String remoteQueueID) {
            this.sessionId = remoteQueueID + "@" + sessionId;
        }

        public String getSessionId() {
            return sessionId;
        }

        public boolean isValid() {
            return valid;
        }

        public boolean endSession() {
            valid = false;
            return true;
        }

        public void setAttribute(String attribute, Object value) {
            attributes.put(attribute, value);
        }

        public <T> T getAttribute(Class<T> type, String attribute) {
            return (T) attributes.get(attribute);
        }

        public boolean hasAttribute(String attribute) {
            return attributes.containsKey(attribute);
        }

        public void removeAttribute(String attribute) {
            attributes.remove(attribute);
        }
    }
}
