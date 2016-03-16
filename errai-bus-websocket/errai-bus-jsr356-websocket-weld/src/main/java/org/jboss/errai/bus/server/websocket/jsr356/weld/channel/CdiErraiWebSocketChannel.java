package org.jboss.errai.bus.server.websocket.jsr356.weld.channel;

import javax.servlet.http.HttpSession;
import javax.websocket.Session;

import org.jboss.errai.bus.server.websocket.jsr356.channel.DefaultErraiWebSocketChannel;
import org.jboss.errai.bus.server.websocket.jsr356.weld.ScopeAdapter;
import org.jboss.errai.bus.server.websocket.jsr356.weld.conversation.ConversationScopeAdapter;
import org.jboss.errai.bus.server.websocket.jsr356.weld.conversation.ConversationState;
import org.jboss.errai.bus.server.websocket.jsr356.weld.conversation.WeldConversationScopeAdapter;
import org.jboss.errai.bus.server.websocket.jsr356.weld.request.WeldRequestScopeAdapter;
import org.jboss.errai.bus.server.websocket.jsr356.weld.session.SessionScopeAdapter;
import org.jboss.errai.bus.server.websocket.jsr356.weld.session.WeldSessionScopeAdapter;

/**
 * CDI version of {@link DefaultErraiWebSocketChannel}
 * 
 * @author Michel Werren
 */
public class CdiErraiWebSocketChannel extends DefaultErraiWebSocketChannel {

  private final SessionScopeAdapter sessionScopeAdapter;

  private final ScopeAdapter requestScopeAdapter;

  private final ConversationScopeAdapter conversationScopeAdapter;

  private final ConversationState conversationState = new ConversationState();

  public CdiErraiWebSocketChannel(Session session, HttpSession httpSession) {
    super(session, httpSession);

    sessionScopeAdapter = WeldSessionScopeAdapter.getInstance();
    requestScopeAdapter = WeldRequestScopeAdapter.getInstance();
    conversationScopeAdapter = WeldConversationScopeAdapter.getInstance();
  }

  public void doErraiMessage(final String message) {
    requestScopeAdapter.activateContext();
    try {
      sessionScopeAdapter.activateContext(httpSession);
      try {
        conversationScopeAdapter.activateContext(conversationState);
        try {
       	  super.doErraiMessage(message);
        } finally {
          conversationScopeAdapter.deactivateContext();
        }
      } finally {
        sessionScopeAdapter.deactivateContext();
      }
    } finally {
      // bean store should be destroyed after each message
      requestScopeAdapter.invalidateContext();
    }
  }

  /**
   * When this channel is closed, long running conversations have to be
   * invalidated.
   */
  public void onSessionClosed() {
    if (conversationState.isLongRunning()) {
      conversationScopeAdapter.activateContext(conversationState);
      conversationScopeAdapter.invalidateContext();
    }
  }
}
