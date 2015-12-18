/*
 * Copyright (C) 2012 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.bus.server.io.websockets;

import static org.slf4j.LoggerFactory.getLogger;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.annotation.concurrent.GuardedBy;

import org.jboss.errai.bus.client.api.QueueSession;
import org.jboss.errai.bus.server.api.ServerMessageBus;
import org.jboss.errai.bus.server.util.SecureHashUtil;
import org.slf4j.Logger;

/**
 * @author Mike Brock
 */
public final class WebSocketTokenManager {
  private static final Logger log = getLogger(WebSocketTokenManager.class);
  private WebSocketTokenManager() {
  }

  private static final String TOKEN_STORE = WebSocketTokenManager.class.getName() + ":Store";

  @SuppressWarnings({"unchecked", "SynchronizationOnLocalVariableOrMethodParameter"})
  @GuardedBy("session")
  public static String getNewOneTimeToken(final QueueSession session) {
    synchronized (session) {
      List tokenStore = session.getAttribute(List.class, TOKEN_STORE);
      if (tokenStore == null) {
        session.setAttribute(TOKEN_STORE, tokenStore = new ArrayList());
      }

      if (tokenStore.size() == 6) {
        log.warn("Client with session " + session  +
                " has too many active tokens. Removing oldest one and deactivating channel.");
        tokenStore.remove(0);
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
        final List tokenStore = session.getAttribute(List.class, TOKEN_STORE);
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
