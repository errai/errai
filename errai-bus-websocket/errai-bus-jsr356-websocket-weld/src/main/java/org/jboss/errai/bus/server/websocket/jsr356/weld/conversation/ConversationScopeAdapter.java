package org.jboss.errai.bus.server.websocket.jsr356.weld.conversation;

import org.jboss.errai.bus.server.websocket.jsr356.weld.ScopeAdapter;

/**
 * @author Michel Werren
 */
public interface ConversationScopeAdapter extends ScopeAdapter {

  /**
   * Lookup id of an existing conversation id and use it. If it's no long running
   * conversation active, a transient will be started.
   * 
   * @param conversationState
   */
  public void activateContext(ConversationState conversationState);
}
