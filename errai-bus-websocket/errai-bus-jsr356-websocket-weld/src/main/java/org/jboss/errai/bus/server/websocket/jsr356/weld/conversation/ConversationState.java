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
