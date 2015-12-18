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

package org.jboss.errai.bus.server.service.bootstrap;

import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.bus.client.api.messaging.MessageCallback;
import org.jboss.errai.bus.server.api.ServerMessageBus;
import org.jboss.errai.bus.server.service.ErraiService;

/**
 * Setup the default services.
 *
 * @author: Heiko Braun <hbraun@redhat.com>
 * @date: May 3, 2010
 */
class DefaultServices implements BootstrapExecution {

  public void execute(final BootstrapContext context) {
    final ServerMessageBus bus = context.getBus();

    /**
     * The standard ServerEchoService.
     */
    bus.subscribe(ErraiService.SERVER_ECHO_SERVICE, new MessageCallback() {
      public void callback(Message c) {
        MessageBuilder.createConversation(c)
            .subjectProvided().noErrorHandling()
            .sendNowWith(bus);
      }
    });

  }
}
