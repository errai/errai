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
package org.jboss.errai.cdi.client.tracker;

import org.jboss.errai.bus.client.ErraiBus;
import org.jboss.errai.bus.client.framework.MessageBus;
import org.jboss.errai.ioc.client.api.Provider;
import org.jboss.errai.ioc.client.api.TypeProvider;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;

/**
 * @author: Heiko Braun <hbraun@redhat.com>
 * @date: Jul 20, 2010
 */
@Provider
@ApplicationScoped
public class ServiceTrackerProvider implements TypeProvider<ServiceTracker>
{
  private MessageBus bus;

  public ServiceTrackerProvider()
  {
    this.bus = ErraiBus.get(); // TODO: CDI bean validation chokes on ctor injection
  }
  
  //@Produces
  //@Dependent
  public ServiceTracker provide() {
    return new ServiceTracker(bus);
  }
}