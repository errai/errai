package org.jboss.errai.cdi.server;

import java.lang.annotation.Annotation;

import javax.enterprise.context.SessionScoped;

import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.server.api.QueueSession;
import org.jboss.weld.context.AbstractManagedContext;
import org.jboss.weld.context.BoundContext;
import org.jboss.weld.context.SessionContext;
import org.jboss.weld.context.beanstore.BeanStore;
import org.jboss.weld.context.beanstore.NamingScheme;
import org.jboss.weld.context.beanstore.SimpleNamingScheme;

/**
 * @author Mike Brock .
 */
public class QueueSessionContext extends AbstractManagedContext implements SessionContext, BoundContext<Message> {
  private final String TOKEN_NAME = QueueSessionContext.class.getName();
  private final String BEAN_STORE_ID = QueueSessionBeanStore.class.getName();

  private static ThreadLocal<org.jboss.errai.bus.server.util.SessionContext> sessionContextThreadLocal = new ThreadLocal<org.jboss.errai.bus.server.util.SessionContext>();

  private final NamingScheme namingScheme = new SimpleNamingScheme(QueueSessionContext.class.getName());

  public QueueSessionContext() {
    super(true);
  }

  public boolean associate(Message storage) {
    org.jboss.errai.bus.server.util.SessionContext ctx = org.jboss.errai.bus.server.util.SessionContext.get(storage);

    sessionContextThreadLocal.set(ctx);

    if (ctx.getAttribute(Object.class, TOKEN_NAME) == null) {
      ctx.setAttribute(TOKEN_NAME, TOKEN_NAME);
      ctx.setAttribute(BEAN_STORE_ID, new QueueSessionBeanStore(namingScheme, ctx));

      return true;
    } else {
      return false;
    }
  }

  public boolean dissociate(Message storage) {
    org.jboss.errai.bus.server.util.SessionContext ctx = org.jboss.errai.bus.server.util.SessionContext.get(storage);

    if (ctx.getAttribute(Object.class, TOKEN_NAME) == null) {
      ctx.removeAttribute(TOKEN_NAME);
      ctx.removeAttribute(BEAN_STORE_ID);

      return true;
    } else {
      return false;
    }
  }

  @Override
  protected BeanStore getBeanStore() {
    return sessionContextThreadLocal.get().getAttribute(BeanStore.class, BEAN_STORE_ID);
  }

  public Class<? extends Annotation> getScope() {
    return SessionScoped.class;
  }

  @Override
  public boolean isActive() {
    return true;
  }
}
