package org.jboss.errai.bus.server.websocket.jsr356.weld.session;

import javax.naming.OperationNotSupportedException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.jboss.weld.context.http.HttpSessionContext;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.servlet.SessionHolder;

/**
 * Adapter for {@link javax.enterprise.context.SessionScoped}
 * 
 * @author Michel Werren
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class WeldSessionScopeAdapter implements SessionScopeAdapter {

  private static WeldSessionScopeAdapter instance;
  private final BeanManagerImpl beanManager;
  
  private ThreadLocal<HttpServletRequest> simulatedHttpRequest = new ThreadLocal<HttpServletRequest>(); 

  private WeldSessionScopeAdapter(BeanManagerImpl beanManager) {
    this.beanManager = beanManager;
  }

  public static WeldSessionScopeAdapter getInstance() {
    if (instance == null) {
      throw new IllegalStateException("Adapter not initialized!");
    }
    return instance;
  }

  public static void init(BeanManagerImpl beanManager) {
    if (instance == null) {
      instance = new WeldSessionScopeAdapter(beanManager);
    }
  }

  @Override
  public void activateContext(HttpSession httpSession) {
    HttpSessionContext sessionContext = beanManager.instance().select(HttpSessionContext.class).get();
    simulatedHttpRequest.set(new FakeHttpServletRequest(httpSession));
    SessionHolder.requestInitialized(simulatedHttpRequest.get());

    sessionContext.associate(simulatedHttpRequest.get());
    sessionContext.activate();
  }

  @Override
  public void activateContext() {
    throw new RuntimeException(new OperationNotSupportedException("Session scope must be referenced with HTTP session"));
  }

  @Override
  public void invalidateContext() {
    deactivateContext();
  }

  @Override
  public void deactivateContext() {
    HttpSessionContext sessionContext = beanManager.instance().select(HttpSessionContext.class).get();
    SessionHolder.clear();
    sessionContext.dissociate(simulatedHttpRequest.get());
  }
}
