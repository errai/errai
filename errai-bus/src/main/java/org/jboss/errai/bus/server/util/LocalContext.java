/*
 * Copyright 2009 JBoss, a divison Red Hat, Inc
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

package org.jboss.errai.bus.server.util;

import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.server.api.QueueSession;

import java.util.HashMap;
import java.util.Map;


public class LocalContext {
    private String context;
    private SubContext ctx;
    private QueueSession session;

    public static LocalContext get(Message message) {
        return new LocalContext(message.getSubject(), message.getResource(QueueSession.class, "Session"));
    }

    private LocalContext(String context, QueueSession session) {
        if (session == null) {
            throw new RuntimeException("no session");
        }
        this.context = createContextString(session.getSessionId() + "//" +  context);
        this.session = session;
        this.ctx = getLocalContext();
    }

    public void setAttribute(Enum key, Object value) {
        ctx.setAttribute(key.toString(), value);
    }

    public <T> T getAttribute(Class<T> type, Enum key) {
        return ctx.getAttribute(type, key.toString());
    }

    public void setAttribute(Class typeIndexed, Object value) {
        ctx.setAttribute(typeIndexed.getName(), value);
    }

    public <T> T getAttribute(Class<T> type, Class typeIndexed) {
        return ctx.getAttribute(type, typeIndexed.getName());
    }

    public <T> T getAttribute(Class<T> type) {
        return getAttribute(type, type);
    }


    public void setAttribute(String param, Object value) {
        ctx.setAttribute(param, value);
    }

    public <T> T getAttribute(Class<T> type, String param) {
        return ctx.getAttribute(type, param);
    }

    public QueueSession getSession() {
        return session;
    }

    public void destroy() {
        session.removeAttribute(context);
    }

    private SubContext getLocalContext() {
        synchronized (this) {
            SubContext ctx = session.getAttribute(SubContext.class, context);
            if (ctx == null) {
                session.setAttribute(context, ctx = new SubContext());
            }
            return ctx;
        }
    }

     private static String createContextString(String context) {
        return "LocalContext://" + context;
    }

    private static final class SubContext {
        private Map<String, Object> contextAttributes = new HashMap<String, Object>();

        public void setAttribute(String attribute, Object value) {
            contextAttributes.put(attribute, value);
        }

        public <T> T getAttribute(Class<T> type, String attribute) {
            return (T) contextAttributes.get(attribute);
        }

        public boolean hasAttribute(String attribute) {
            return contextAttributes.containsKey(attribute);
        }

        public void removeAttribute(String attribute) {
            contextAttributes.remove(attribute);
        }
    }
}
