/*
 * Copyright (C) 2013 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.bus.client.api.messaging;

/**
 * The <tt>RequestDispatcher</tt> interface provides a way to create a message delivery system into the bus
 */
public interface RequestDispatcher {

  /**
   * Dispatches a message to all global listeners on the bus
   *
   * @param message - a message to dispatch globally
   * @throws Exception throws an InterruptedException specifically, if the thread is interrupted while trying
   *                   to offer a message to the worker queue. This isn't specifically exposed here due
   *                   to the fact that InterruptedException is not exposed to the GWT client library.
   */
  public void dispatchGlobal(Message message) throws Exception;

  /**
   * Dispatches a message to a single receiver on the bus
   *
   * @param message - a message to dispatch
   * @throws Exception throws an InterruptedException specifically, if the thread is interrupted while trying
   *                   to offer a message to the worker queue. This isn't specifically exposed here due
   *                   to the fact that InterruptedException is not exposed to the GWT client library.
   */
  public void dispatch(Message message) throws Exception;
}
