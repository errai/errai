package org.jboss.errai.cdi.server;

import org.jboss.errai.bus.server.util.SessionContext;
import org.jboss.weld.context.beanstore.AttributeBeanStore;
import org.jboss.weld.context.beanstore.NamingScheme;
import org.jboss.weld.context.beanstore.http.AbstractSessionBeanStore;

import javax.servlet.http.HttpSession;
import java.util.Collection;

/**
 * @author Mike Brock .
 */
public class QueueSessionBeanStore extends AttributeBeanStore {

    SessionContext ctx;

    public QueueSessionBeanStore(NamingScheme namingScheme, SessionContext ctx) {
        super(namingScheme);
        this.ctx = ctx;
    }

    @Override
    protected Object getAttribute(String prefixedId) {
        return ctx.getAttribute(Object.class, prefixedId);
    }

    @Override
    protected void removeAttribute(String prefixedId) {
        ctx.removeAttribute(prefixedId);
    }

    @Override
    protected Collection<String> getAttributeNames() {
        return ctx.getAttributeNames();
    }

    @Override
    protected void setAttribute(String prefixedId, Object instance) {
        ctx.setAttribute(prefixedId, instance);
    }
}
