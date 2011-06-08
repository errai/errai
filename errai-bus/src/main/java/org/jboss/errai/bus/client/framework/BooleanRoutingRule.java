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

package org.jboss.errai.bus.client.framework;

import org.jboss.errai.bus.client.api.Message;

/**
 * A RoutingRule that is called by the bus before routing any message.
 */
public interface BooleanRoutingRule {
  /**
   * This method is called in order determine whether or not a message should continue to be routed based on
   * whatever rules have been implemented in the <tt>decision()</tt> method.  If the method returns <tt>false</tt>,
   * the message will not be routed.  If it returns <tt>true</tt>, routing continues normally.
   *
   * @param message - the command message to be inspected.
   * @return boolean - indicating whether or not the message should be routed.
   */
  public boolean decision(Message message);
}
