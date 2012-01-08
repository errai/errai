/*
 * Copyright 2011 JBoss, by Red Hat, Inc
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

package org.jboss.errai.cdi.server.events;

/**
 * @author Mike Brock
 */
public class EventConversationContext {
  private static final ThreadLocal<Context> threadLocalConversationContext
          = new ThreadLocal<Context>();

  public static void activate(Object o, String session) {
    threadLocalConversationContext.set(new Context(o, session));
  }

  public static void activate(Object o) {
    threadLocalConversationContext.set(new Context(o, null));
  }

  public static void deactivate() {
    threadLocalConversationContext.remove();
  }

  public static Context get() {
    return threadLocalConversationContext.get();
  }

  public static boolean isEventObjectInContext(Object event) {
    Context ctx = get();
    return ctx != null && event == ctx.getEventObject();
  }

  public static class Context {
    private final Object eventObject;
    private final String session;

    public Context(Object eventObject, String session) {
      this.eventObject = eventObject;
      this.session = session;
    }

    public Object getEventObject() {
      return eventObject;
    }

    public String getSession() {
      return session;
    }
  }
}
