/*
 * Copyright (C) 2011 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.ioc.support.bus.tests.server;

import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.bus.client.api.messaging.MessageCallback;
import org.jboss.errai.bus.client.api.BusErrorCallback;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.server.annotations.Service;

/**
 * @author Mike Brock
 */
@Service
public class LocalTestTesterService implements MessageCallback {
  @Override
  public void callback(Message message) {
    try {
      MessageBuilder.createConversation(message)
              .toSubject("LocalTestCompleteService")
              .errorsHandledBy(new BusErrorCallback() {
                @Override
                public boolean error(Message message, Throwable throwable) {
                  sendConfirmation(message);
                  return false;
                }
              })
              .reply();
    }
    catch (Throwable t) {
      sendConfirmation(message);
    }
  }

  private void sendConfirmation(Message message) {
    MessageBuilder.createConversation(message)
            .toSubject("LocalTestCompleteServiceConfirmation")
            .done().reply();
  }
}
