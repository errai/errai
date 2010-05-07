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
package org.jboss.errai.bus.server.service.bootstrap;

import org.jboss.errai.bus.server.service.ErraiServiceConfiguratorImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;

/**
 * Bootstrap Errai in an ordered fashion.
 *
 * @see org.jboss.errai.bus.server.service.bootstrap.BootstrapExecution
 * 
 * @author: Heiko Braun <hbraun@redhat.com>
 * @date: May 3, 2010
 */
public class OrderedBootstrap implements BootstrapExecution
{
  private Logger log = LoggerFactory.getLogger(OrderedBootstrap.class);
  
  private List<BootstrapExecution> bootstrap = new LinkedList<BootstrapExecution>();

  public OrderedBootstrap()
  {
    bootstrap.add(new DefaultComponents());
    bootstrap.add(new DefaultServices());
    bootstrap.add(new LoadExtensions());
    bootstrap.add(new DiscoverServices());
    bootstrap.add(new AuthenticationRules());
    bootstrap.add(new DefaultResources());
    bootstrap.add(new CleanupStartupFiles());
    bootstrap.add(new RegisterTypes());
    bootstrap.add(new BusConfiguration());
  }

  public void execute(final BootstrapContext context)
  {
    log.info("Bootstrap Errai");
        
    try
    {
      for(BootstrapExecution execution : bootstrap)
      {
        execution.execute(context);
      }

      // any deferred tasks?
      context.executeDeferred();

      // freeze config
      ((ErraiServiceConfiguratorImpl)context.getConfig()).lockdown();

      log.info("Bootstrap complete. Ready to rumble!");
      
    }
    catch (Exception e)
    {      
      throw new RuntimeException("Bootstrap failed", e);
    }
  }
}
