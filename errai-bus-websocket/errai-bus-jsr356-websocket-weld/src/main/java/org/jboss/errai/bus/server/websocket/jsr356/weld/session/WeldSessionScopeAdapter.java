package org.jboss.errai.bus.server.websocket.jsr356.weld.session;

import org.jboss.errai.bus.server.websocket.jsr356.weld.SyncBeanStore;
import org.jboss.weld.context.bound.BoundSessionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.OperationNotSupportedException;
import javax.servlet.http.HttpSession;

/**
 * Adapter for {@link javax.enterprise.context.SessionScoped}
 * 
 * @author Michel Werren
 */
public class WeldSessionScopeAdapter implements SessionScopeAdapter {

  private static final String BEAN_STORE_SESSION_ATTR_NAME = "erraiBeanStore";

  private static final Logger LOGGER = LoggerFactory.getLogger(WeldSessionScopeAdapter.class.getName());

  private static final ThreadLocal<SyncBeanStore> CURRENT_BEAN_STORE = new ThreadLocal<SyncBeanStore>();

  private static WeldSessionScopeAdapter instance;

  private final BoundSessionContext sessionContext;

  private WeldSessionScopeAdapter(BoundSessionContext sessionContext) {
    this.sessionContext = sessionContext;
  }

  public static WeldSessionScopeAdapter getInstance() {
    if (instance == null) {
      throw new IllegalStateException("Adapter not initialized!");
    }
    return instance;
  }

  public static void init(BoundSessionContext context) {
    if (instance == null) {
      instance = new WeldSessionScopeAdapter(context);
    }
  }

  @Override
  public void activateContext(HttpSession httpSession) {
    if (!sessionContext.isActive()) {
      final SyncBeanStore beanStore = getOrCreateBeanStore(httpSession);
      if (sessionContext.associate(beanStore)) {
        CURRENT_BEAN_STORE.set(beanStore);
        sessionContext.activate();
      }
      else {
        LOGGER.error("could not associate session context for session: {}", httpSession.getId());
      }
    }
  }

  /**
   * Extract the existing
   * {@link org.jboss.errai.bus.server.websocket.jsr356.weld.SyncBeanStore} from
   * the {@link javax.servlet.http.HttpSession}. If no exists, then a new one
   * will be created.
   * 
   * @param httpSession
   * @return
   */
  private SyncBeanStore getOrCreateBeanStore(HttpSession httpSession) {
    final Object beanStore = httpSession.getAttribute(BEAN_STORE_SESSION_ATTR_NAME);
    if (beanStore != null) {
      return (SyncBeanStore) beanStore;
    }
    else {
      final SyncBeanStore newBeanStore = new SyncBeanStore();
      httpSession.setAttribute(BEAN_STORE_SESSION_ATTR_NAME, newBeanStore);
      return newBeanStore;
    }
  }

  @Override
  public void activateContext() {
    throw new RuntimeException(new OperationNotSupportedException("session scope must be referenced with http session"));
  }

  /**
   * used for relation to {@link org.jboss.weld.context.ConversationContext}
   * beans
   * 
   * @return session bean store of current Thread
   */
  public static SyncBeanStore getCurrentBeanStore() {
    return CURRENT_BEAN_STORE.get();
  }

  @Override
  public void invalidateContext() {
    sessionContext.invalidate();
    deactivateContext();
  }

  @Override
  public void deactivateContext() {
    sessionContext.deactivate();
    sessionContext.dissociate(getCurrentBeanStore());
    CURRENT_BEAN_STORE.remove();
  }
}
