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

package org.jboss.errai.bus.server;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.framework.MessageBus;
import org.jboss.errai.bus.client.framework.RequestDispatcher;
import org.jboss.errai.bus.server.io.JSONEncoder;
import org.jboss.errai.bus.server.service.ErraiService;

import static org.jboss.errai.bus.client.util.ErrorHelper.handleMessageDeliveryFailure;

/**
 * Simple request dispatcher implementation.
 *
 * @see org.jboss.errai.bus.server.AsyncDispatcher
 */
@Singleton
public class SimpleDispatcher implements RequestDispatcher {
  private MessageBus bus;

  @Inject
  public SimpleDispatcher(ErraiService svc) {
    this.bus = svc.getBus();
  }

  public void dispatchGlobal(Message message) {
    try {
      bus.sendGlobal(message);
    }
    catch (QueueUnavailableException e) {
      handleMessageDeliveryFailure(bus, message, "Queue is not available", e, true);
    }
    catch (Throwable e) {
      message.setResource("Exception", e.getCause());
      handleMessageDeliveryFailure(bus, message, "Error calling remote service: " + message.getSubject(), e, false);
    }
  }

  public void dispatch(Message message) {
    try {
      bus.send(message);
    }
    catch (QueueUnavailableException e) {
      handleMessageDeliveryFailure(bus, message, "Queue is not available", e, true);
    }
    catch (Throwable e) {
      message.setResource("Exception", e.getCause());
      handleMessageDeliveryFailure(bus, message, "Error calling remote service: " + message.getSubject(), e, false);
    }
  }
}
