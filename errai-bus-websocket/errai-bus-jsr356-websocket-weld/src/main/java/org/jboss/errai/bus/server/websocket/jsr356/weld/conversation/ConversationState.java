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

import org.apache.commons.lang3.StringUtils;


/**
 * Running state of {@link javax.enterprise.context.Conversation}.
 * 
 * @author Michel Werren
 */
public class ConversationState {

  private String conversationId;

  public Boolean isLongRunning() {
    return !StringUtils.isEmpty(conversationId);
  }

  public String getConversationId() {
    return conversationId;
  }

  public void registerLongRunningConversaton(String conversationId) {
    this.conversationId = conversationId;
  }

  public void removeLongRunningConversation() {
    conversationId = null;
  }

}
