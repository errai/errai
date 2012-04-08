package org.jboss.errai.bus.server.io.websockets;

import org.jboss.errai.bus.client.api.QueueSession;
import org.jboss.errai.bus.server.util.SecureHashUtil;

import javax.annotation.concurrent.GuardedBy;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Mike Brock
 */
public final class WebSocketTokenManager {
  private WebSocketTokenManager() {
  }

  private static final String TOKEN_STORE = WebSocketTokenManager.class.getName() + ":Store";

  @GuardedBy("session")
  public static String getNewOneTimeToken(final QueueSession session) {
    synchronized (session) {
      Set tokenStore = session.getAttribute(Set.class, TOKEN_STORE);
      if (tokenStore == null) {
        session.setAttribute(TOKEN_STORE, tokenStore = new HashSet());
      }

      if (tokenStore.size() > 6) {
        throw new RuntimeException("too many active tokens!");
      }

      final String oneTimeToken = SecureHashUtil.nextSecureHash("SHA-1");
      tokenStore.add(oneTimeToken);

      return oneTimeToken;
    }
  }

  @GuardedBy("session")
  public static boolean verifyOneTimeToken(final QueueSession session, final String token) {
    synchronized (session) {
      final boolean tokenRemoved;
      if (session.hasAttribute(TOKEN_STORE)) {
        final Set tokenStore = session.getAttribute(Set.class, TOKEN_STORE);
        tokenRemoved = tokenStore.remove(token);
        if (tokenStore.isEmpty()) {
          session.removeAttribute(TOKEN_STORE);
        }
      }
      else {
        tokenRemoved = false;
      }
      return tokenRemoved;

    }
  }
}
