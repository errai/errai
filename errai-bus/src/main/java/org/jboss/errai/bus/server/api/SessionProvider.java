/*
 * Copyright 2010 JBoss, a divison Red Hat, Inc
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

/**
 * This interface, <tt>SessionProvider</tt>, is a template for creating a session provider based on the type of
 * session specified
 */
public interface SessionProvider<T> {

  /**
   * Gets an instance of <tt>QueueSession</tt> using the external session reference given.
   *
   * @param externSessRef - the external session reference
   * @return an instance of <tt>QueueSession</tt>
   */
  public QueueSession getSession(T externSessRef, String remoteQueueId);
}
