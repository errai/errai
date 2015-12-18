package org.jboss.errai.bus.server.websocket.jsr356.weld;

/**
 * Adapter interface for CDI builtin scope management.
 * 
 * @author Michel Werren
 */
public interface ScopeAdapter {

  /**
   * Associate and activate the context for the current {@link Thread}
   */
  public void activateContext();

  /**
   * Marks the currently bounded bean store as to be invalidated,
   */
  public void invalidateContext();

  /**
   * After this invocation, this context is no more in use for this
   * {@link Thread}
   */
  public void deactivateContext();
}
