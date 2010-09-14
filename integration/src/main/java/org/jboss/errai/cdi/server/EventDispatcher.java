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
package org.jboss.errai.cdi.server;

import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.api.MessageCallback;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.framework.MessageBus;
import org.jboss.errai.cdi.client.CDICommands;
import org.jboss.errai.cdi.client.CDIProtocol;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.BeanManager;

/**
 * Acts as a bridge between Errai Bus and the CDI event system.<br/>
 * Includes marshalling/unmarshalling of event types.
 */
public class EventDispatcher implements MessageCallback
{
  private BeanManager beanManager;

  private MessageBus erraiBus;

  public EventDispatcher(BeanManager beanManager, MessageBus erraiBus)
  {
    this.beanManager = beanManager;
    this.erraiBus = erraiBus;
  }

  // Invoked by Errai
  public void callback(Message message)
  {    
    try
    {
      switch (CDICommands.valueOf(message.getCommandType()))
      {
        case CDI_EVENT:
          String type = message.get(String.class, CDIProtocol.TYPE);
          Class clazz = Thread.currentThread().getContextClassLoader().loadClass(type);
          Object o = message.get(clazz, CDIProtocol.OBJECT_REF);
            try {
                CDIExtensionPoints.activateContexts(true);
                beanManager.fireEvent(o, new InboundQualifier());
            } finally {
                CDIExtensionPoints.activateContexts(false);
            }

            break;
        default:
          throw new IllegalArgumentException(
              "Unknown command type "+message.getCommandType());
      }
    }
    catch (Exception e)
    {
      throw new RuntimeException("Failed to dispatch CDI Event", e);
    }
  }

  // Invoked by Weld Event producer
  public void sendMessage(@Observes Outbound event)
  {   
    Object payload = event.getPayload();
    MessageBuilder.createMessage()
        .toSubject("cdi.event:"+payload.getClass().getName())
        .command(CDICommands.CDI_EVENT)
        .with(CDIProtocol.TYPE, payload.getClass().getName())
        .with(CDIProtocol.OBJECT_REF, event.getPayload())
        .noErrorHandling().sendNowWith(erraiBus);

  }  
}
