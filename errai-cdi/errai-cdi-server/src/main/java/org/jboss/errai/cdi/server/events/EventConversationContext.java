/*
 * Copyright (C) 2011 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.cdi.server.events;

import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.bus.client.api.QueueSession;
import org.jboss.errai.cdi.server.CDIServerUtil;

/**
 * An context control helper for handling Errai conversations within CDI. Internally, this class uses a 
 * {@link ThreadLocal} to store its state. Thus, conversational scopes within Errai are dependent upon single-threaded
 * dispatch within the CDI container. 
 * 
 * @author Mike Brock
 */
public class EventConversationContext {
  private static final ThreadLocal<Context> threadLocalConversationContext
          = new ThreadLocal<Context>();

  /**
   * Activate the conversation scope. If there is a currently active scope, it is replaced with this new scope.
   * 
   * @param o reference to the event object which is opening the scope.
   * @param session the bus session ID of the scope.
   */
  public static void activate(Object o, QueueSession session) {
    threadLocalConversationContext.set(new Context(o, session));
  }

  /**
   * Activate a conversations scope. If there is a currently active scope, it is replaced with this new scope.
   * @param session the bus session ID of the scope.
   */
  public static void activate(QueueSession session) {
    threadLocalConversationContext.set(new Context(null, session));
  }

  /**
   * Convenience method to active the conversation scope from a Errai {@code Message} object, by extracting the
   * session ID referenced within.
   *
   * @param message An Errai message.
   */
  public static void activate(Message message) {
    activate(CDIServerUtil.getSession(message));
  }

  /**
   * Deactivate any current scope.
   */
  public static void deactivate() {
    threadLocalConversationContext.remove();
  }

  /**
   * Get the current scope.
   * @return isntance of the context.
   */
  public static Context get() {
    return threadLocalConversationContext.get();
  }

  /**
   * Checks to see if the specified event reference is the same event reference that initiated the conversation.
   * @param event the event instance
   * @return boolean indicating if the event object is the same instance
   */
  public static boolean isEventObjectInContext(Object event) {
    Context ctx = get();
    return ctx != null && event == ctx.getEventObject();
  }

  public static class Context {
    private final Object eventObject;
    private final QueueSession session;

    public Context(Object eventObject, QueueSession session) {
      this.eventObject = eventObject;
      this.session = session;
    }

    public Object getEventObject() {
      return eventObject;
    }

    public String getSessionId() {
      return session.getSessionId();
    }

    public QueueSession getSession() {
      return session;
    }
  }
}
