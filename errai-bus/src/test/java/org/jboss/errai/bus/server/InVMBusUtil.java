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

package org.jboss.errai.bus.server;

import org.jboss.errai.bus.server.cluster.jgroups.JGroupsClusteringProvider;
import org.jboss.errai.bus.server.service.ErraiConfigAttribs;
import org.jboss.errai.bus.server.service.ErraiService;
import org.jboss.errai.bus.server.service.ErraiServiceConfigurator;
import org.jboss.errai.bus.server.service.ErraiServiceConfiguratorImpl;
import org.jboss.errai.bus.server.service.ErraiServiceFactory;

/**
 * @author Mike Brock
 */
public class InVMBusUtil {

  public static ErraiService startService(final int portOffset) {
    final ErraiServiceConfigurator configurator = new ErraiServiceConfiguratorImpl();
    final int port = ErraiConfigAttribs.CLUSTER_PORT.getInt(configurator) + portOffset;
    ErraiConfigAttribs.CLUSTER_PORT.set(configurator, String.valueOf(port));
    ErraiConfigAttribs.ENABLE_CLUSTERING.set(configurator, "true");
    ErraiConfigAttribs.CLUSTERING_PROVIDER.set(configurator, JGroupsClusteringProvider.class.getName());
    ErraiConfigAttribs.AUTO_DISCOVER_SERVICES.set(configurator, "false");
    ErraiConfigAttribs.BUS_BUFFER_SIZE.set(configurator, "2"); // 2 MB
    return ErraiServiceFactory.create(configurator);
  }
}
