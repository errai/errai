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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedType;

/**
 * Holds references to the types discovered when CDI bootraps.
 * These are used through the extension lifecycle, i.e. in {@link org.jboss.errai.cdi.server.events.ShutdownEventObserver}
 *
 * @author Heiko Braun <hbraun@redhat.com>
 * @author Mike Brock
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class TypeRegistry {

  private final List<AnnotatedType> serviceEndpoints = new ArrayList<AnnotatedType>();
  private final Map<AnnotatedType, List<AnnotatedMethod>> serviceMethods = new HashMap<AnnotatedType, List<AnnotatedMethod>>();
  private final Set<Class<?>> remoteInterfaces = new HashSet<Class<?>>();

  public void addServiceEndpoint(final AnnotatedType service) {
    serviceEndpoints.add(service);
  }

  public void addServiceMethod(final AnnotatedType service, final AnnotatedMethod method) {
    if (!serviceMethods.containsKey(service)) {
      serviceMethods.put(service, new ArrayList<AnnotatedMethod>());
    }
    serviceMethods.get(service).add(method);
  }

  public void addRemoteInterface(final Class<?> intf) {
    remoteInterfaces.add(intf);
  }

  public List<AnnotatedType> getServiceEndpoints() {
    return serviceEndpoints;
  }

  public Map<AnnotatedType, List<AnnotatedMethod>> getServiceMethods() {
    return serviceMethods;
  }

  public Set<Class<?>> getRemoteInterfaces() {
    return remoteInterfaces;
  }
}
