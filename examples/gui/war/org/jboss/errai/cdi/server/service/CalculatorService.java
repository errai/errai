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
package org.jboss.errai.cdi.server.service;

import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.api.MessageCallback;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.framework.MessageBus;
import org.jboss.errai.bus.server.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

/**
 * Using a managed bean as service.
 * Dependencies provided by CDI container.
 * 
 * @author: Heiko Braun <hbraun@redhat.com>
 * @date: Apr 6, 2010
 */
@Service("calculator")
@ApplicationScoped
public class CalculatorService implements MessageCallback
{

  private static final Logger log =
      LoggerFactory.getLogger(CalculatorService.class);

  @Inject
  MessageBus bus;

  @Inject
  Calculator calculator;

  public void callback(Message message)
  {
    log.debug("CalculatorService received: "+message);
    
    if(null==calculator)
      new RuntimeException("Not CDI managed").printStackTrace();

    Long a = message.get(Long.class, "a");
    Long b = message.get(Long.class, "b");

    Long result = calculator.add(a, b);

    MessageBuilder.createConversation(message)
        .subjectProvided()
        .signalling()
        .with("result", result)
        .noErrorHandling()
        .sendNowWith(bus);
  }
}
