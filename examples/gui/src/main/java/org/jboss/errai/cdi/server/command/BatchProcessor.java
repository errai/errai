/*
 * Copyright 2009 JBoss, a divison Red Hat, Inc
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
package org.jboss.errai.cdi.server.command;

import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.framework.MessageBus;
import org.jboss.errai.bus.server.annotations.Command;
import org.jboss.errai.bus.server.annotations.Service;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

/**
 * @author: Heiko Braun <hbraun@redhat.com>
 * @date: Jul 20, 2010
 */
@ApplicationScoped
@Service
public class BatchProcessor
{
  @Inject
  MessageBus bus;

  @Command("start")
  public void startBatch(Message message)
  {
    MessageBuilder.createConversation(message)
        .subjectProvided()
        .with("response", "Batch processing has been started.")
        .done().reply();
  }

  @Command("stop")
  public void stopBatch(Message message)
  {
    MessageBuilder.createConversation(message)
        .subjectProvided()
        .with("response", "Batch processing has been stopped.")
        .done().reply();
  }
}
