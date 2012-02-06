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

package org.jboss.errai.cdi.server;

import javax.servlet.http.HttpServletRequest;

import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.server.api.QueueSession;
import org.jboss.weld.Container;
import org.jboss.weld.context.http.HttpRequestContext;
import org.jboss.weld.context.http.HttpSessionContext;

/**
 * @author Mike Brock
 */
public class ScopeUtil {
  public static QueueSession getSessionFrom(Message message) {
    return message.getResource(QueueSession.class, "Session");
  }

  public static HttpSessionContext getSessionContext(Message message) {
    HttpSessionContext context = getSessionFrom(message)
            .getAttribute(HttpSessionContext.class, HttpSessionContext.class.getName());

    if (context == null) {
      getSessionFrom(message)
              .setAttribute(HttpSessionContext.class.getName(),
                      context = Container.instance().deploymentManager().instance()
                              .select(HttpSessionContext.class).get());
    }
    return context;
  }

  public static HttpRequestContext getRequestContext(Message message) {
    HttpRequestContext context = getSessionFrom(message)
            .getAttribute(HttpRequestContext.class, HttpRequestContext.class.getName());

    if (context == null) {
      getSessionFrom(message)
              .setAttribute(HttpRequestContext.class.getName(),
                      context = Container.instance().deploymentManager().instance()
                              .select(HttpRequestContext.class).get());
    }

    return context;
  }

  public static void associateSessionContext(Message message) {
    HttpSessionContext ctx = getSessionContext(message);
    ctx.associate(getHttpServletRequest(message));
  }

  public static void associateRequestContext(Message message) {
    HttpRequestContext ctx = getRequestContext(message);
    ctx.associate(getHttpServletRequest(message));
  }

  public static void disassociateSessionContext(Message message) {
    getSessionContext(message).dissociate(getHttpServletRequest(message));
  }

  public static void disassociateRequestContext(Message message) {
    getRequestContext(message).dissociate(getHttpServletRequest(message));
  }

  private static HttpServletRequest getHttpServletRequest(Message message) {
    return message.getResource(HttpServletRequest.class,
            HttpServletRequest.class.getName());
  }
}
