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

package org.jboss.errai.bus.server.security.auth;

import org.jboss.errai.bus.client.api.Message;

/**
 * Defines interface for performing authentication on the bus.
 */
public interface AuthenticationAdapter {
  /**
   * Performs a security challenge against any existing credentials (or lack thereof) in the current
   * <tt>CommandMessage</tt>
   *
   * @param message -
   */
  public void challenge(Message message);

  /**
   * Returns true if the current <tt>CommandMessage</tt> has an authentication sessino.
   *
   * @param message
   * @return
   */
  public boolean isAuthenticated(Message message);

  /**
   * Terminates any session associated with the specified <tt>CommandMessage</tt>
   *
   * @param message
   * @return
   */
  public boolean endSession(Message message);
}
