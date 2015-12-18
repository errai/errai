/*
 * Copyright (C) 2015 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.bus.server.websocket.jsr356.weld.conversation;

import java.util.Map;

import javax.naming.OperationNotSupportedException;

import org.jboss.errai.bus.server.websocket.jsr356.weld.request.WeldRequestScopeAdapter;
import org.jboss.errai.bus.server.websocket.jsr356.weld.session.WeldSessionScopeAdapter;
import org.jboss.weld.context.ManagedConversation;
import org.jboss.weld.context.bound.BoundConversationContext;
import org.jboss.weld.context.bound.BoundRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Adapter for {@link javax.enterprise.context.ConversationScoped}
 * 
 * @authorMichel Werren
 */
public class WeldConversationScopeAdapter implements ConversationScopeAdapter {

  private static final Logger LOGGER = LoggerFactory.getLogger(WeldConversationScopeAdapter.class.getName());

  private final BoundConversationContext boundConversationContext;

  private static final ThreadLocal<BoundRequest> CURRENT_BOUND_REQUEST = new ThreadLocal<BoundRequest>();

  private static final ThreadLocal<ConversationState> CURRENT_CONVERSATION_STATE = new ThreadLocal<ConversationState>();

  private static WeldConversationScopeAdapter instance;

  private WeldConversationScopeAdapter(BoundConversationContext boundConversationContext) {
    this.boundConversationContext = boundConversationContext;
  }

  public static WeldConversationScopeAdapter getInstance() {
    if (instance == null) {
      throw new IllegalStateException("Adapter not initialized!");
    }
    return instance;
  }

  public static void init(BoundConversationContext context) {
    if (instance == null) {
      instance = new WeldConversationScopeAdapter(context);
    }
  }

  private void associateContext() {
    final BoundRequest storage = new BoundRequest() {
      @Override
      public Map<String, Object> getRequestMap() {
        return WeldRequestScopeAdapter.getCurrentBeanStore();
      }

      @Override
      public Map<String, Object> getSessionMap(boolean create) {
        return WeldSessionScopeAdapter.getInstance().getCurrentBeanStore();
      }
    };
    final boolean successful = boundConversationContext.associate(storage);
    if (!successful) {
      LOGGER.error("could not attach conversation storage");
    }
    CURRENT_BOUND_REQUEST.set(storage);
  }

  @Override
  public void activateContext() {
    throw new RuntimeException(new OperationNotSupportedException("Conversation state needed"));
  }

  @Override
  public void activateContext(ConversationState conversationState) {
    CURRENT_CONVERSATION_STATE.set(conversationState);
    if (!boundConversationContext.isActive()) {
      associateContext();
      if (conversationState.isLongRunning()) {
        boundConversationContext.activate(conversationState.getConversationId());
      }
      else {
        boundConversationContext.activate();
      }
    }
  }

  @Override
  public void invalidateContext() {
    boundConversationContext.invalidate();
    deactivateContext();
  }

  @Override
  public void deactivateContext() {
    final ConversationState conversationState = CURRENT_CONVERSATION_STATE.get();
    final ManagedConversation currentConversation = boundConversationContext.getCurrentConversation();
    if (!currentConversation.isTransient() && !conversationState.isLongRunning()) {
      startLongRunningConversation(conversationState, currentConversation);
    }
    else if (currentConversation.isTransient() && conversationState.isLongRunning()) {
      endLongRunningConversation(conversationState);
    }
    else if (currentConversation.getId() != null
            && !currentConversation.getId().equals(conversationState.getConversationId())) {
      LOGGER.warn("current conversation id: {} and registered: {}. There shouldn't be two activated conversations",
              currentConversation.getId(), conversationState.getConversationId());
    }
    boundConversationContext.deactivate();
    boundConversationContext.dissociate(CURRENT_BOUND_REQUEST.get());
    CURRENT_BOUND_REQUEST.remove();
    CURRENT_CONVERSATION_STATE.remove();
  }

  /**
   * Tasks when a long running conversation has started during this event.
   * 
   * @param conversationState
   * @param managedConversation
   */
  private void startLongRunningConversation(ConversationState conversationState, ManagedConversation managedConversation) {
    conversationState.registerLongRunningConversaton(managedConversation.getId());
    LOGGER.trace("register long running conversation: {}", managedConversation.getId());
  }

  /**
   * Tasks when a long running conversation has ended during this event.
   * 
   * @param conversationState
   */
  private void endLongRunningConversation(ConversationState conversationState) {
    conversationState.removeLongRunningConversation();
    boundConversationContext.invalidate();
    LOGGER.trace("end long running conversation id: {}", conversationState.getConversationId());
  }
}
