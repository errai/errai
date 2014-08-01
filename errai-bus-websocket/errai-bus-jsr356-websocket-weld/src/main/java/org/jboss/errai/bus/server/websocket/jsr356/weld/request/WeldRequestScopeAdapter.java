package org.jboss.errai.bus.server.websocket.jsr356.weld.request;

import org.jboss.errai.bus.server.websocket.jsr356.weld.ScopeAdapter;
import org.jboss.errai.bus.server.websocket.jsr356.weld.SyncBeanStore;
import org.jboss.weld.context.bound.BoundRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Adapter for {@link javax.enterprise.context.RequestScoped}
 * 
 * @author Michel Werren
 */
public class WeldRequestScopeAdapter implements ScopeAdapter {

  private static final Logger LOGGER = LoggerFactory.getLogger(WeldRequestScopeAdapter.class.getName());

  private static WeldRequestScopeAdapter instance;

  private final BoundRequestContext requestContext;

  private static final ThreadLocal<SyncBeanStore> CURRENT_BEAN_STORE = new ThreadLocal<SyncBeanStore>();

  private WeldRequestScopeAdapter(BoundRequestContext requestContext) {
    this.requestContext = requestContext;
  }

  public static WeldRequestScopeAdapter getInstance() {
    if (instance == null) {
      throw new IllegalStateException("Adapter not initialized!");
    }
    return instance;
  }

  public static void init(BoundRequestContext context) {
    if (instance == null) {
      instance = new WeldRequestScopeAdapter(context);
    }
  }

  @Override
  public void activateContext() {
    if (!requestContext.isActive()) {
      final SyncBeanStore syncBeanStore = new SyncBeanStore();
      CURRENT_BEAN_STORE.set(syncBeanStore);
      if (requestContext.associate(syncBeanStore)) {
        requestContext.activate();
      }
      else {
        LOGGER.error("could not associate request context");
      }
    }
  }

  @Override
  public void invalidateContext() {
    requestContext.invalidate();
    deactivateContext();
  }

  @Override
  public void deactivateContext() {
    requestContext.deactivate();
    requestContext.dissociate(getCurrentBeanStore());
    CURRENT_BEAN_STORE.remove();
  }

  public static SyncBeanStore getCurrentBeanStore() {
    return CURRENT_BEAN_STORE.get();
  }
}
