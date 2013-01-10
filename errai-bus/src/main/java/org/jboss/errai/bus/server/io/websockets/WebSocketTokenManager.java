/*
 * Copyright 2012 JBoss, by Red Hat, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

  @SuppressWarnings({"unchecked", "SynchronizationOnLocalVariableOrMethodParameter"})
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

      final String oneTimeToken = SecureHashUtil.nextSecureHash("SHA-256");
      tokenStore.add(oneTimeToken);

      return oneTimeToken;
    }
  }

  @SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
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
