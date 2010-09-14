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
package org.jboss.errai.container;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import org.jboss.errai.bus.client.framework.MessageBus;
import org.jboss.errai.bus.server.ServerMessageBusImpl;
import org.jboss.errai.bus.server.api.ServerMessageBus;
import org.jboss.errai.bus.server.service.ErraiService;
import org.jboss.errai.bus.server.service.ErraiServiceConfigurator;
import org.jboss.errai.bus.server.service.ErraiServiceConfiguratorImpl;
import org.jboss.errai.bus.server.service.ErraiServiceImpl;
import org.jboss.util.naming.NonSerializableFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.InitialContext;
import javax.naming.Name;
import javax.naming.NamingException;

/**
 * Service wrapper to bootstrap Errai within JBoss AS.
 * 
 * @author: Heiko Braun <hbraun@redhat.com>
 * @date: Apr 23, 2010
 */
public class JBossService
{
  private static final Logger log = LoggerFactory.getLogger("Errai");

  protected ErraiService service;
  
  private String jndiName;
  
  public void start()
  {
    log.info("Starting Errai Service");

    this.service =
        Guice.createInjector(new AbstractModule() {
          public void configure() {
            bind(MessageBus.class).to(ServerMessageBusImpl.class);
            bind(ServerMessageBus.class).to(ServerMessageBusImpl.class);
            bind(ErraiService.class).to(ErraiServiceImpl.class);
            bind(ErraiServiceConfigurator.class).to(ErraiServiceConfiguratorImpl.class);
          }
        }).getInstance(ErraiService.class);

    try
    {
      rebind();
    }
    catch (NamingException e)
    {
      log.error("JNDI binding error", e);
    }
  }

  public void stop()
  {
    log.info("Shutdown Errai Service");
    unbind(jndiName);
  }

  public void setJndiName(String jndiName) throws NamingException
  {
    this.jndiName = jndiName;  
  }

  private void rebind() throws NamingException
    {
     InitialContext rootCtx = new InitialContext();
     Name fullName = rootCtx.getNameParser("").parse(jndiName);
     log.info("Bound to "+fullName);
     NonSerializableFactory.rebind(fullName, this.service, true);
   }

   private void unbind(String jndiName) {
     try {
       InitialContext rootCtx = new InitialContext();
       rootCtx.unbind(jndiName);
       NonSerializableFactory.unbind(jndiName);
     } catch(NamingException e) {
       log.error("Failed to unbind map", e);
     }
   }

}
