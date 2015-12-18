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

package org.jboss.errai.bus.server.service;

import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.bus.client.api.messaging.RequestDispatcher;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Mike Brock
 */
public class RequestDispatcherProxy implements RequestDispatcher {
  private List<Message> heldGlobalMessages = new ArrayList<Message>();
  private List<Message> heldMessages = new ArrayList<Message>();
  private RequestDispatcher proxied;
  private boolean proxyClosed;

  @Override
  public void dispatchGlobal(Message message) throws Exception {
    if (proxyClosed) {
      proxied.dispatch(message);
    }
    else {
      heldGlobalMessages.add(message);
    }
  }

  @Override
  public void dispatch(Message message) throws Exception {
    if (proxyClosed) {
      proxied.dispatch(message);
    }
    else {
      heldMessages.add(message);
    }
  }

  void closeProxy(RequestDispatcher dispatcher) {
    try {
      this.proxied = dispatcher;
      this.proxyClosed = true;

      for (Message message : heldMessages) {
        dispatcher.dispatch(message);
      }

      for (Message message : heldGlobalMessages) {
        dispatcher.dispatchGlobal(message);
      }

      heldMessages = null;
      heldGlobalMessages = null;

    }
    catch (Exception e) {
      throw new RuntimeException("failed to close proxy", e);
    }
  }
}
