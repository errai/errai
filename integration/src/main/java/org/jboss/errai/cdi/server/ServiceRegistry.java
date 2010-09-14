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

import javax.enterprise.context.ApplicationScoped;
import java.util.*;

/**
 * Interim service registry that is used to register
 * CDI components with Errai when Errai bootstraps.
 *
 * @author: Heiko Braun <hbraun@redhat.com>
 * @date: Apr 6, 2010
 *
 */
@ApplicationScoped
public class ServiceRegistry
{
  private List<Class> services = new ArrayList<Class>();
  private Map<Class<?>, Class<?>> rpcEndpoints = new HashMap<Class<?>, Class<?>>();

  List<Class> getServices()
  {
    return Collections.unmodifiableList(services);
  }

  public void setServices(List<Class> discoveredServices)
  {
    this.services = discoveredServices;
  }

  public Map<Class<?>, Class<?>> getRpcEndpoints()
  {
    return Collections.unmodifiableMap(rpcEndpoints);
  }

  public void setRpcEndpoints(Map<Class<?>, Class<?>> rpcEndpoints)
  {
    this.rpcEndpoints = rpcEndpoints;
  }
}

