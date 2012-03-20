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
package org.jboss.errai.bus.server.api;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.api.QueueSession;

/**
 * This utility provides access to {@link Message} resources otherwise not visible to RPC endpoints. It can be used to
 * gain access to HTTP session and servlet request objects.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class RpcContext {
  private static final ThreadLocal<HttpSession> threadLocalHttpSession = new ThreadLocal<HttpSession>();
  private static final ThreadLocal<ServletRequest> threadLocalServletRequest = new ThreadLocal<ServletRequest>();

  /**
   * Reads resources from the provided {@link Message} and stores them in {@link ThreadLocal}s.
   * 
   * @param message
   */
  public static void set(Message message) {
    QueueSession queueSession = message.getResource(QueueSession.class, "Session");
    if (queueSession != null) {
      HttpSession session =
            queueSession.getAttribute(HttpSession.class, HttpSession.class.getName());

      if (session != null) {
        threadLocalHttpSession.set(session);
      }
    }

    HttpServletRequest request = message.getResource(HttpServletRequest.class, HttpServletRequest.class.getName());
    if (request != null) {
      threadLocalServletRequest.set(request);
    }
  }

  /**
   * Removes the resources associated with the current thread.
   */
  public static void remove() {
    threadLocalHttpSession.remove();
    threadLocalServletRequest.remove();
  }
  
  /**
   * @return the HTTP session object associated with this {@see Thread}
   */
  public static HttpSession getHttpSession() {
    return threadLocalHttpSession.get();
  }

  /**
   * @return the servlet request instance associated with this {@see Thread}
   */
  public static ServletRequest getServletRequest() {
    return threadLocalServletRequest.get();
  }
}