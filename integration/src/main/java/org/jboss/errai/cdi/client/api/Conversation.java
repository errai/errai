/*
 * Copyright 2009 JBoss, a divison Red Hat, Inc
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
package org.jboss.errai.cdi.client.api;

import org.jboss.errai.bus.client.ErraiBus;
import org.jboss.errai.bus.client.api.base.MessageBuilder;

/**
 * Client side conversation handle.
 * Conversations and messages correlate through the subject.<br/>
 * Handles can be created through the
 * {@link org.jboss.errai.cdi.client.api.CDI#createConversation(String)} client interface.
 *
 * @see CDI
 *
 * @author: Heiko Braun <hbraun@redhat.com>
 * @date: Oct 5, 2010
 */
public class Conversation {

  private String id;
  private String subject;

  private boolean hasBegun, hasEnded = false;

  Conversation(String id, String subject) {
    this.id = id;
    this.subject = subject;
  }

  public String getSubject() {
    return subject;
  }

  public String getId() {
    return id;
  }

  public void begin() {
    assertBegun();

    // register client side conversation state globally
    // so that the message builder picks it
    // Once that's done we don't need to wrap the message builder calls

    CDI.getActiveConversations().put(id, this);
    hasBegun = true;
  }

  public boolean isActive() {
    return hasBegun;
  }

  /**
   * Explicitly end a conversation
   */
  public void end() {
    assertEnded();

    MessageBuilder.createMessage().toSubject("cdi.conversation:Manager,conversation=" + id).command("end")
        .with("cdi.conversation.id", id).with("cdi.internal", true) // will be excluded in interceptor
        .done().sendNowWith(ErraiBus.get());

    CDI.getActiveConversations().remove(id);

    hasEnded = true;
  }

  public boolean hasEnded() {
    // might be a server side component ends the conversation
    // in that case this instance needs to be terminated to reflect the
    // server side state change
    return hasEnded;
  }

  private void assertEnded() {
    if (hasEnded)
      throw new IllegalStateException("Converation already ended: " + id);
  }

  private void assertBegun() {
    if (hasBegun)
      throw new IllegalStateException("Converation already begun: " + id);
  }

  public void reset() {
    id = CDI.generateId();
    hasBegun = false;
    hasEnded = false;
  }
}
