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

package org.jboss.errai.bus.server.io;

import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.bus.client.api.messaging.MessageCallback;
import org.jboss.errai.bus.client.api.base.MessageDeliveryFailure;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.unmodifiableSet;

/**
 * <tt>RemoteServiceCallback</tt> implements callback functionality for a remote service. It invokes the callback
 * functions for all endpoints specified
 */
public class RemoteServiceCallback implements MessageCallback {
  private final Map<String, MessageCallback> endpoints;

  /**
   * Initializes the <tt>RemoteServiceCallback</tt> with a set of endpoints and their callback functions
   *
   * @param endpoints - Map of endpoints to their callback function
   */
  public RemoteServiceCallback(Map<String, MessageCallback> endpoints) {
    this.endpoints = Collections.unmodifiableMap(endpoints);
  }

  /**
   * Invokes all callback functions that can be associated to the <tt>message</tt>
   *
   * @param message - the message in question
   */
  public void callback(Message message) {
    if (!endpoints.containsKey(message.getCommandType())) {
      throw new MessageDeliveryFailure("no such endpoint '" + message.getCommandType() + "' in service: " + message.getSubject());
    }
    endpoints.get(message.getCommandType()).callback(message);
  }

  public Set<String> getEndpoints() {
    return unmodifiableSet(endpoints.keySet());
  }
}
