package org.jboss.errai.bus.server.websocket.jsr356.weld.session;

import org.jboss.errai.bus.server.websocket.jsr356.weld.ScopeAdapter;

import javax.servlet.http.HttpSession;

/**
 * @author Michel Werren
 */
public interface SessionScopeAdapter extends ScopeAdapter {

  /**
   * Activate context for {@link javax.enterprise.context.SessionScoped} beans.
   * 
   * @param httpSession
   */
  public void activateContext(HttpSession httpSession);
}
