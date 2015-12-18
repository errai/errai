/*
 * Copyright (C) 2009 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.cdi.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jboss.errai.bus.server.util.ServiceParser;

/**
 * Holds references to the types discovered when CDI bootraps.
 * These are used through the extension lifecycle, i.e. in {@link org.jboss.errai.cdi.server.events.ShutdownEventObserver}
 *
 * @author Heiko Braun <hbraun@redhat.com>
 * @author Mike Brock
 * @author Christian Sadilek <csadilek@redhat.com>
 * @author Max Barkley <mbarkley@redhat.com>
 */
public class TypeRegistry {

  private final Set<Class<?>> remoteInterfaces = new HashSet<Class<?>>();
  private final Map<Class<?>, List<ServiceParser>> services = new HashMap<Class<?>, List<ServiceParser>>();

  public void addRemoteInterface(final Class<?> intf) {
    remoteInterfaces.add(intf);
  }

  public Set<Class<?>> getRemoteInterfaces() {
    return remoteInterfaces;
  }
  
  /**
   * @return All registered beans which are services or contain methods which are services.
   */
  public Collection<Class<?>> getDelegateClasses() {
    return services.keySet();
  }
  
  /**
   * Get all the services associated with a delegate class.
   */
  public Collection<ServiceParser> getDelegateServices(Class<?> delegateClass) {
    return services.get(delegateClass);
  }
  
  /**
   * Register a service.
   */
  public void addService(ServiceParser service) {
    if (!services.containsKey(service.getDelegateClass()))
      services.put(service.getDelegateClass(), new ArrayList<ServiceParser>());
    services.get(service.getDelegateClass()).add(service);
  }
}
