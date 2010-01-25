package org.jboss.errai.bus.server;

import org.jboss.errai.bus.server.QueueSession;

import javax.servlet.http.HttpSession;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class HttpSessionProvider implements SessionProvider<HttpSession> {
    private static final String HTTP_SESS = "org.jboss.errai.QueueSession";

    public QueueSession getSession(HttpSession externSessRef) {
        QueueSession sess = (QueueSession) externSessRef.getAttribute(HTTP_SESS);
        if (sess == null) {
            externSessRef.setAttribute(HTTP_SESS, sess = new HttpSessionWrapper(externSessRef.getId()));
        }
        return sess;
    }

    public static class HttpSessionWrapper implements QueueSession, Serializable {
        private Map<String, Object> attributes = new HashMap<String, Object>();
        private String sessionId;
        private boolean valid;

        public HttpSessionWrapper(String sessionId) {
            this.sessionId = sessionId;
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
