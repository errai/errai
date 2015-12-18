/*
 * Copyright (C) 2015 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.cdi.producer.server;

import javax.inject.Inject;

import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.bus.client.api.messaging.MessageBus;
import org.jboss.errai.bus.client.api.messaging.MessageCallback;
import org.jboss.errai.bus.server.annotations.Service;
import org.jboss.errai.cdi.producer.client.shared.LoggerTestUtil;
import org.jboss.errai.cdi.producer.client.shared.LoggerTestUtil.TestCommand;
import org.jboss.errai.common.client.api.annotations.NamedLogger;
import org.jboss.errai.common.client.protocols.MessageParts;
import org.slf4j.Logger;

@Service
public class ClassWithNamedLoggerField implements MessageCallback {

  @Inject
  @NamedLogger(LoggerTestUtil.LOGGER_NAME)
  private Logger logger;
  @Inject
  private MessageBus bus;

  @Override
  public void callback(final Message message) {
    final TestCommand command = TestCommand.valueOf(message.get(String.class, MessageParts.CommandType));
    final Boolean res;
    if (command.equals(TestCommand.IS_NOT_NULL)) {
      res = logger != null;
      MessageBuilder.createConversation(message).subjectProvided().with(LoggerTestUtil.RESULT_PART, res).done()
              .sendNowWith(bus);
    }
    else if (command.equals(TestCommand.IS_CORRECT_NAME)) {
      res = logger.getName().equals(LoggerTestUtil.LOGGER_NAME);
      MessageBuilder.createConversation(message).subjectProvided().with(LoggerTestUtil.RESULT_PART, res).done()
              .sendNowWith(bus);
    }
  }
}
