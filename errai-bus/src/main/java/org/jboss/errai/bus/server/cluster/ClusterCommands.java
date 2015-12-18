/*
 * Copyright (C) 2012 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.bus.server.cluster;

/**
 * @author Mike Brock
 */
public enum ClusterCommands {
  /**
   * The <tt>WhoHandles</tt> verb is a broadcast message to the cluster that asks all buses who is
   * responsible for handling the specified session.
   */
  WhoHandles,

  /**
   * The <tt>NotifyOwner</tt> verb is sent in response to a <tt>WhoHandles</tt> broadcast on a
   * point-to-point basis, directly back to the sender of the <tt>WhoHandles</tt> to notify that
   * the bus is responsible for handling the request session.
   */
  NotifyOwner,

  /**
   * The <tt>MessageForward</tt> verb is used in a point-to-point or global message which encapsulates
   * a message payload to be forwarded to specific or all buses.
   */
  MessageForward,

  /**
   * The <tt>InvalidRoute</tt> verb is used in a point-to-point message to indicate that the bus was
   * forwarded a message which it is not or no longer responsible for.
   */
  InvalidRoute
}
